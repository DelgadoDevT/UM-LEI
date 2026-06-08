"""X3DH key agreement implementation for asynchronous session bootstrap."""

from __future__ import annotations

from dataclasses import dataclass

from cryptography.hazmat.primitives.asymmetric.ed25519 import Ed25519PrivateKey
from cryptography.hazmat.primitives.asymmetric.x25519 import X25519PrivateKey

from .asymmetric import (
	deserialize_ed25519_public,
	deserialize_x25519_public,
	ed25519_verify,
	generate_x25519_keypair,
	serialize_public_key,
	x25519_exchange,
)
from .kdf import hkdf_derive


@dataclass
class KeyBundle:
	"""Public key material published to bootstrap X3DH sessions."""

	identity_key: bytes
	identity_dh_key: bytes
	signed_prekey: bytes
	signed_prekey_sig: bytes
	onetime_prekey: bytes | None
	onetime_prekey_id: int | None


@dataclass
class X3DHSenderResult:
	"""Sender-side output needed to start a ratcheted session."""

	session_key: bytes
	ephemeral_pub: bytes
	onetime_key_id: int | None


@dataclass
class X3DHRecipientKeys:
	"""Recipient private keys required to mirror sender DH computations."""

	identity_priv: Ed25519PrivateKey
	identity_x25519_priv: X25519PrivateKey
	signed_prekey_priv: X25519PrivateKey
	onetime_priv: X25519PrivateKey | None


def x3dh_sender(
	sender_identity_priv: Ed25519PrivateKey,
	sender_identity_x25519_priv: X25519PrivateKey,
	recipient_bundle: KeyBundle,
) -> X3DHSenderResult:
	"""Perform X3DH as sender and return session key plus ephemeral public key.

	Security guarantees:
	- Verifies signed prekey authenticity before any DH operation.
	- Produces a fresh session key bound to both identities and ephemeral key.
	"""
	del sender_identity_priv  # Signing key is kept for API symmetry and future transcript binding.

	recipient_ik_ed_pub = deserialize_ed25519_public(recipient_bundle.identity_key)
	if not ed25519_verify(
		recipient_ik_ed_pub,
		recipient_bundle.signed_prekey_sig,
		recipient_bundle.signed_prekey,
	):
		raise ValueError("Invalid signed prekey signature")

	recipient_ik_dh_pub = deserialize_x25519_public(recipient_bundle.identity_dh_key)
	recipient_spk_pub = deserialize_x25519_public(recipient_bundle.signed_prekey)

	ephemeral_priv, ephemeral_pub = generate_x25519_keypair()
	ephemeral_pub_bytes = serialize_public_key(ephemeral_pub)

	dh1 = x25519_exchange(sender_identity_x25519_priv, recipient_spk_pub)
	dh2 = x25519_exchange(ephemeral_priv, recipient_ik_dh_pub)
	dh3 = x25519_exchange(ephemeral_priv, recipient_spk_pub)

	material = dh1 + dh2 + dh3
	if recipient_bundle.onetime_prekey is not None:
		recipient_opk_pub = deserialize_x25519_public(recipient_bundle.onetime_prekey)
		dh4 = x25519_exchange(ephemeral_priv, recipient_opk_pub)
		material += dh4

	session_key = hkdf_derive(
		input_key_material=material,
		length=32,
		salt=b"\x00" * 32,
		info=b"X3DH",
	)
	return X3DHSenderResult(
		session_key=session_key,
		ephemeral_pub=ephemeral_pub_bytes,
		onetime_key_id=recipient_bundle.onetime_prekey_id,
	)


def x3dh_recipient(
	recipient_keys: X3DHRecipientKeys,
	sender_identity_pub_bytes: bytes,
	sender_identity_x25519_pub_bytes: bytes,
	sender_ephemeral_pub_bytes: bytes,
	onetime_key_id: int | None,
) -> bytes:
	"""Perform X3DH as recipient and derive the same 32-byte session key.

	Security guarantees:
	- Mirrors sender computations exactly so key confirmation can be implicit.
	- Includes optional one-time prekey DH when that prekey was consumed.
	"""
	del sender_identity_pub_bytes  # Reserved for optional identity transcript checks.

	sender_ik_dh_pub = deserialize_x25519_public(sender_identity_x25519_pub_bytes)
	sender_ek_pub = deserialize_x25519_public(sender_ephemeral_pub_bytes)

	dh1 = x25519_exchange(recipient_keys.signed_prekey_priv, sender_ik_dh_pub)
	dh2 = x25519_exchange(recipient_keys.identity_x25519_priv, sender_ek_pub)
	dh3 = x25519_exchange(recipient_keys.signed_prekey_priv, sender_ek_pub)

	material = dh1 + dh2 + dh3
	if onetime_key_id is not None:
		if recipient_keys.onetime_priv is None:
			raise ValueError("One-time prekey id provided but no private key available")
		material += x25519_exchange(recipient_keys.onetime_priv, sender_ek_pub)

	return hkdf_derive(
		input_key_material=material,
		length=32,
		salt=b"\x00" * 32,
		info=b"X3DH",
	)
