"""
Unit tests for the client module.

Tests:
- KeyManager
- SessionManager
- ContactManager
- GroupManager
"""
from __future__ import annotations

import asyncio
import base64
import json
import os
import shutil
import sqlite3
import tempfile

import pytest

from src.client.key_manager import KeyManager
from src.client.client import ChatClient
from src.client.cli import ChatCLI
from src.client.app_service import ClientAppService
from src.client.session_manager import SessionManager
from src.client.contact_manager import ContactManager
from src.client.group_manager import GroupManager
from src.storage.client_db import ClientDB
from src.protocol.messages import BundleResponse, DeliverMessage, OfflineMessages


class TestKeyManager:
    """Tests for KeyManager."""
    
    @pytest.fixture
    def temp_dir(self):
        """Create a temporary directory."""
        tmpdir = tempfile.mkdtemp()
        yield tmpdir
        shutil.rmtree(tmpdir)
    
    def test_generate_keys(self, temp_dir):
        """Test key generation."""
        km = KeyManager("alice", data_dir=temp_dir)
        
        # Should have all key types
        keys = km.keys
        assert keys.ik_ed_priv is not None
        assert keys.ik_ed_pub is not None
        assert keys.ik_dh_priv is not None
        assert keys.ik_dh_pub is not None
        assert keys.spk_priv is not None
        assert keys.spk_pub is not None
        assert keys.spk_sig is not None
        assert len(keys.opks) == 10  # Default count
    
    def test_persist_and_reload(self, temp_dir):
        """Test key persistence."""
        km1 = KeyManager("alice", data_dir=temp_dir)
        bundle1 = km1.get_registration_bundle()
        
        # Create new instance, should load same keys
        km2 = KeyManager("alice", data_dir=temp_dir)
        bundle2 = km2.get_registration_bundle()
        
        assert bundle1 == bundle2
    
    def test_registration_bundle(self, temp_dir):
        """Test registration bundle format."""
        km = KeyManager("alice", data_dir=temp_dir)
        bundle = km.get_registration_bundle()
        
        # Should have required fields
        assert "ik_ed_pub" in bundle
        assert "ik_dh_pub" in bundle
        assert "spk_pub" in bundle
        assert "spk_sig" in bundle
        
        # Should be base64 encoded
        assert isinstance(bundle["ik_ed_pub"], str)
        base64.b64decode(bundle["ik_ed_pub"])  # Should not raise
    
    def test_opk_lookup_and_marking(self, temp_dir):
        """Test OPK lookup and usage marking."""
        km = KeyManager("alice", data_dir=temp_dir)
        
        # Should find OPK 0
        opk_priv = km.get_opk_private(0)
        assert opk_priv is not None
        
        # Mark as used
        km.mark_opk_used(0)
        
        # Should no longer be in list
        assert all(opk_id != 0 for opk_id, _, _ in km.keys.opks)
    
    def test_generate_new_opks(self, temp_dir):
        """Test generating additional OPKs."""
        km = KeyManager("alice", data_dir=temp_dir)
        initial_count = len(km.keys.opks)
        
        new_opks = km.generate_new_opks(count=5)
        
        assert len(new_opks) == 5
        assert len(km.keys.opks) == initial_count + 5


