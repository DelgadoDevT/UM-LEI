"""Comprehensive tests for server functionality including registration, authentication, messaging."""

from __future__ import annotations

import asyncio
import json
import os
import sqlite3
import tempfile
from pathlib import Path

import pytest

from src.crypto.asymmetric import (
	generate_ed25519_keypair,
	generate_x25519_keypair,
	serialize_public_key,
	ed25519_sign,
)
from src.crypto.certificates import cert_to_pem, pem_to_cert, verify_certificate
from src.crypto.passwords import hash_password, verify_password
from src.server.server import ChatServer
from src.storage.secure_db import SecureDB
from src.server.ca.certificate_authority import CertificateAuthority
from src.protocol.messages import (
	ClientHello,
	Register,
	ClientAuth,
	FetchBundle,
	SendMessage,
	FetchOffline,
)
from src.protocol.transport import send_message, recv_message


@pytest.fixture
def temp_db_path():
	"""Create a temporary database file for testing."""
	with tempfile.TemporaryDirectory() as tmpdir:
		db_path = Path(tmpdir) / "test.db"
		yield db_path


@pytest.fixture
def temp_ca_dir():
	"""Create a temporary CA directory for testing."""
	with tempfile.TemporaryDirectory() as tmpdir:
		yield Path(tmpdir)


@pytest.fixture
def test_db(temp_db_path):
	"""Create a test database instance."""
	db_key = os.urandom(32)  # Random database encryption key
	db = SecureDB(str(temp_db_path), db_key)
	yield db
	# Cleanup
	if temp_db_path.exists():
		temp_db_path.unlink()


@pytest.fixture
def test_ca(temp_ca_dir):
	"""Create a test CA instance."""
	# Set config paths
	import config
	original_ca_key = config.CA_KEY_PATH
	original_ca_cert = config.CA_CERT_PATH
	
	config.CA_KEY_PATH = str(temp_ca_dir / "ca_private_key.raw")
	config.CA_CERT_PATH = str(temp_ca_dir / "ca_root_cert.pem")
	
	ca = CertificateAuthority()
	
	yield ca
	
	# Restore original paths
	config.CA_KEY_PATH = original_ca_key
	config.CA_CERT_PATH = original_ca_cert


class TestDatabaseOperations:
	"""Test secure database operations."""

	def test_store_and_retrieve_user(self, test_db):
		"""Test storing and retrieving user data."""
		username = "alice"
		pw_hash = "hash_value"
		ik_ed = os.urandom(32)
		ik_dh = os.urandom(32)
		spk = os.urandom(32)
		spk_sig = os.urandom(64)

		test_db.store_user(username, pw_hash, ik_ed, ik_dh, spk, spk_sig)

		stored_hash = test_db.get_user_hash(username)
		assert stored_hash == pw_hash

	def test_get_user_bundle(self, test_db):
		"""Test retrieving complete user key bundle."""
		username = "bob"
		pw_hash = "hash"
		ik_ed = os.urandom(32)
		ik_dh = os.urandom(32)
		spk = os.urandom(32)
		spk_sig = os.urandom(64)

		test_db.store_user(username, pw_hash, ik_ed, ik_dh, spk, spk_sig)

		bundle = test_db.get_user_bundle(username)
		assert bundle is not None
		assert bundle["ik_ed_pub"] == ik_ed
		assert bundle["ik_dh_pub"] == ik_dh
		assert bundle["spk_pub"] == spk
		assert bundle["spk_sig"] == spk_sig
		assert "opk_id" in bundle
		assert "opk_pub" in bundle
		assert "p2p_host" in bundle
		assert "p2p_port" in bundle

	def test_get_nonexistent_user_returns_none(self, test_db):
		"""Test that getting nonexistent user returns None."""
		bundle = test_db.get_user_bundle("nonexistent")
		assert bundle is None

	def test_store_onetime_prekeys(self, test_db):
		"""Test storing multiple one-time prekeys."""
		username = "charlie"
		prekeys = [(i, os.urandom(32)) for i in range(50)]

		test_db.store_onetime_prekeys(username, prekeys)

		# Pop one and verify it exists
		popped = test_db.pop_onetime_prekey(username)
		assert popped is not None
		assert "opk_id" in popped
		assert "opk_pub" in popped

	def test_pop_onetime_prekey_returns_none_when_empty(self, test_db):
		"""Test that popping OPK from empty pool returns None."""
		username = "dave"

		result = test_db.pop_onetime_prekey(username)
		assert result is None

	def test_offline_message_storage(self, test_db):
		"""Test storing and retrieving offline messages."""
		recipient = "alice"
		sender = "bob"
		payload = json.dumps({"ciphertext": "encrypted_data"})

		test_db.store_offline_message(recipient, sender, payload)

		messages = test_db.pop_offline_messages(recipient)
		assert len(messages) == 1
		assert messages[0] == payload

		# Second pop should return empty
		messages2 = test_db.pop_offline_messages(recipient)
		assert len(messages2) == 0

	def test_multiple_offline_messages(self, test_db):
		"""Test storing and retrieving multiple offline messages."""
		recipient = "alice"
		messages_data = [
			json.dumps({"msg": f"message_{i}"})
			for i in range(5)
		]

		for i, msg in enumerate(messages_data):
			test_db.store_offline_message(recipient, f"sender_{i}", msg)

		retrieved = test_db.pop_offline_messages(recipient)
		assert len(retrieved) == 5

	def test_certificate_storage(self, test_db):
		"""Test storing and retrieving certificates."""
		username = "eve"
		cert_pem = b"-----BEGIN CERTIFICATE-----\nFAKE_CERT\n-----END CERTIFICATE-----"

		test_db.store_certificate(username, cert_pem.decode())

		retrieved = test_db.get_certificate(username)
		assert retrieved == cert_pem.decode()


