"""Unit tests for low-level cryptographic primitives."""

from __future__ import annotations

import os

import pytest
from cryptography.exceptions import InvalidTag

from src.crypto.asymmetric import (
	deserialize_ed25519_public,
	deserialize_x25519_public,
	ed25519_sign,
	ed25519_verify,
	generate_ed25519_keypair,
	generate_x25519_keypair,
	serialize_private_key,
	serialize_public_key,
	x25519_exchange,
)
from src.crypto.kdf import hkdf_derive, kdf_ck, kdf_rk
from src.crypto.primitives import aes_gcm_decrypt, aes_gcm_encrypt
from src.crypto.x3dh import KeyBundle, X3DHRecipientKeys, x3dh_recipient, x3dh_sender


def _build_x3dh_fixture() -> tuple[tuple, KeyBundle, X3DHRecipientKeys]:
	"""Build valid sender/recipient material for X3DH corruption tests."""
	alice_ik_ed_priv, _ = generate_ed25519_keypair()
	alice_ik_x_priv, alice_ik_x_pub = generate_x25519_keypair()

	bob_ik_ed_priv, bob_ik_ed_pub = generate_ed25519_keypair()
	bob_ik_x_priv, bob_ik_x_pub = generate_x25519_keypair()
	bob_spk_priv, bob_spk_pub = generate_x25519_keypair()

	bundle = KeyBundle(
		identity_key=serialize_public_key(bob_ik_ed_pub),
		identity_dh_key=serialize_public_key(bob_ik_x_pub),
		signed_prekey=serialize_public_key(bob_spk_pub),
		signed_prekey_sig=ed25519_sign(bob_ik_ed_priv, serialize_public_key(bob_spk_pub)),
		onetime_prekey=None,
		onetime_prekey_id=None,
	)

	recipient_keys = X3DHRecipientKeys(
		identity_priv=bob_ik_ed_priv,
		identity_x25519_priv=bob_ik_x_priv,
		signed_prekey_priv=bob_spk_priv,
		onetime_priv=None,
	)

	sender_keys = (alice_ik_ed_priv, alice_ik_x_priv, alice_ik_x_pub)
	return sender_keys, bundle, recipient_keys


def test_aes_gcm_roundtrip_and_nonce_uniqueness() -> None:
	"""Ensure AES-GCM decrypts correctly and uses unique nonces per encryption."""
	key = os.urandom(32)
	plaintext = b"hello e2ee"
	aad = b"header"

	nonce1, ct1 = aes_gcm_encrypt(key, plaintext, aad)
	nonce2, ct2 = aes_gcm_encrypt(key, plaintext, aad)

	assert nonce1 != nonce2
	assert aes_gcm_decrypt(key, nonce1, ct1, aad) == plaintext
	assert aes_gcm_decrypt(key, nonce2, ct2, aad) == plaintext


def test_aes_gcm_invalid_tag_on_wrong_aad() -> None:
	"""Ensure tampered AAD causes AES-GCM authentication failure."""
	key = os.urandom(32)
	nonce, ciphertext = aes_gcm_encrypt(key, b"secret", b"good")

	with pytest.raises(InvalidTag):
		aes_gcm_decrypt(key, nonce, ciphertext, b"bad")


def test_hkdf_is_deterministic_per_domain() -> None:
	"""Ensure HKDF output is deterministic for same domain inputs."""
	ikm = b"ikm"
	salt = b"salt"
	info = b"context"

	out1 = hkdf_derive(ikm, 32, salt=salt, info=info)
	out2 = hkdf_derive(ikm, 32, salt=salt, info=info)
	out3 = hkdf_derive(ikm, 32, salt=salt, info=b"other")

	assert out1 == out2
	assert out1 != out3


def test_ratchet_kdfs_return_two_distinct_keys() -> None:
	"""Ensure ratchet KDF steps return correctly sized distinct key outputs."""
	root_key = os.urandom(32)
	dh_output = os.urandom(32)
	chain_key = os.urandom(32)

	new_root, new_chain = kdf_rk(root_key, dh_output)
	next_chain, message_key = kdf_ck(chain_key)

	assert len(new_root) == 32
	assert len(new_chain) == 32
	assert len(next_chain) == 32
	assert len(message_key) == 32
	assert next_chain != message_key


