"""Double Ratchet implementation for E2EE forward secrecy and post-compromise recovery."""

from __future__ import annotations

import base64
import json
from dataclasses import dataclass, field

from cryptography.hazmat.primitives.asymmetric.x25519 import X25519PrivateKey

from config import RATCHET_MAX_SKIP

from .asymmetric import (
	deserialize_x25519_public,
	generate_x25519_keypair,
	serialize_public_key,
	x25519_exchange,
)
from .kdf import kdf_ck, kdf_rk
from .primitives import aes_gcm_decrypt, aes_gcm_encrypt


@dataclass
class MessageHeader:
	"""Metadata needed for receiver ratchet synchronization."""

	dh_pub: bytes
	pn: int
	n: int


@dataclass
class RatchetState:
	"""Mutable state of a two-party Double Ratchet session."""

	dh_send_priv: X25519PrivateKey | None
	dh_send_pub: bytes | None
	dh_recv_pub: bytes | None
	root_key: bytes
	chain_key_send: bytes | None = None
	chain_key_recv: bytes | None = None
	send_count: int = 0
	recv_count: int = 0
	prev_send_count: int = 0
	skipped: dict[tuple[bytes, int], bytes] = field(default_factory=dict)


def ratchet_init_sender(session_key: bytes, recipient_ratchet_pub: bytes) -> RatchetState:
	"""Initialize sender state using recipient initial ratchet public key."""
	dh_send_priv, dh_send_pub_obj = generate_x25519_keypair()
	dh_send_pub = serialize_public_key(dh_send_pub_obj)

	dh_out = x25519_exchange(dh_send_priv, deserialize_x25519_public(recipient_ratchet_pub))
	root_key, chain_key_send = kdf_rk(session_key, dh_out)

	return RatchetState(
		dh_send_priv=dh_send_priv,
		dh_send_pub=dh_send_pub,
		dh_recv_pub=recipient_ratchet_pub,
		root_key=root_key,
		chain_key_send=chain_key_send,
		chain_key_recv=None,
	)


def ratchet_init_recipient(
	session_key: bytes,
	recipient_ratchet_priv: X25519PrivateKey,
) -> RatchetState:
	"""Initialize recipient state with its current ratchet private key."""
	return RatchetState(
		dh_send_priv=recipient_ratchet_priv,
		dh_send_pub=serialize_public_key(recipient_ratchet_priv.public_key()),
		dh_recv_pub=None,
		root_key=session_key,
		chain_key_send=None,
		chain_key_recv=None,
	)


def ratchet_encrypt(
	state: RatchetState,
	plaintext: bytes,
	aad: bytes = b"",
) -> tuple[MessageHeader, bytes, bytes]:
	"""Encrypt a message using the sending chain and advance state."""
	if state.chain_key_send is None or state.dh_send_pub is None:
		raise ValueError("Sending chain is not initialized")

	state.chain_key_send, message_key = kdf_ck(state.chain_key_send)
	header = MessageHeader(dh_pub=state.dh_send_pub, pn=state.prev_send_count, n=state.send_count)
	state.send_count += 1

	nonce, ciphertext = aes_gcm_encrypt(message_key, plaintext, _compose_aad(aad, header))
	return header, nonce, ciphertext


def ratchet_decrypt(
	state: RatchetState,
	header: MessageHeader,
	nonce: bytes,
	ciphertext: bytes,
	aad: bytes = b"",
) -> bytes:
	"""Decrypt a ratcheted message and advance receiving state safely."""
	skipped_key = state.skipped.pop((header.dh_pub, header.n), None)
	if skipped_key is not None:
		return aes_gcm_decrypt(skipped_key, nonce, ciphertext, _compose_aad(aad, header))

	if state.dh_recv_pub != header.dh_pub:
		_skip_message_keys(state, header.pn)
		_dh_ratchet_step(state, header)

	_skip_message_keys(state, header.n)

	if state.chain_key_recv is None:
		raise ValueError("Receiving chain is not initialized")

	state.chain_key_recv, message_key = kdf_ck(state.chain_key_recv)
	state.recv_count += 1
	return aes_gcm_decrypt(message_key, nonce, ciphertext, _compose_aad(aad, header))


def _dh_ratchet_step(state: RatchetState, header: MessageHeader) -> None:
	"""Advance ratchet when peer rotates its DH key."""
	if state.dh_send_priv is None:
		raise ValueError("Local DH private key is not initialized")

	peer_pub = deserialize_x25519_public(header.dh_pub)

	state.prev_send_count = state.send_count
	state.send_count = 0
	state.recv_count = 0

	dh_recv_out = x25519_exchange(state.dh_send_priv, peer_pub)
	state.root_key, state.chain_key_recv = kdf_rk(state.root_key, dh_recv_out)
	state.dh_recv_pub = header.dh_pub

	state.dh_send_priv, new_send_pub = generate_x25519_keypair()
	state.dh_send_pub = serialize_public_key(new_send_pub)
	dh_send_out = x25519_exchange(state.dh_send_priv, peer_pub)
	state.root_key, state.chain_key_send = kdf_rk(state.root_key, dh_send_out)


def _skip_message_keys(state: RatchetState, until: int) -> None:
	"""Derive and cache skipped receiving keys until target message number."""
	if until < state.recv_count:
		return

	if until - state.recv_count > RATCHET_MAX_SKIP:
		raise ValueError("Exceeded maximum skipped message keys")

	if state.chain_key_recv is None:
		if until == state.recv_count:
			return
		raise ValueError("Receiving chain is not initialized")

	while state.recv_count < until:
		state.chain_key_recv, skipped_mk = kdf_ck(state.chain_key_recv)
		key = (state.dh_recv_pub or b"", state.recv_count)
		state.skipped[key] = skipped_mk
		state.recv_count += 1

		if len(state.skipped) > RATCHET_MAX_SKIP:
			oldest_key = next(iter(state.skipped))
			state.skipped.pop(oldest_key)


def _serialize_header(header: MessageHeader) -> bytes:
	"""Serialize header deterministically for AEAD authentication."""
	payload = {
		"dh_pub": base64.b64encode(header.dh_pub).decode("ascii"),
		"pn": header.pn,
		"n": header.n,
	}
	return json.dumps(payload, sort_keys=True, separators=(",", ":")).encode("utf-8")


def _compose_aad(aad: bytes, header: MessageHeader) -> bytes:
	"""Bind external AAD and header into a single AEAD-associated payload."""
	header_bytes = _serialize_header(header)
	if aad:
		return aad + b"|" + header_bytes
	return header_bytes