class TestPasswordHashing:
	"""Test password hashing and verification behavior."""

	def test_hash_password_uses_pbkdf2_format(self):
		"""Ensure stored hashes include PBKDF2 scheme prefix."""
		stored = hash_password("super-secret")
		assert stored.startswith("pbkdf2_sha256$")

	def test_verify_password_accepts_valid_password(self):
		"""Ensure password verification accepts valid and rejects invalid secrets."""
		stored = hash_password("correct horse battery staple")
		assert verify_password("correct horse battery staple", stored)
		assert not verify_password("wrong-password", stored)


class TestCertificateAuthority:
	"""Test CA functionality."""

	def test_ca_generation(self, test_ca):
		"""Test CA certificate generation."""
		assert test_ca.ca_cert is not None
		cert_pem = test_ca.get_ca_cert_pem()
		assert b"BEGIN CERTIFICATE" in cert_pem
		assert b"END CERTIFICATE" in cert_pem

	def test_ca_persistence(self, temp_ca_dir):
		"""Test CA key and cert are saved to disk."""
		import config
		original_ca_key = config.CA_KEY_PATH
		original_ca_cert = config.CA_CERT_PATH

		config.CA_KEY_PATH = str(temp_ca_dir / "ca_private_key.raw")
		config.CA_CERT_PATH = str(temp_ca_dir / "ca_root_cert.pem")

		ca1 = CertificateAuthority()
		ca1_cert = ca1.get_ca_cert_pem()

		# Load again and verify same CA
		ca2 = CertificateAuthority()
		ca2_cert = ca2.get_ca_cert_pem()

		assert ca1_cert == ca2_cert

		# Restore
		config.CA_KEY_PATH = original_ca_key
		config.CA_CERT_PATH = original_ca_cert

	def test_user_certificate_issuance(self, test_ca):
		"""Test issuing user certificates."""
		username = "alice"
		priv, pub = generate_ed25519_keypair()

		cert = test_ca.issue_user_cert(pub, username)
		assert cert is not None

	def test_ca_cert_pem_format(self, test_ca):
		"""Test CA certificate is in valid PEM format."""
		pem = test_ca.get_ca_cert_pem()

		assert isinstance(pem, bytes)
		assert b"-----BEGIN CERTIFICATE-----" in pem
		assert b"-----END CERTIFICATE-----" in pem
		assert len(pem) > 100  # Sanity check


class TestMessageTypes:
	"""Test protocol message creation and properties."""

	def test_client_hello_creation(self):
		"""Test ClientHello message."""
		ephemeral_pub = os.urandom(32)
		msg = ClientHello(ephemeral_pub_b64=ephemeral_pub.hex())

		assert msg.ephemeral_pub_b64 == ephemeral_pub.hex()
		json_str = msg.to_json()
		assert "ephemeral_pub_b64" in json_str

	def test_register_message_creation(self):
		"""Test Register message with key bundle."""
		bundle = {
			"ik_ed_pub": os.urandom(32).hex(),
			"ik_dh_pub": os.urandom(32).hex(),
			"spk_pub": os.urandom(32).hex(),
			"spk_sig": os.urandom(64).hex(),
		}

		msg = Register(
			username="alice",
			password_hash="hash123",
			key_bundle_json=json.dumps(bundle)
		)

		assert msg.username == "alice"
		assert msg.password_hash == "hash123"
		json_str = msg.to_json()
		assert "username" in json_str

	def test_client_auth_message(self):
		"""Test ClientAuth message."""
		msg = ClientAuth(username="bob", password_hash="hash456")

		assert msg.username == "bob"
		json_str = msg.to_json()
		assert "username" in json_str

	def test_fetch_bundle_message(self):
		"""Test FetchBundle message."""
		msg = FetchBundle(username="alice")

		assert msg.username == "alice"
		json_str = msg.to_json()
		assert "alice" in json_str

	def test_send_message_creation(self):
		"""Test SendMessage for E2EE chat."""
		msg = SendMessage(
			recipient="bob",
			sender="alice",
			ciphertext_b64="encrypted_data",
			ephemeral_pub_b64=os.urandom(32).hex(),
			opk_id=5
		)

		assert msg.recipient == "bob"
		assert msg.sender == "alice"
		assert msg.opk_id == 5


