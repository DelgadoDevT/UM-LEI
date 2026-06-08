"""Tests for X3DH key agreement implementation."""

from __future__ import annotations

import pytest

from src.crypto.asymmetric import (
	ed25519_sign,
	generate_ed25519_keypair,
	generate_x25519_keypair,
	serialize_public_key,
)
from src.crypto.x3dh import KeyBundle, X3DHRecipientKeys, x3dh_recipient, x3dh_sender


def _build_fixture(with_opk: bool) -> tuple[tuple, KeyBundle, X3DHRecipientKeys]:
	"""Build sender/recipient key material for X3DH tests."""
	alice_ik_ed_priv, _ = generate_ed25519_keypair()
	alice_ik_x_priv, alice_ik_x_pub = generate_x25519_keypair()

	bob_ik_ed_priv, bob_ik_ed_pub = generate_ed25519_keypair()
	bob_ik_x_priv, bob_ik_x_pub = generate_x25519_keypair()
	bob_spk_priv, bob_spk_pub = generate_x25519_keypair()

	opk_priv = None
	opk_pub_bytes = None
	opk_id = None
	if with_opk:
		opk_priv, opk_pub = generate_x25519_keypair()
		opk_pub_bytes = serialize_public_key(opk_pub)
		opk_id = 7

	bundle = KeyBundle(
		identity_key=serialize_public_key(bob_ik_ed_pub),
		identity_dh_key=serialize_public_key(bob_ik_x_pub),
		signed_prekey=serialize_public_key(bob_spk_pub),
		signed_prekey_sig=ed25519_sign(
			bob_ik_ed_priv,
			serialize_public_key(bob_spk_pub),
		),
		onetime_prekey=opk_pub_bytes,
		onetime_prekey_id=opk_id,
	)

	recipient_keys = X3DHRecipientKeys(
		identity_priv=bob_ik_ed_priv,
		identity_x25519_priv=bob_ik_x_priv,
		signed_prekey_priv=bob_spk_priv,
		onetime_priv=opk_priv,
	)
	sender_keys = (alice_ik_ed_priv, alice_ik_x_priv, alice_ik_x_pub)
	return sender_keys, bundle, recipient_keys


def test_x3dh_sender_and_recipient_derive_same_session_key() -> None:
	"""Validate both sides derive the same session key when OPK is used."""
	sender_keys, bundle, recipient_keys = _build_fixture(with_opk=True)
	alice_ik_ed_priv, alice_ik_x_priv, alice_ik_x_pub = sender_keys

	sender_out = x3dh_sender(alice_ik_ed_priv, alice_ik_x_priv, bundle)
	recipient_sk = x3dh_recipient(
		recipient_keys,
		sender_identity_pub_bytes=b"unused",
		sender_identity_x25519_pub_bytes=serialize_public_key(alice_ik_x_pub),
		sender_ephemeral_pub_bytes=sender_out.ephemeral_pub,
		onetime_key_id=sender_out.onetime_key_id,
	)

	assert sender_out.session_key == recipient_sk
	assert len(sender_out.session_key) == 32


def test_x3dh_works_without_onetime_prekey() -> None:
	"""Validate X3DH succeeds when recipient has no one-time prekey."""
	sender_keys, bundle, recipient_keys = _build_fixture(with_opk=False)
	alice_ik_ed_priv, alice_ik_x_priv, alice_ik_x_pub = sender_keys

	sender_out = x3dh_sender(alice_ik_ed_priv, alice_ik_x_priv, bundle)
	recipient_sk = x3dh_recipient(
		recipient_keys,
		sender_identity_pub_bytes=b"unused",
		sender_identity_x25519_pub_bytes=serialize_public_key(alice_ik_x_pub),
		sender_ephemeral_pub_bytes=sender_out.ephemeral_pub,
		onetime_key_id=sender_out.onetime_key_id,
	)

	assert sender_out.session_key == recipient_sk
	assert sender_out.onetime_key_id is None


def test_x3dh_rejects_invalid_signed_prekey_signature() -> None:
	"""Reject bundle when signed prekey signature verification fails."""
	sender_keys, bundle, _ = _build_fixture(with_opk=False)
	alice_ik_ed_priv, alice_ik_x_priv, _ = sender_keys

	tampered = KeyBundle(
		identity_key=bundle.identity_key,
		identity_dh_key=bundle.identity_dh_key,
		signed_prekey=bundle.signed_prekey,
		signed_prekey_sig=b"\x00" * 64,
		onetime_prekey=bundle.onetime_prekey,
		onetime_prekey_id=bundle.onetime_prekey_id,
	)

	with pytest.raises(ValueError, match="Invalid signed prekey signature"):
		x3dh_sender(alice_ik_ed_priv, alice_ik_x_priv, tampered)


def test_x3dh_rejects_missing_opk_private_on_recipient() -> None:
	"""Reject recipient processing when referenced OPK private key is absent."""
	sender_keys, bundle, recipient_keys = _build_fixture(with_opk=True)
	alice_ik_ed_priv, alice_ik_x_priv, alice_ik_x_pub = sender_keys
	sender_out = x3dh_sender(alice_ik_ed_priv, alice_ik_x_priv, bundle)

	recipient_keys_without_opk = X3DHRecipientKeys(
		identity_priv=recipient_keys.identity_priv,
		identity_x25519_priv=recipient_keys.identity_x25519_priv,
		signed_prekey_priv=recipient_keys.signed_prekey_priv,
		onetime_priv=None,
	)

	with pytest.raises(ValueError, match="One-time prekey id provided"):
		x3dh_recipient(
			recipient_keys_without_opk,
			sender_identity_pub_bytes=b"unused",
			sender_identity_x25519_pub_bytes=serialize_public_key(alice_ik_x_pub),
			sender_ephemeral_pub_bytes=sender_out.ephemeral_pub,
			onetime_key_id=sender_out.onetime_key_id,
		)