class TestSessionManager:
    """Tests for SessionManager."""
    
    @pytest.fixture
    def setup(self):
        """Set up two users with key managers and databases."""
        tmpdir = tempfile.mkdtemp()
        
        km_alice = KeyManager("alice", data_dir=tmpdir)
        km_bob = KeyManager("bob", data_dir=tmpdir)
        
        db_alice = ClientDB(os.path.join(tmpdir, "alice.db"), b"x" * 32)
        db_bob = ClientDB(os.path.join(tmpdir, "bob.db"), b"x" * 32)
        
        sm_alice = SessionManager(km_alice, db_alice)
        sm_bob = SessionManager(km_bob, db_bob)
        
        yield {
            "tmpdir": tmpdir,
            "km_alice": km_alice,
            "km_bob": km_bob,
            "sm_alice": sm_alice,
            "sm_bob": sm_bob,
        }
        
        shutil.rmtree(tmpdir)
    
    def test_session_establishment(self, setup):
        """Test X3DH session establishment."""
        sm_alice = setup["sm_alice"]
        sm_bob = setup["sm_bob"]
        km_alice = setup["km_alice"]
        km_bob = setup["km_bob"]
        
        # Alice initiates with Bob's bundle
        bob_bundle = {
            "ik_ed_pub": km_bob.get_registration_bundle()["ik_ed_pub"],
            "ik_dh_pub": km_bob.get_registration_bundle()["ik_dh_pub"],
            "spk_pub": km_bob.get_registration_bundle()["spk_pub"],
            "spk_sig": km_bob.get_registration_bundle()["spk_sig"],
            "opk_pub": km_bob.get_onetime_prekeys_for_upload()[0][1],
            "opk_id": km_bob.get_onetime_prekeys_for_upload()[0][0],
        }
        
        eph_pub, _, opk_id = sm_alice.initiate_session("bob", bob_bundle)
        
        assert sm_alice.has_session("bob")
        assert not sm_bob.has_session("alice")
        
        # Bob receives initial message
        sm_bob.receive_initial_message(
            peer="alice",
            peer_ik_ed_pub=base64.b64decode(km_alice.get_registration_bundle()["ik_ed_pub"]),
            peer_ik_dh_pub=base64.b64decode(km_alice.get_registration_bundle()["ik_dh_pub"]),
            ephemeral_pub=eph_pub,
            opk_id=opk_id,
        )
        
        assert sm_bob.has_session("alice")
    
    def test_encrypt_decrypt(self, setup):
        """Test message encryption and decryption."""
        sm_alice = setup["sm_alice"]
        sm_bob = setup["sm_bob"]
        km_alice = setup["km_alice"]
        km_bob = setup["km_bob"]
        
        # Establish session
        bob_bundle = {
            "ik_ed_pub": km_bob.get_registration_bundle()["ik_ed_pub"],
            "ik_dh_pub": km_bob.get_registration_bundle()["ik_dh_pub"],
            "spk_pub": km_bob.get_registration_bundle()["spk_pub"],
            "spk_sig": km_bob.get_registration_bundle()["spk_sig"],
            "opk_pub": km_bob.get_onetime_prekeys_for_upload()[0][1],
            "opk_id": km_bob.get_onetime_prekeys_for_upload()[0][0],
        }
        
        eph_pub, _, opk_id = sm_alice.initiate_session("bob", bob_bundle)
        sm_bob.receive_initial_message(
            peer="alice",
            peer_ik_ed_pub=base64.b64decode(km_alice.get_registration_bundle()["ik_ed_pub"]),
            peer_ik_dh_pub=base64.b64decode(km_alice.get_registration_bundle()["ik_dh_pub"]),
            ephemeral_pub=eph_pub,
            opk_id=opk_id,
        )
        
        # Alice sends message
        encrypted = sm_alice.encrypt_message("bob", "Hello Bob!")
        assert "header" in encrypted
        assert "nonce" in encrypted
        assert "ciphertext" in encrypted
        
        # Bob decrypts
        plaintext = sm_bob.decrypt_message("alice", encrypted)
        assert plaintext == "Hello Bob!"
    
    def test_bidirectional_messaging(self, setup):
        """Test messages in both directions."""
        sm_alice = setup["sm_alice"]
        sm_bob = setup["sm_bob"]
        km_alice = setup["km_alice"]
        km_bob = setup["km_bob"]
        
        # Establish session
        bob_bundle = {
            "ik_ed_pub": km_bob.get_registration_bundle()["ik_ed_pub"],
            "ik_dh_pub": km_bob.get_registration_bundle()["ik_dh_pub"],
            "spk_pub": km_bob.get_registration_bundle()["spk_pub"],
            "spk_sig": km_bob.get_registration_bundle()["spk_sig"],
            "opk_pub": km_bob.get_onetime_prekeys_for_upload()[0][1],
            "opk_id": km_bob.get_onetime_prekeys_for_upload()[0][0],
        }
        
        eph_pub, _, opk_id = sm_alice.initiate_session("bob", bob_bundle)
        sm_bob.receive_initial_message(
            peer="alice",
            peer_ik_ed_pub=base64.b64decode(km_alice.get_registration_bundle()["ik_ed_pub"]),
            peer_ik_dh_pub=base64.b64decode(km_alice.get_registration_bundle()["ik_dh_pub"]),
            ephemeral_pub=eph_pub,
            opk_id=opk_id,
        )
        
        # Multiple messages in both directions
        for i in range(5):
            enc1 = sm_alice.encrypt_message("bob", f"Alice msg {i}")
            assert sm_bob.decrypt_message("alice", enc1) == f"Alice msg {i}"
            
            enc2 = sm_bob.encrypt_message("alice", f"Bob msg {i}")
            assert sm_alice.decrypt_message("bob", enc2) == f"Bob msg {i}"


class TestContactManager:
    """Tests for ContactManager."""
    
    @pytest.fixture
    def setup(self):
        """Set up database and contact manager."""
        tmpfile = tempfile.mktemp(suffix=".db")
        db = ClientDB(tmpfile, b"x" * 32)
        cm = ContactManager(db)
        
        yield {"tmpfile": tmpfile, "db": db, "cm": cm}
        
        os.unlink(tmpfile)
    
    def test_add_and_get_contact(self, setup):
        """Test adding and retrieving contacts."""
        cm = setup["cm"]
        
        bundle = {
            "ik_ed_pub": base64.b64encode(b"a" * 32).decode(),
            "ik_dh_pub": base64.b64encode(b"b" * 32).decode(),
            "spk_pub": base64.b64encode(b"c" * 32).decode(),
            "spk_sig": base64.b64encode(b"d" * 64).decode(),
            "p2p_host": "127.0.0.1",
            "p2p_port": 7777,
        }
        
        cm.add_contact("bob", bundle, verify=False)
        
        assert cm.has_contact("bob")
        
        contact = cm.get_contact("bob")
        assert contact is not None
        assert contact["username"] == "bob"
        assert contact["spk_sig"] == bundle["spk_sig"]
        assert contact["p2p_host"] == "127.0.0.1"
        assert contact["p2p_port"] == 7777
    
    def test_list_contacts(self, setup):
        """Test listing contacts."""
        cm = setup["cm"]
        
        bundle = {
            "ik_ed_pub": base64.b64encode(b"a" * 32).decode(),
            "ik_dh_pub": base64.b64encode(b"b" * 32).decode(),
            "spk_pub": base64.b64encode(b"c" * 32).decode(),
        }
        
        cm.add_contact("bob", bundle, verify=False)
        cm.add_contact("charlie", bundle, verify=False)
        
        contacts = cm.list_contacts()
        assert len(contacts) == 2
        assert "bob" in contacts
        assert "charlie" in contacts

    def test_existing_contacts_table_is_migrated_with_spk_sig(self):
        """Test migration adds spk_sig to pre-existing contacts table."""
        tmpdir = tempfile.mkdtemp()
        db_path = os.path.join(tmpdir, "legacy.db")
        try:
            conn = sqlite3.connect(db_path)
            conn.execute(
                """CREATE TABLE contacts (
                    username TEXT PRIMARY KEY,
                    ik_ed_pub BLOB NOT NULL,
                    ik_dh_pub BLOB NOT NULL,
                    spk_pub BLOB NOT NULL,
                    p2p_host TEXT NOT NULL DEFAULT '',
                    p2p_port INTEGER NOT NULL DEFAULT 0,
                    cert_pem TEXT,
                    added_at REAL NOT NULL
                )"""
            )
            conn.commit()
            conn.close()

            db = ClientDB(db_path, b"x" * 32)
            cursor = db.conn.cursor()
            cursor.execute("PRAGMA table_info(contacts)")
            columns = {row["name"] for row in cursor.fetchall()}
            assert "spk_sig" in columns
        finally:
            shutil.rmtree(tmpdir)

    def test_message_dedup_tracking(self, setup):
        """Test dedup table storage and lookup."""
        db = setup["db"]
        assert db.has_seen_message("m1") is False
        db.mark_message_seen("m1", seen_at=200.0)
        assert db.has_seen_message("m1") is True