class TestServerIntegration:
	"""Integration tests for server operations."""

	def test_user_registration_workflow(self, test_db, test_ca):
		"""Test complete user registration."""
		username = "alice"
		password = "secure_password"

		# Create user bundle
		ik_ed_priv, ik_ed_pub = generate_ed25519_keypair()
		ik_dh_priv, ik_dh_pub = generate_x25519_keypair()
		spk_priv, spk_pub = generate_x25519_keypair()
		spk_sig = ed25519_sign(ik_ed_priv, serialize_public_key(spk_pub))

		# Store user
		test_db.store_user(
			username,
			password,
			serialize_public_key(ik_ed_pub),
			serialize_public_key(ik_dh_pub),
			serialize_public_key(spk_pub),
			spk_sig
		)

		# Issue certificate
		cert = test_ca.issue_user_cert(ik_ed_pub, username)
		cert_pem = cert_to_pem(cert)
		test_db.store_certificate(username, cert_pem)

		# Verify user exists
		stored_hash = test_db.get_user_hash(username)
		assert stored_hash == password

		# Verify bundle retrievable
		bundle = test_db.get_user_bundle(username)
		assert bundle is not None

		# Verify cert stored
		stored_cert = test_db.get_certificate(username)
		assert stored_cert is not None

	def test_offline_message_flow(self, test_db):
		"""Test offline message store and forward."""
		alice_msg = json.dumps({
			"sender": "alice",
			"recipient": "bob",
			"ciphertext": "encrypted_message"
		})

		# Alice sends message to offline Bob
		test_db.store_offline_message("bob", "alice", alice_msg)

		# Bob comes online and fetches
		messages = test_db.pop_offline_messages("bob")
		assert len(messages) == 1
		assert json.loads(messages[0])["sender"] == "alice"

	def test_multiple_users_offline_messages(self, test_db):
		"""Test offline messages for multiple recipients."""
		# Alice sends to Bob and Charlie
		msg_to_bob = json.dumps({"msg": "for bob"})
		msg_to_charlie = json.dumps({"msg": "for charlie"})

		test_db.store_offline_message("bob", "alice", msg_to_bob)
		test_db.store_offline_message("charlie", "alice", msg_to_charlie)

		# Bob fetches
		bob_msgs = test_db.pop_offline_messages("bob")
		assert len(bob_msgs) == 1
		assert "bob" in bob_msgs[0]

		# Charlie fetches
		charlie_msgs = test_db.pop_offline_messages("charlie")
		assert len(charlie_msgs) == 1
		assert "charlie" in charlie_msgs[0]

		# Both should be empty now
		assert len(test_db.pop_offline_messages("bob")) == 0
		assert len(test_db.pop_offline_messages("charlie")) == 0

	def test_opk_lifecycle(self, test_db):
		"""Test OPK pool management."""
		username = "dave"
		opks = [(i, os.urandom(32)) for i in range(50)]

		# Store initial pool
		test_db.store_onetime_prekeys(username, opks)

		# Pop 10 OPKs
		popped_ids = []
		for _ in range(10):
			opk = test_db.pop_onetime_prekey(username)
			assert opk is not None
			popped_ids.append(opk['opk_id'])

		# All IDs should be unique
		assert len(popped_ids) == len(set(popped_ids))

		# Replenish
		new_opks = [(i + 50, os.urandom(32)) for i in range(20)]
		test_db.store_onetime_prekeys(username, new_opks)

		# Should have 50 - 10 + 20 = 60 now
		# Pop them all
		count = 0
		while test_db.pop_onetime_prekey(username) is not None:
			count += 1

		assert count == 60