def test_ed25519_sign_verify_and_tamper_detection() -> None:
	"""Ensure Ed25519 signatures verify valid data and reject tampered data."""
	private_key, public_key = generate_ed25519_keypair()
	data = b"signed payload"
	signature = ed25519_sign(private_key, data)

	assert ed25519_verify(public_key, signature, data)
	assert not ed25519_verify(public_key, signature, b"tampered payload")


def test_ed25519_signature_from_other_key_is_invalid() -> None:
	"""Ensure a signature cannot be verified with a different Ed25519 public key."""
	private_a, public_a = generate_ed25519_keypair()
	_, public_b = generate_ed25519_keypair()
	data = b"message-authentication"
	signature = ed25519_sign(private_a, data)

	assert ed25519_verify(public_a, signature, data)
	assert not ed25519_verify(public_b, signature, data)


def test_x25519_exchange_matches_both_sides() -> None:
	"""Ensure X25519 shared secret matches on both participating sides."""
	a_priv, a_pub = generate_x25519_keypair()
	b_priv, b_pub = generate_x25519_keypair()

	secret_ab = x25519_exchange(a_priv, b_pub)
	secret_ba = x25519_exchange(b_priv, a_pub)

	assert secret_ab == secret_ba
	assert len(secret_ab) == 32


def test_public_and_private_key_serialization() -> None:
	"""Ensure key serialization and deserialization preserve raw key bytes."""
	ed_priv, ed_pub = generate_ed25519_keypair()
	x_priv, x_pub = generate_x25519_keypair()

	ed_pub_raw = serialize_public_key(ed_pub)
	x_pub_raw = serialize_public_key(x_pub)
	ed_priv_raw = serialize_private_key(ed_priv)
	x_priv_raw = serialize_private_key(x_priv)

	assert len(ed_pub_raw) == 32
	assert len(x_pub_raw) == 32
	assert len(ed_priv_raw) == 32
	assert len(x_priv_raw) == 32

	assert serialize_public_key(deserialize_ed25519_public(ed_pub_raw)) == ed_pub_raw
	assert serialize_public_key(deserialize_x25519_public(x_pub_raw)) == x_pub_raw


def test_x3dh_sender_rejects_corrupted_identity_dh_key_packet() -> None:
	"""Ensure malformed recipient identity DH key bytes are rejected in X3DH sender path."""
	sender_keys, bundle, _ = _build_x3dh_fixture()
	alice_ik_ed_priv, alice_ik_x_priv, _ = sender_keys

	corrupted_bundle = KeyBundle(
		identity_key=bundle.identity_key,
		identity_dh_key=b"\x01" * 31,
		signed_prekey=bundle.signed_prekey,
		signed_prekey_sig=bundle.signed_prekey_sig,
		onetime_prekey=bundle.onetime_prekey,
		onetime_prekey_id=bundle.onetime_prekey_id,
	)

	with pytest.raises(ValueError, match="X25519 public key must be 32 bytes"):
		x3dh_sender(alice_ik_ed_priv, alice_ik_x_priv, corrupted_bundle)


def test_x3dh_recipient_rejects_corrupted_ephemeral_packet() -> None:
	"""Ensure malformed sender ephemeral bytes are rejected in X3DH recipient path."""
	sender_keys, _, recipient_keys = _build_x3dh_fixture()
	_, _, alice_ik_x_pub = sender_keys

	with pytest.raises(ValueError, match="X25519 public key must be 32 bytes"):
		x3dh_recipient(
			recipient_keys,
			sender_identity_pub_bytes=b"unused",
			sender_identity_x25519_pub_bytes=serialize_public_key(alice_ik_x_pub),
			sender_ephemeral_pub_bytes=b"\x02" * 31,
			onetime_key_id=None,
		)