class TestGroupManager:
    """Tests for GroupManager."""
    
    @pytest.fixture
    def setup(self):
        """Set up databases and group managers for two users."""
        tmpdir = tempfile.mkdtemp()
        
        db_alice = ClientDB(os.path.join(tmpdir, "alice.db"), b"x" * 32)
        db_bob = ClientDB(os.path.join(tmpdir, "bob.db"), b"x" * 32)
        
        gm_alice = GroupManager(db_alice, "alice")
        gm_bob = GroupManager(db_bob, "bob")
        
        yield {
            "tmpdir": tmpdir,
            "gm_alice": gm_alice,
            "gm_bob": gm_bob,
        }
        
        shutil.rmtree(tmpdir)
    
    def test_create_group(self, setup):
        """Test group creation."""
        gm_alice = setup["gm_alice"]
        
        group_id = gm_alice.create_group("Test Group")
        
        assert group_id is not None
        assert len(group_id) == 8  # UUID prefix
        
        groups = gm_alice.list_groups()
        assert len(groups) == 1
        assert groups[0]["name"] == "Test Group"
    
    def test_sender_key_exchange(self, setup):
        """Test sender key distribution."""
        gm_alice = setup["gm_alice"]
        gm_bob = setup["gm_bob"]
        
        # Alice creates group
        group_id = gm_alice.create_group("Test Group")
        
        # Bob joins
        bob_sender_key = gm_bob.join_group(group_id, "Test Group", "alice")
        
        # Exchange sender keys
        alice_sender_key = gm_alice.get_my_sender_key(group_id)
        
        gm_alice.add_member(group_id, "bob", bob_sender_key)
        gm_bob.receive_sender_key(group_id, "alice", alice_sender_key)
        
        # Both should have each other's keys
        assert len(gm_alice.get_group_members(group_id)) == 2
        assert len(gm_bob.get_group_members(group_id)) == 2
    
    def test_group_encryption_decryption(self, setup):
        """Test group message encryption and decryption."""
        gm_alice = setup["gm_alice"]
        gm_bob = setup["gm_bob"]
        
        # Set up group
        group_id = gm_alice.create_group("Test Group")
        bob_sender_key = gm_bob.join_group(group_id, "Test Group", "alice")
        alice_sender_key = gm_alice.get_my_sender_key(group_id)
        gm_alice.add_member(group_id, "bob", bob_sender_key)
        gm_bob.receive_sender_key(group_id, "alice", alice_sender_key)
        
        # Alice sends message
        encrypted = gm_alice.encrypt_message(group_id, "Hello group!")
        
        assert encrypted["group_id"] == group_id
        assert encrypted["sender"] == "alice"
        
        # Bob decrypts
        plaintext = gm_bob.decrypt_message(encrypted)
        assert plaintext == "Hello group!"
    
    def test_forward_secrecy(self, setup):
        """Test that chain keys advance (forward secrecy)."""
        gm_alice = setup["gm_alice"]
        gm_bob = setup["gm_bob"]
        
        # Set up group
        group_id = gm_alice.create_group("Test Group")
        bob_sender_key = gm_bob.join_group(group_id, "Test Group", "alice")
        alice_sender_key = gm_alice.get_my_sender_key(group_id)
        gm_alice.add_member(group_id, "bob", bob_sender_key)
        gm_bob.receive_sender_key(group_id, "alice", alice_sender_key)
        
        # Send multiple messages
        for i in range(5):
            enc = gm_alice.encrypt_message(group_id, f"Message {i}")
            assert enc["message_index"] == i
            dec = gm_bob.decrypt_message(enc)
            assert dec == f"Message {i}"

    def test_member_removal_forces_sender_key_rotation(self, setup):
        """Removed members must not decrypt messages after rotation."""
        gm_alice = setup["gm_alice"]
        gm_bob = setup["gm_bob"]

        group_id = gm_alice.create_group("Test Group")
        bob_sender_key = gm_bob.join_group(group_id, "Test Group", "alice")
        alice_sender_key = gm_alice.get_my_sender_key(group_id)
        gm_alice.add_member(group_id, "bob", bob_sender_key)
        gm_bob.receive_sender_key(group_id, "alice", alice_sender_key)

        pre_rotation = gm_alice.encrypt_message(group_id, "before")
        assert gm_bob.decrypt_message(pre_rotation) == "before"

        old_sender_key = gm_alice.get_my_sender_key(group_id)
        assert gm_alice.remove_member(group_id, "bob") is True
        rotated_sender_key = gm_alice.rotate_own_sender_key(group_id)
        assert rotated_sender_key != old_sender_key
        assert "bob" not in gm_alice.get_group_members(group_id)

        post_rotation = gm_alice.encrypt_message(group_id, "after")
        with pytest.raises(Exception):
            gm_bob.decrypt_message(post_rotation)