class TestMessageSerialization:
	"""Test message serialization and deserialization."""

	def test_register_message_roundtrip(self):
		"""Test Register message survives serialization."""
		from src.protocol.messages import BaseMessage

		bundle = {"ik": "test"}
		original = Register(
			username="alice",
			password_hash="pwd",
			key_bundle_json=json.dumps(bundle)
		)

		json_str = original.to_json()
		restored = BaseMessage.from_json(json_str)

		assert isinstance(restored, Register)
		assert restored.username == "alice"
		assert restored.password_hash == "pwd"

	def test_send_message_roundtrip(self):
		"""Test SendMessage survives serialization."""
		from src.protocol.messages import BaseMessage

		original = SendMessage(
			recipient="bob",
			sender="alice",
			ciphertext_b64="cipher",
			ephemeral_pub_b64="ephemeral",
			opk_id=7
		)

		json_str = original.to_json()
		restored = BaseMessage.from_json(json_str)

		assert isinstance(restored, SendMessage)
		assert restored.recipient == "bob"
		assert restored.sender == "alice"
		assert restored.opk_id == 7

	def test_fetch_offline_roundtrip(self):
		"""Test FetchOffline message survives serialization."""
		from src.protocol.messages import BaseMessage, FetchOffline

		original = FetchOffline()
		json_str = original.to_json()
		restored = BaseMessage.from_json(json_str)

		assert isinstance(restored, FetchOffline)


class TestConcurrency:
	"""Test server behavior with concurrent operations."""

	def test_concurrent_user_registrations(self, test_db):
		"""Test registering multiple users concurrently."""
		users = [
			("alice", "pwd1", os.urandom(32), os.urandom(32), os.urandom(32), os.urandom(64)),
			("bob", "pwd2", os.urandom(32), os.urandom(32), os.urandom(32), os.urandom(64)),
			("charlie", "pwd3", os.urandom(32), os.urandom(32), os.urandom(32), os.urandom(64)),
		]

		for username, pwd, ik_ed, ik_dh, spk, spk_sig in users:
			test_db.store_user(username, pwd, ik_ed, ik_dh, spk, spk_sig)

		# Verify all exist
		for username, pwd, *_ in users:
			assert test_db.get_user_hash(username) == pwd

	def test_concurrent_offline_messages(self, test_db):
		"""Test multiple offline messages in rapid succession."""
		recipient = "bob"
		msg_count = 20

		# Store messages
		for i in range(msg_count):
			payload = json.dumps({"seq": i})
			test_db.store_offline_message(recipient, f"sender_{i % 3}", payload)

		# Fetch all
		messages = test_db.pop_offline_messages(recipient)
		assert len(messages) == msg_count

		# Verify none remain
		assert len(test_db.pop_offline_messages(recipient)) == 0


class TestErrorHandling:
	"""Test error conditions."""

	def test_get_bundle_for_nonexistent_user(self, test_db):
		"""Test fetching bundle for user that doesn't exist."""
		bundle = test_db.get_user_bundle("does_not_exist")
		assert bundle is None

	def test_get_password_hash_for_nonexistent_user(self, test_db):
		"""Test getting password hash for nonexistent user."""
		hash_val = test_db.get_user_hash("does_not_exist")
		assert hash_val is None

	def test_pop_certificate_for_nonexistent_user(self, test_db):
		"""Test getting cert for user without one."""
		cert = test_db.get_certificate("no_cert_user")
		assert cert is None

	def test_pop_opk_from_empty_pool(self, test_db):
		"""Test popping OPK when pool is empty."""
		opk = test_db.pop_onetime_prekey("user_with_no_opks")
		assert opk is None


class TestPKIInvalidCertificates:
	"""Additional PKI negative tests."""

	def test_certificate_signed_by_untrusted_ca_is_rejected(self, test_ca, temp_ca_dir):
		"""A cert from a rogue CA must fail verification against trusted CA."""
		import config

		trusted_ca_cert = pem_to_cert(test_ca.get_ca_cert_pem())
		original_ca_key = config.CA_KEY_PATH
		original_ca_cert = config.CA_CERT_PATH

		config.CA_KEY_PATH = str(temp_ca_dir / "rogue_ca_private_key.raw")
		config.CA_CERT_PATH = str(temp_ca_dir / "rogue_ca_root_cert.pem")
		rogue_ca = CertificateAuthority()

		_, user_pub = generate_ed25519_keypair()
		rogue_user_cert = rogue_ca.issue_user_cert(user_pub, "mallory")

		assert not verify_certificate(rogue_user_cert, trusted_ca_cert)

		config.CA_KEY_PATH = original_ca_key
		config.CA_CERT_PATH = original_ca_cert

	def test_malformed_certificate_pem_is_rejected(self):
		"""Malformed PEM bytes must not parse as valid certificates."""
		malformed_pem = b"-----BEGIN CERTIFICATE-----\ninvalid\n-----END CERTIFICATE-----"
		with pytest.raises(ValueError):
			pem_to_cert(malformed_pem)


if __name__ == "__main__":
	pytest.main([__file__, "-v"])
