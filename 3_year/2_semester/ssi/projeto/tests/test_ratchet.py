"""Tests for Double Ratchet state evolution and security guarantees."""

from __future__ import annotations

import pytest
from cryptography.exceptions import InvalidTag

from config import RATCHET_MAX_SKIP

from src.crypto.asymmetric import generate_x25519_keypair, serialize_public_key
from src.crypto.ratchet import (
	RatchetState,
	ratchet_decrypt,
	ratchet_encrypt,
	ratchet_init_recipient,
	ratchet_init_sender,
)


def _init_states() -> tuple[RatchetState, RatchetState]:
	"""Initialize sender/recipient ratchet states sharing one bootstrap secret."""
	session_key = b"\x11" * 32
	bob_spk_priv, bob_spk_pub = generate_x25519_keypair()
	sender = ratchet_init_sender(session_key, serialize_public_key(bob_spk_pub))
	recipient = ratchet_init_recipient(session_key, bob_spk_priv)
	return sender, recipient


def test_ratchet_single_message_roundtrip() -> None:
	"""Ensure one encrypted ratchet message decrypts successfully."""
	sender, recipient = _init_states()

	header, nonce, ciphertext = ratchet_encrypt(sender, b"hello", aad=b"ctx")
	plaintext = ratchet_decrypt(recipient, header, nonce, ciphertext, aad=b"ctx")

	assert plaintext == b"hello"


def test_ratchet_multiple_messages_out_of_order() -> None:
	"""Ensure recipient can decrypt skipped messages delivered out of order."""
	sender, recipient = _init_states()

	msg1 = ratchet_encrypt(sender, b"m1", aad=b"chat")
	msg2 = ratchet_encrypt(sender, b"m2", aad=b"chat")
	msg3 = ratchet_encrypt(sender, b"m3", aad=b"chat")

	assert ratchet_decrypt(recipient, *msg2, aad=b"chat") == b"m2"
	assert ratchet_decrypt(recipient, *msg1, aad=b"chat") == b"m1"
	assert ratchet_decrypt(recipient, *msg3, aad=b"chat") == b"m3"


def test_ratchet_rejects_tampered_ciphertext() -> None:
	"""Ensure modified ciphertext is rejected by AEAD authentication."""
	sender, recipient = _init_states()

	header, nonce, ciphertext = ratchet_encrypt(sender, b"authenticated", aad=b"aad")
	tampered = ciphertext[:-1] + bytes([ciphertext[-1] ^ 0x01])

	with pytest.raises(InvalidTag):
		ratchet_decrypt(recipient, header, nonce, tampered, aad=b"aad")


def test_ratchet_forward_progress_changes_message_keys() -> None:
	"""Ensure ratchet state advances and yields distinct encrypted outputs."""
	sender, _ = _init_states()

	header1, nonce1, ct1 = ratchet_encrypt(sender, b"first", aad=b"a")
	header2, nonce2, ct2 = ratchet_encrypt(sender, b"second", aad=b"a")

	assert header1.n == 0
	assert header2.n == 1
	assert nonce1 != nonce2
	assert ct1 != ct2


def test_ratchet_out_of_order_with_gap_and_replay_rejection() -> None:
	"""Ensure large out-of-order gaps decrypt, and consumed skipped keys cannot be replayed."""
	sender, recipient = _init_states()
	packets = [ratchet_encrypt(sender, f"m{i}".encode(), aad=b"chat") for i in range(6)]

	assert ratchet_decrypt(recipient, *packets[4], aad=b"chat") == b"m4"
	assert ratchet_decrypt(recipient, *packets[1], aad=b"chat") == b"m1"
	assert ratchet_decrypt(recipient, *packets[0], aad=b"chat") == b"m0"
	assert ratchet_decrypt(recipient, *packets[5], aad=b"chat") == b"m5"

	with pytest.raises(InvalidTag):
		ratchet_decrypt(recipient, *packets[1], aad=b"chat")


def test_ratchet_rejects_gap_above_max_skip_limit() -> None:
	"""Ensure recipient rejects packets that require skipping too many message keys."""
	sender, recipient = _init_states()
	latest_packet: tuple | None = None

	for i in range(RATCHET_MAX_SKIP + 2):
		latest_packet = ratchet_encrypt(sender, f"gap-{i}".encode(), aad=b"gap")

	assert latest_packet is not None
	with pytest.raises(ValueError, match="Exceeded maximum skipped message keys"):
		ratchet_decrypt(recipient, *latest_packet, aad=b"gap")