class TestClientAppService:
    """Tests for shared orchestration in ClientAppService."""

    def test_build_cached_bundle_for_session_requires_complete_bundle(self):
        """Ensure missing required cached bundle fields produce guidance."""
        tmpdir = tempfile.mkdtemp()
        try:
            db = ClientDB(os.path.join(tmpdir, "client.db"), b"x" * 32)
            contact_manager = ContactManager(db)
            contact_manager.add_contact(
                "bob",
                {
                    "ik_ed_pub": base64.b64encode(b"a" * 32).decode(),
                    "ik_dh_pub": base64.b64encode(b"b" * 32).decode(),
                    "spk_pub": base64.b64encode(b"c" * 32).decode(),
                    "p2p_host": "127.0.0.1",
                    "p2p_port": 4444,
                },
                verify=False,
            )

            service = ClientAppService(
                client=ChatClient(),
                session_manager=object(),  # type: ignore[arg-type]
                contact_manager=contact_manager,
                preferred_p2p_port=lambda _user: 32123,
            )

            bundle, error = service.build_cached_bundle_for_session("bob")

            assert bundle is None
            assert "missing required fields (spk_sig)" in error
        finally:
            shutil.rmtree(tmpdir)

    @pytest.mark.asyncio
    async def test_prepare_outbound_payload_refreshes_endpoint_for_existing_session(self):
        """Ensure existing session flow refreshes endpoint metadata before send."""
        tmpdir = tempfile.mkdtemp()
        try:
            db = ClientDB(os.path.join(tmpdir, "client.db"), b"x" * 32)
            contact_manager = ContactManager(db)
            contact_manager.add_contact(
                "bob",
                {
                    "ik_ed_pub": base64.b64encode(b"a" * 32).decode(),
                    "ik_dh_pub": base64.b64encode(b"b" * 32).decode(),
                    "spk_pub": base64.b64encode(b"c" * 32).decode(),
                    "p2p_host": "127.0.0.1",
                    "p2p_port": 1111,
                },
                verify=False,
            )

            class DummyWriter:
                def is_closing(self) -> bool:
                    return False

            class DummySessionManager:
                def has_session(self, username: str) -> bool:
                    return username == "bob"

                def encrypt_message(self, username: str, text: str) -> dict:
                    return {"ciphertext": f"{username}:{text}"}

            client = ChatClient()
            client.writer = DummyWriter()  # type: ignore[assignment]
            client.username = "alice"

            async def fake_fetch_bundle(username: str):
                assert username == "bob"
                return {
                    "ik_ed_pub": base64.b64encode(b"a" * 32).decode(),
                    "ik_dh_pub": base64.b64encode(b"b" * 32).decode(),
                    "spk_pub": base64.b64encode(b"c" * 32).decode(),
                    "p2p_host": "127.0.0.1",
                    "p2p_port": 3333,
                }

            client.fetch_bundle = fake_fetch_bundle  # type: ignore[method-assign]

            service = ClientAppService(
                client=client,
                session_manager=DummySessionManager(),  # type: ignore[arg-type]
                contact_manager=contact_manager,
                preferred_p2p_port=lambda _user: 32123,
            )

            payload = await service.prepare_outbound_payload("bob", "hello")

            contact = contact_manager.get_contact("bob")
            assert contact is not None
            assert contact["p2p_port"] == 3333
            assert payload.context.peer_host == "127.0.0.1"
            assert payload.context.peer_port == 3333
            assert payload.context.ephemeral_pub_b64 == ""
        finally:
            shutil.rmtree(tmpdir)

    @pytest.mark.asyncio
    async def test_prepare_outbound_payload_offline_without_cache_raises(self):
        """Ensure offline first-message send fails fast without cached contact bundle."""
        tmpdir = tempfile.mkdtemp()
        try:
            db = ClientDB(os.path.join(tmpdir, "client.db"), b"x" * 32)
            contact_manager = ContactManager(db)

            class DummySessionManager:
                def has_session(self, username: str) -> bool:
                    return False

            service = ClientAppService(
                client=ChatClient(),
                session_manager=DummySessionManager(),  # type: ignore[arg-type]
                contact_manager=contact_manager,
                preferred_p2p_port=lambda _user: 32123,
            )

            with pytest.raises(ValueError, match="No cached contact bundle"):
                await service.prepare_outbound_payload("bob", "hello")
        finally:
            shutil.rmtree(tmpdir)


class TestCLI:
    """Tests for ChatCLI command parsing, UX flow, and delivery behavior."""

    def test_derive_client_db_key_is_stable_for_same_user_and_password(self):
        """Ensure DB key derivation is stable per user/password and salt."""
        tmpdir = tempfile.mkdtemp()
        try:
            cli = ChatCLI(data_dir=tmpdir)
            key1 = cli._derive_client_db_key("alice", "password123")
            key2 = cli._derive_client_db_key("alice", "password123")
            key3 = cli._derive_client_db_key("alice", "different-password")

            assert key1 == key2
            assert key1 != key3
            assert len(key1) == 32
        finally:
            shutil.rmtree(tmpdir)

    @pytest.mark.asyncio
    async def test_chatclient_fetch_bundle_dispatches_notifications(self, monkeypatch):
        """Ensure fetch_bundle dispatches out-of-band offline notifications."""
        client = ChatClient()

        class DummyWriter:
            def is_closing(self) -> bool:
                return False

        client.reader = object()  # type: ignore[assignment]
        client.writer = DummyWriter()  # type: ignore[assignment]
        client.transport_key_send = b"a" * 32
        client.transport_key_recv = b"b" * 32
        client.username = "alice"

        seen: list[str] = []

        async def handler(msg):
            seen.append(type(msg).__name__)
            return True

        client.set_message_handler(handler)
        responses = iter(
            [
                OfflineMessages(messages=["queued"]),
                BundleResponse(username="bob", bundle_json='{"ik_ed_pub":"x"}'),
            ]
        )

        async def fake_send_message(*args, **kwargs):
            return None

        async def fake_recv_message(*args, **kwargs):
            return next(responses)

        monkeypatch.setattr("src.client.client.send_message", fake_send_message)
        monkeypatch.setattr("src.client.client.recv_message", fake_recv_message)

        bundle = await client.fetch_bundle("bob")
        assert bundle == {"ik_ed_pub": "x"}
        assert seen == ["OfflineMessages"]

    @pytest.mark.asyncio
    async def test_chatclient_fetch_offline_keeps_live_notifications(self, monkeypatch):
        """Ensure fetch_offline preserves live notification dispatch behavior."""
        client = ChatClient()

        class DummyWriter:
            def is_closing(self) -> bool:
                return False

        client.reader = object()  # type: ignore[assignment]
        client.writer = DummyWriter()  # type: ignore[assignment]
        client.transport_key_send = b"a" * 32
        client.transport_key_recv = b"b" * 32
        client.username = "alice"

        seen: list[str] = []

        async def handler(msg):
            seen.append(type(msg).__name__)
            return True

        client.set_message_handler(handler)
        responses = iter(
            [
                DeliverMessage(payload_json=json.dumps({"msg_id": "live-1"})),
                OfflineMessages(messages=["offline-1"]),
            ]
        )

        async def fake_send_message(*args, **kwargs):
            return None

        async def fake_recv_message(*args, **kwargs):
            return next(responses)

        monkeypatch.setattr("src.client.client.send_message", fake_send_message)
        monkeypatch.setattr("src.client.client.recv_message", fake_recv_message)

        messages = await client.fetch_offline_messages()
        assert messages == ["offline-1"]
        assert seen == ["DeliverMessage"]

    @pytest.mark.asyncio
    async def test_chatclient_receive_loop_continues_after_handler_error(self, monkeypatch):
        """Ensure receive loop survives exceptions raised by message handlers."""
        client = ChatClient()

        class DummyWriter:
            def is_closing(self) -> bool:
                return False

        client.reader = object()  # type: ignore[assignment]
        client.writer = DummyWriter()  # type: ignore[assignment]
        client.transport_key_recv = b"b" * 32
        client._running = True

        async def failing_handler(msg):
            raise RuntimeError("ui error")

        client.set_message_handler(failing_handler)
        calls = {"count": 0}

        async def fake_recv_message(*args, **kwargs):
            calls["count"] += 1
            if calls["count"] == 1:
                return DeliverMessage(payload_json=json.dumps({"msg_id": "live-1"}))
            raise asyncio.IncompleteReadError(partial=b"", expected=1)

        monkeypatch.setattr("src.client.client.recv_message", fake_recv_message)

        await client._receive_loop()
        assert calls["count"] == 2

    @pytest.mark.asyncio
    async def test_process_delivered_message_rejects_non_object_payload(self):
        """Ensure non-object JSON payloads are rejected during delivery parsing."""
        cli = ChatCLI()
        cli.client.username = "bob"
        ok = await cli._process_delivered_message(DeliverMessage(payload_json="1234"))
        assert ok is False

    @pytest.mark.asyncio
    async def test_cmd_offline_handles_malformed_payload_without_int_get_error(self):
        """Ensure malformed offline payloads do not trigger attribute errors."""
        cli = ChatCLI()
        cli.client.username = "alice"  # marks authenticated=True

        async def fake_fetch_offline_messages():
            return ["1234"]

        cli.client.fetch_offline_messages = fake_fetch_offline_messages  # type: ignore[method-assign]
        errors: list[str] = []
        cli._print_error = lambda msg: errors.append(msg)  # type: ignore[method-assign]

        await cli._cmd_offline()
        assert all("'int' object has no attribute 'get'" not in err for err in errors)

    def test_group_message_type_checks_handle_non_object_json(self):
        """Ensure group message type helpers reject non-object JSON values."""
        cli = ChatCLI()
        assert cli._is_group_invite("1234") is False
        assert cli._is_group_sender_key("1234") is False
        assert cli._is_group_msg("1234") is False

    def test_build_completion_candidates_for_root_commands(self):
        """Ensure root slash-command completion returns expected matches."""
        cli = ChatCLI()
        assert cli._build_completion_candidates("/he", "/he") == ["/help "]

    def test_build_completion_candidates_for_group_subcommands(self):
        """Ensure /group subcommand completion returns filtered matches."""
        cli = ChatCLI()
        assert cli._build_completion_candidates("/group m", "m") == ["members ", "msg "]

    def test_build_completion_candidates_ignores_non_commands(self):
        """Ensure completion returns empty for non-command input buffers."""
        cli = ChatCLI()
        assert cli._build_completion_candidates("hello world", "world") == []

    def test_preferred_p2p_port_is_stable_per_user(self, monkeypatch):
        """Ensure computed fallback P2P port is stable per username."""
        cli = ChatCLI()
        monkeypatch.setattr("src.client.cli.config.CLIENT_P2P_PORT", 0)

        alice_first = cli._preferred_p2p_port("alice")
        alice_second = cli._preferred_p2p_port("alice")
        bob_port = cli._preferred_p2p_port("bob")

        assert alice_first == alice_second
        assert 30000 <= alice_first < 45000
        assert alice_first != bob_port

    def test_preferred_p2p_port_honors_config_override(self, monkeypatch):
        """Ensure explicit CLIENT_P2P_PORT overrides hashed fallback port."""
        cli = ChatCLI()
        monkeypatch.setattr("src.client.cli.config.CLIENT_P2P_PORT", 41234)
        assert cli._preferred_p2p_port("alice") == 41234

    @pytest.mark.asyncio
    async def test_ensure_user_direct_listener_falls_back_when_preferred_port_unavailable(self, monkeypatch):
        """Ensure listener setup falls back when preferred endpoint bind fails."""
        cli = ChatCLI()
        monkeypatch.setattr("src.client.cli.config.CLIENT_P2P_PORT", 0)

        calls: list[tuple[str | None, int | None, bool]] = []

        async def fake_ensure_direct_listener(
            host: str | None = None,
            port: int | None = None,
            force_rebind: bool = False,
        ):
            calls.append((host, port, force_rebind))
            if force_rebind:
                raise OSError("port already in use")
            return "127.0.0.1", 0

        cli.client.ensure_direct_listener = fake_ensure_direct_listener  # type: ignore[method-assign]

        await cli._ensure_user_direct_listener("alice")

        assert len(calls) == 2
        assert calls[0][2] is True
        assert calls[1] == (None, None, False)

    def test_command_output_title_for_root_command(self):
        """Ensure output section title reflects root command name."""
        cli = ChatCLI()
        assert cli._command_output_title("/connect 127.0.0.1") == "Output · /connect"

    def test_command_output_title_for_group_subcommand(self):
        """Ensure output title includes /group subcommand context."""
        cli = ChatCLI()
        assert cli._command_output_title("/group invite group1 bob") == "Output · /group invite"

    def test_command_output_title_fallback_for_non_command_input(self):
        """Ensure output title falls back for non-command text input."""
        cli = ChatCLI()
        assert cli._command_output_title("hello world") == "Output"

    def test_command_completer_uses_line_buffer(self, monkeypatch):
        """Ensure completer reads line buffer and iterates candidates by state."""
        cli = ChatCLI()
        monkeypatch.setattr("src.client.cli.readline.get_line_buffer", lambda: "/group m")
        assert cli._command_completer("m", 0) == "members "
        assert cli._command_completer("m", 1) == "msg "
        assert cli._command_completer("m", 2) is None

    def test_configure_readline_completion_skips_non_interactive(self, monkeypatch):
        """Ensure readline completion setup is skipped in non-interactive mode."""
        cli = ChatCLI()
        cli._is_interactive_terminal = lambda: False  # type: ignore[method-assign]
        parse_calls: list[str] = []
        monkeypatch.setattr("src.client.cli.readline.parse_and_bind", lambda spec: parse_calls.append(spec))
        monkeypatch.setattr("src.client.cli.readline.set_completer", lambda completer: parse_calls.append("set"))

        cli._configure_readline_completion()

        assert parse_calls == []
        assert cli._readline_completion_enabled is False

    @pytest.mark.asyncio
    async def test_run_configures_and_restores_readline_completion(self, monkeypatch):
        """Ensure run() configures and restores readline completion lifecycle."""
        cli = ChatCLI()
        lifecycle: list[str] = []
        cli._print_banner = lambda: None  # type: ignore[method-assign]
        cli._configure_readline_completion = lambda: lifecycle.append("configure")  # type: ignore[method-assign]
        cli._restore_readline_completion = lambda: lifecycle.append("restore")  # type: ignore[method-assign]
        monkeypatch.setattr("builtins.input", lambda _prompt: (_ for _ in ()).throw(EOFError()))
        monkeypatch.setattr(cli.console, "print", lambda *args, **kwargs: None)

        await cli.run()

        assert lifecycle == ["configure", "restore"]

    @pytest.mark.asyncio
    async def test_process_command_rejects_non_command_input(self):
        """Ensure plain text input is rejected outside slash-command format."""
        cli = ChatCLI()
        errors: list[str] = []
        cli._print_error = lambda msg: errors.append(msg)  # type: ignore[method-assign]

        await cli._process_command("hello world")

        assert errors == ["Commands start with /. Use /help for list"]

    @pytest.mark.asyncio
    async def test_process_command_reports_unknown_command(self):
        """Ensure unknown slash commands return an actionable error message."""
        cli = ChatCLI()
        errors: list[str] = []
        cli._print_error = lambda msg: errors.append(msg)  # type: ignore[method-assign]

        await cli._process_command("/doesnotexist")

        assert errors == ["Unknown command: /doesnotexist. Use /help"]

    @pytest.mark.asyncio
    async def test_process_command_group_msg_without_text_reports_usage(self):
        """Ensure missing group message text triggers usage hint."""
        cli = ChatCLI()
        cli.client.username = "alice"  # marks authenticated=True
        errors: list[str] = []
        cli._print_error = lambda msg: errors.append(msg)  # type: ignore[method-assign]

        await cli._process_command("/group msg group1")

        assert errors == ["Usage: /group msg <group_id> <message>"]

    @pytest.mark.asyncio
    async def test_cmd_add_refreshes_existing_contact_endpoint(self):
        """Ensure /add refreshes stored endpoint data for existing contacts."""
        tmpdir = tempfile.mkdtemp()
        try:
            cli = ChatCLI(data_dir=tmpdir)
            cli.client.username = "alice"
            cli.db = ClientDB(os.path.join(tmpdir, "client.db"), b"x" * 32)
            cli.contact_manager = ContactManager(cli.db)

            stale_bundle = {
                "ik_ed_pub": base64.b64encode(b"a" * 32).decode(),
                "ik_dh_pub": base64.b64encode(b"b" * 32).decode(),
                "spk_pub": base64.b64encode(b"c" * 32).decode(),
                "p2p_host": "127.0.0.1",
                "p2p_port": 1111,
            }
            refreshed_bundle = {
                "ik_ed_pub": base64.b64encode(b"a" * 32).decode(),
                "ik_dh_pub": base64.b64encode(b"b" * 32).decode(),
                "spk_pub": base64.b64encode(b"c" * 32).decode(),
                "p2p_host": "127.0.0.1",
                "p2p_port": 2222,
            }
            cli.contact_manager.add_contact("bob", stale_bundle, verify=False)

            async def fake_fetch_bundle(username: str):
                assert username == "bob"
                return refreshed_bundle

            cli.client.fetch_bundle = fake_fetch_bundle  # type: ignore[method-assign]
            infos: list[str] = []
            successes: list[str] = []
            errors: list[str] = []
            cli._print_info = lambda msg: infos.append(msg)  # type: ignore[method-assign]
            cli._print_success = lambda msg: successes.append(msg)  # type: ignore[method-assign]
            cli._print_error = lambda msg: errors.append(msg)  # type: ignore[method-assign]

            await cli._cmd_add("bob")

            contact = cli.contact_manager.get_contact("bob")
            assert contact is not None
            assert contact["p2p_port"] == 2222
            assert infos == ["bob is already a contact; refreshing details"]
            assert successes == ["Updated bob contact details"]
            assert errors == []
        finally:
            shutil.rmtree(tmpdir)

    @pytest.mark.asyncio
    async def test_cmd_msg_refreshes_contact_endpoint_for_existing_session(self):
        """Ensure /msg refreshes peer endpoint before routed delivery."""
        tmpdir = tempfile.mkdtemp()
        try:
            cli = ChatCLI(data_dir=tmpdir)
            cli.client.username = "alice"
            cli.db = ClientDB(os.path.join(tmpdir, "client.db"), b"x" * 32)
            cli.contact_manager = ContactManager(cli.db)

            stale_bundle = {
                "ik_ed_pub": base64.b64encode(b"a" * 32).decode(),
                "ik_dh_pub": base64.b64encode(b"b" * 32).decode(),
                "spk_pub": base64.b64encode(b"c" * 32).decode(),
                "p2p_host": "127.0.0.1",
                "p2p_port": 1111,
            }
            refreshed_bundle = {
                "ik_ed_pub": base64.b64encode(b"a" * 32).decode(),
                "ik_dh_pub": base64.b64encode(b"b" * 32).decode(),
                "spk_pub": base64.b64encode(b"c" * 32).decode(),
                "p2p_host": "127.0.0.1",
                "p2p_port": 3333,
            }
            cli.contact_manager.add_contact("bob", stale_bundle, verify=False)

            class DummyWriter:
                def is_closing(self) -> bool:
                    return False

            class DummySessionManager:
                def has_session(self, username: str) -> bool:
                    return username == "bob"

                def encrypt_message(self, username: str, text: str) -> dict:
                    return {"ciphertext": f"{username}:{text}"}

            cli.client.writer = DummyWriter()  # type: ignore[assignment]
            cli.session_manager = DummySessionManager()  # type: ignore[assignment]

            async def fake_fetch_bundle(username: str):
                assert username == "bob"
                return refreshed_bundle

            route_call: dict[str, str | int] = {}

            async def fake_send_chat_message_routed(**kwargs):
                route_call["peer_host"] = kwargs["peer_host"]
                route_call["peer_port"] = kwargs["peer_port"]
                return "direct", None

            cli.client.fetch_bundle = fake_fetch_bundle  # type: ignore[method-assign]
            cli.client.send_chat_message_routed = fake_send_chat_message_routed  # type: ignore[method-assign]
            cli._print_state = lambda *args, **kwargs: None  # type: ignore[method-assign]
            cli._print_message = lambda *args, **kwargs: None  # type: ignore[method-assign]
            errors: list[str] = []
            cli._print_error = lambda msg: errors.append(msg)  # type: ignore[method-assign]

            await cli._cmd_msg("bob", "hello")

            contact = cli.contact_manager.get_contact("bob")
            assert contact is not None
            assert contact["p2p_port"] == 3333
            assert route_call["peer_host"] == "127.0.0.1"
            assert route_call["peer_port"] == 3333
            assert errors == []
        finally:
            shutil.rmtree(tmpdir)

    @pytest.mark.asyncio
    async def test_cmd_msg_bootstraps_offline_session_from_cached_contact(self):
        """Ensure /msg can bootstrap offline session from cached contact bundle."""
        tmpdir = tempfile.mkdtemp()
        try:
            cli = ChatCLI(data_dir=tmpdir)
            cli.client.username = "alice"
            cli.db = ClientDB(os.path.join(tmpdir, "client.db"), b"x" * 32)
            cli.contact_manager = ContactManager(cli.db)

            cached_bundle = {
                "ik_ed_pub": base64.b64encode(b"a" * 32).decode(),
                "ik_dh_pub": base64.b64encode(b"b" * 32).decode(),
                "spk_pub": base64.b64encode(b"c" * 32).decode(),
                "spk_sig": base64.b64encode(b"d" * 64).decode(),
                "p2p_host": "127.0.0.1",
                "p2p_port": 4444,
            }
            cli.contact_manager.add_contact("bob", cached_bundle, verify=False)

            class DummySessionManager:
                def __init__(self):
                    self.session_ready = False
                    self.initiated_bundle: dict | None = None

                def has_session(self, username: str) -> bool:
                    return self.session_ready and username == "bob"

                def initiate_session(self, username: str, bundle: dict):
                    assert username == "bob"
                    self.initiated_bundle = bundle
                    self.session_ready = True
                    return b"ephemeral", "offline-ephemeral", None

                def encrypt_message(self, username: str, text: str) -> dict:
                    assert username == "bob"
                    assert self.session_ready
                    return {"ciphertext": f"{username}:{text}"}

            dummy_session_manager = DummySessionManager()
            cli.session_manager = dummy_session_manager  # type: ignore[assignment]

            async def fake_fetch_bundle(username: str):
                raise AssertionError(f"fetch_bundle should not be called offline ({username})")

            routed_call: dict[str, str | int] = {}

            async def fake_send_chat_message_routed(**kwargs):
                routed_call["peer_host"] = kwargs["peer_host"]
                routed_call["peer_port"] = kwargs["peer_port"]
                routed_call["ephemeral_pub_b64"] = kwargs["ephemeral_pub_b64"]
                return "direct", None

            cli.client.fetch_bundle = fake_fetch_bundle  # type: ignore[method-assign]
            cli.client.send_chat_message_routed = fake_send_chat_message_routed  # type: ignore[method-assign]
            cli._print_state = lambda *args, **kwargs: None  # type: ignore[method-assign]
            cli._print_message = lambda *args, **kwargs: None  # type: ignore[method-assign]
            errors: list[str] = []
            cli._print_error = lambda msg: errors.append(msg)  # type: ignore[method-assign]

            await cli._cmd_msg("bob", "hello offline")

            assert dummy_session_manager.initiated_bundle is not None
            assert dummy_session_manager.initiated_bundle["spk_sig"] == cached_bundle["spk_sig"]
            assert routed_call["peer_host"] == "127.0.0.1"
            assert routed_call["peer_port"] == cli._preferred_p2p_port("bob")
            assert routed_call["ephemeral_pub_b64"] == "offline-ephemeral"
            assert errors == []
        finally:
            shutil.rmtree(tmpdir)

    @pytest.mark.asyncio
    async def test_send_encrypted_to_bootstraps_offline_session_from_cached_contact(self):
        """Ensure helper sender bootstraps offline session from cached bundle."""
        tmpdir = tempfile.mkdtemp()
        try:
            cli = ChatCLI(data_dir=tmpdir)
            cli.client.username = "alice"
            cli.db = ClientDB(os.path.join(tmpdir, "client.db"), b"x" * 32)
            cli.contact_manager = ContactManager(cli.db)

            cached_bundle = {
                "ik_ed_pub": base64.b64encode(b"a" * 32).decode(),
                "ik_dh_pub": base64.b64encode(b"b" * 32).decode(),
                "spk_pub": base64.b64encode(b"c" * 32).decode(),
                "spk_sig": base64.b64encode(b"d" * 64).decode(),
                "p2p_host": "127.0.0.1",
                "p2p_port": 5555,
            }
            cli.contact_manager.add_contact("bob", cached_bundle, verify=False)

            class DummySessionManager:
                def __init__(self):
                    self.session_ready = False
                    self.initiated_bundle: dict | None = None

                def has_session(self, username: str) -> bool:
                    return self.session_ready and username == "bob"

                def initiate_session(self, username: str, bundle: dict):
                    assert username == "bob"
                    self.initiated_bundle = bundle
                    self.session_ready = True
                    return b"ephemeral", "helper-offline-ephemeral", None

                def encrypt_message(self, username: str, text: str) -> dict:
                    assert username == "bob"
                    assert self.session_ready
                    return {"ciphertext": f"{username}:{text}"}

            dummy_session_manager = DummySessionManager()
            cli.session_manager = dummy_session_manager  # type: ignore[assignment]

            async def fake_send_chat_message_routed(**kwargs):
                assert kwargs["peer_host"] == "127.0.0.1"
                assert kwargs["peer_port"] == cli._preferred_p2p_port("bob")
                assert kwargs["ephemeral_pub_b64"] == "helper-offline-ephemeral"
                return "direct", None

            cli.client.send_chat_message_routed = fake_send_chat_message_routed  # type: ignore[method-assign]

            success = await cli._send_encrypted_to("bob", "group key")

            assert success is True
            assert dummy_session_manager.initiated_bundle is not None
            assert dummy_session_manager.initiated_bundle["spk_sig"] == cached_bundle["spk_sig"]
        finally:
            shutil.rmtree(tmpdir)

    @pytest.mark.asyncio
    async def test_cmd_msg_disconnected_direct_failure_prints_actionable_guidance(self):
        """Ensure direct-delivery failure prints recovery guidance while offline."""
        tmpdir = tempfile.mkdtemp()
        try:
            cli = ChatCLI(data_dir=tmpdir)
            cli.client.username = "alice"
            cli.db = ClientDB(os.path.join(tmpdir, "client.db"), b"x" * 32)
            cli.contact_manager = ContactManager(cli.db)
            cli.contact_manager.add_contact(
                "bob",
                {
                    "ik_ed_pub": base64.b64encode(b"a" * 32).decode(),
                    "ik_dh_pub": base64.b64encode(b"b" * 32).decode(),
                    "spk_pub": base64.b64encode(b"c" * 32).decode(),
                    "spk_sig": base64.b64encode(b"d" * 64).decode(),
                    "p2p_host": "127.0.0.1",
                    "p2p_port": 4444,
                },
                verify=False,
            )

            class DummySessionManager:
                def has_session(self, username: str) -> bool:
                    return username == "bob"

                def encrypt_message(self, username: str, text: str) -> dict:
                    return {"ciphertext": f"{username}:{text}"}

            cli.session_manager = DummySessionManager()  # type: ignore[assignment]

            async def fake_send_chat_message_routed(**kwargs):
                raise ConnectionError(
                    "Server disconnected and direct delivery to 127.0.0.1:4444 failed"
                )

            cli.client.send_chat_message_routed = fake_send_chat_message_routed  # type: ignore[method-assign]

            errors: list[str] = []
            infos: list[str] = []
            cli._print_error = lambda msg: errors.append(msg)  # type: ignore[method-assign]
            cli._print_info = lambda msg: infos.append(msg)  # type: ignore[method-assign]

            await cli._cmd_msg("bob", "hello")

            assert errors == [
                "Failed to send message: Server disconnected and direct delivery to 127.0.0.1:4444 failed"
            ]
            assert infos == [
                "Direct delivery requires the recipient to be online now with an active direct listener.",
                "If the recipient is offline, reconnect + /login and resend so the server can queue it.",
            ]
        finally:
            shutil.rmtree(tmpdir)

    @pytest.mark.asyncio
    async def test_cmd_msg_disconnected_with_incomplete_cached_bundle_reports_error(self):
        """Ensure incomplete cached bundle reports clear offline bootstrap error."""
        tmpdir = tempfile.mkdtemp()
        try:
            cli = ChatCLI(data_dir=tmpdir)
            cli.client.username = "alice"
            cli.db = ClientDB(os.path.join(tmpdir, "client.db"), b"x" * 32)
            cli.contact_manager = ContactManager(cli.db)

            incomplete_bundle = {
                "ik_ed_pub": base64.b64encode(b"a" * 32).decode(),
                "ik_dh_pub": base64.b64encode(b"b" * 32).decode(),
                "spk_pub": base64.b64encode(b"c" * 32).decode(),
                "p2p_host": "127.0.0.1",
                "p2p_port": 4444,
            }
            cli.contact_manager.add_contact("bob", incomplete_bundle, verify=False)

            class DummySessionManager:
                def has_session(self, username: str) -> bool:
                    return False

            cli.session_manager = DummySessionManager()  # type: ignore[assignment]
            errors: list[str] = []
            cli._print_error = lambda msg: errors.append(msg)  # type: ignore[method-assign]

            await cli._cmd_msg("bob", "hello offline")

            assert len(errors) == 1
            assert "missing required fields (spk_sig)" in errors[0]
        finally:
            shutil.rmtree(tmpdir)
