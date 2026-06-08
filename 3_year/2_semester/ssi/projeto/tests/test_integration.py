"""
Integration tests for the E2EE chat system.

Tests the full stack: client <-> server communication with E2EE.
"""
from __future__ import annotations

import asyncio
import base64
import json
import os
import shutil
import socket
import tempfile

import pytest

from src.server.server import ChatServer
from src.client.client import ChatClient
from src.client.key_manager import KeyManager
from src.client.session_manager import SessionManager
from src.client.contact_manager import ContactManager
from src.storage.client_db import ClientDB
from src.protocol.messages import DeliverMessage, BaseMessage, SendMessage, DirectAck
from src.protocol.transport import send_message, recv_message

# Configure pytest-asyncio
pytestmark = pytest.mark.asyncio(loop_scope="function")


def get_free_port():
    """Get a free port for testing."""
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.bind(('', 0))
        s.listen(1)
        port = s.getsockname()[1]
    return port


class TestIntegration:
    """Integration tests for client-server communication."""
    
    @pytest.fixture
    def client_setup(self):
        """Create client setup."""
        tmpdir = tempfile.mkdtemp()
        yield tmpdir
        shutil.rmtree(tmpdir)
    
    @pytest.fixture
    def server_setup(self):
        """Create server setup with temp files and dynamic port."""
        tmpdir = tempfile.mkdtemp()
        
        import config
        original_db_path = config.DB_PATH_SERVER
        original_ca_key = config.CA_KEY_PATH
        original_ca_cert = config.CA_CERT_PATH
        original_port = config.SERVER_PORT
        
        config.DB_PATH_SERVER = os.path.join(tmpdir, "server.db")
        config.CA_KEY_PATH = os.path.join(tmpdir, "ca_key.raw")
        config.CA_CERT_PATH = os.path.join(tmpdir, "ca_cert.pem")
        config.SERVER_PORT = get_free_port()
        
        yield tmpdir
        
        config.DB_PATH_SERVER = original_db_path
        config.CA_KEY_PATH = original_ca_key
        config.CA_CERT_PATH = original_ca_cert
        config.SERVER_PORT = original_port
        shutil.rmtree(tmpdir)
    
    async def test_connect_to_server(self, server_setup, client_setup):
        """Test basic connection to server."""
        # Start server
        server = ChatServer()
        server_task = asyncio.create_task(server.start())
        await asyncio.sleep(0.1)  # Let server start
        
        client = ChatClient()
        
        try:
            await client.connect()
            assert client.connected
            assert client.transport_key_send is not None
            assert client.transport_key_recv is not None
            assert client.ca_cert_pem is not None
        finally:
            await client.disconnect()
            server_task.cancel()
            try:
                await server_task
            except asyncio.CancelledError:
                pass
    
    async def test_register_user(self, server_setup, client_setup):
        """Test user registration."""
        server = ChatServer()
        server_task = asyncio.create_task(server.start())
        await asyncio.sleep(0.1)
        
        client = ChatClient()
        km = KeyManager("alice", data_dir=client_setup)
        
        try:
            await client.connect()
            
            bundle = km.get_registration_bundle()
            success = await client.register("alice", "password123", bundle)
            
            assert success
            assert client.authenticated
            assert client.username == "alice"
        finally:
            await client.disconnect()
            server_task.cancel()
            try:
                await server_task
            except asyncio.CancelledError:
                pass
    
    async def test_login_user(self, server_setup, client_setup):
        """Test user login after registration."""
        server = ChatServer()
        server_task = asyncio.create_task(server.start())
        await asyncio.sleep(0.1)
        
        km = KeyManager("bob", data_dir=client_setup)
        
        try:
            # First register
            client1 = ChatClient()
            await client1.connect()
            bundle = km.get_registration_bundle()
            await client1.register("bob", "password123", bundle)
            await client1.disconnect()
            
            # Now login
            client2 = ChatClient()
            await client2.connect()
            success = await client2.login("bob", "password123")
            
            assert success
            assert client2.username == "bob"
            await client2.disconnect()
        finally:
            server_task.cancel()
            try:
                await server_task
            except asyncio.CancelledError:
                pass

    async def test_failed_login_then_reconnect_allows_register(self, server_setup, client_setup):
        """After failed login, reconnecting should allow a clean registration attempt."""
        server = ChatServer()
        server_task = asyncio.create_task(server.start())
        await asyncio.sleep(0.1)

        client = ChatClient()
        km_hugo = KeyManager("hugo", data_dir=client_setup)

        try:
            await client.connect()
            success = await client.login("alice", "wrongpass")
            assert success is False
            assert not client.connected

            await client.connect()
            registered = await client.register("hugo", "password123", km_hugo.get_registration_bundle())
            assert registered
            assert client.authenticated
            assert client.username == "hugo"
        finally:
            await client.disconnect()
            server_task.cancel()
            try:
                await server_task
            except asyncio.CancelledError:
                pass

    async def test_failed_login_then_duplicate_register_fails_cleanly(self, server_setup, client_setup):
        """Duplicate register after failed login should return AuthFail semantics, not transport errors."""
        server = ChatServer()
        server_task = asyncio.create_task(server.start())
        await asyncio.sleep(0.1)

        km_hugo = KeyManager("hugo", data_dir=client_setup)
        client_register = ChatClient()
        client_login = ChatClient()

        try:
            await client_register.connect()
            ok = await client_register.register("hugo", "pass123", km_hugo.get_registration_bundle())
            assert ok
            await client_register.disconnect()

            await client_login.connect()
            logged_in = await client_login.login("hugo", "wrongpass")
            assert logged_in is False
            assert not client_login.connected

            await client_login.connect()
            duplicate_register = await client_login.register(
                "hugo",
                "pass123",
                km_hugo.get_registration_bundle(),
            )
            assert duplicate_register is False
            assert not client_login.connected
        finally:
            await client_register.disconnect()
            await client_login.disconnect()
            server_task.cancel()
            try:
                await server_task
            except asyncio.CancelledError:
                pass

    async def test_same_user_can_keep_multiple_active_sessions(self, server_setup, client_setup):
        """Same account can keep multiple simultaneous active server sessions."""
        server = ChatServer()
        server_task = asyncio.create_task(server.start())
        await asyncio.sleep(0.1)

        km_alice = KeyManager("alice", data_dir=client_setup)
        client_alice_1 = ChatClient()
        client_alice_2 = ChatClient()

        try:
            await client_alice_1.connect()
            await client_alice_1.register("alice", "pass1", km_alice.get_registration_bundle())

            await client_alice_2.connect()
            logged_in = await client_alice_2.login("alice", "pass1")
            assert logged_in

            await asyncio.sleep(0.1)
            handlers = server.connected_clients.get("alice")
            assert handlers is not None
            assert len(handlers) == 2
        finally:
            await client_alice_1.disconnect()
            await client_alice_2.disconnect()
            server_task.cancel()
            try:
                await server_task
            except asyncio.CancelledError:
                pass
    
    async def test_fetch_bundle(self, server_setup, client_setup):
        """Test fetching another user's key bundle."""
        server = ChatServer()
        server_task = asyncio.create_task(server.start())
        await asyncio.sleep(0.1)
        
        km_alice = KeyManager("alice", data_dir=client_setup)
        km_bob = KeyManager("bob", data_dir=client_setup)
        
        try:
            # Register alice
            client_alice = ChatClient()
            await client_alice.connect()
            await client_alice.register("alice", "pass1", km_alice.get_registration_bundle())
            
            # Register bob
            client_bob = ChatClient()
            await client_bob.connect()
            await client_bob.register("bob", "pass2", km_bob.get_registration_bundle())
            
            # Bob fetches Alice's bundle
            alice_bundle = await client_bob.fetch_bundle("alice")
            
            assert alice_bundle is not None
            assert "ik_ed_pub" in alice_bundle
            assert "ik_dh_pub" in alice_bundle
            assert "spk_pub" in alice_bundle
            
            # Nonexistent user
            nonexistent = await client_bob.fetch_bundle("nobody")
            assert nonexistent is None
            
            await client_alice.disconnect()
            await client_bob.disconnect()
        finally:
            server_task.cancel()
            try:
                await server_task
            except asyncio.CancelledError:
                pass
    
    async def test_send_receive_message_online(self, server_setup, client_setup):
        """Test sending message when recipient is online."""
        server = ChatServer()
        server_task = asyncio.create_task(server.start())
        await asyncio.sleep(0.1)
        
        km_alice = KeyManager("alice", data_dir=client_setup)
        km_bob = KeyManager("bob", data_dir=client_setup)
        
        try:
            # Set up Alice
            client_alice = ChatClient()
            db_alice = ClientDB(os.path.join(client_setup, "alice.db"), b"x" * 32)
            await client_alice.connect()
            await client_alice.register("alice", "pass1", km_alice.get_registration_bundle())
            sm_alice = SessionManager(km_alice, db_alice)
            
            # Set up Bob
            client_bob = ChatClient()
            db_bob = ClientDB(os.path.join(client_setup, "bob.db"), b"x" * 32)
            await client_bob.connect()
            await client_bob.register("bob", "pass2", km_bob.get_registration_bundle())
            sm_bob = SessionManager(km_bob, db_bob)
            
            # Collect received messages
            received_messages = []
            
            async def bob_handler(msg):
                received_messages.append(msg)
            
            client_bob.set_message_handler(bob_handler)
            receive_task = asyncio.create_task(client_bob.start_receiving())
            
            # Alice gets Bob's bundle and initiates session
            bob_bundle = await client_alice.fetch_bundle("bob")
            eph_pub, eph_pub_b64, opk_id = sm_alice.initiate_session("bob", bob_bundle)
            
            # Alice encrypts and sends message
            encrypted = sm_alice.encrypt_message("bob", "Hello Bob!")
            ciphertext_b64 = base64.b64encode(json.dumps(encrypted).encode()).decode()
            
            await client_alice.send_chat_message(
                recipient="bob",
                ciphertext_b64=ciphertext_b64,
                ephemeral_pub_b64=eph_pub_b64,
                opk_id=opk_id,
            )
            
            # Wait for message delivery
            await asyncio.sleep(0.5)
            
            # Bob should have received a message
            assert len(received_messages) > 0
            
            client_bob.stop_receiving()
            receive_task.cancel()
            try:
                await receive_task
            except asyncio.CancelledError:
                pass
            
            await client_alice.disconnect()
            await client_bob.disconnect()
        finally:
            server_task.cancel()
            try:
                await server_task
            except asyncio.CancelledError:
                pass

    async def test_send_message_fanout_to_all_recipient_sessions(self, server_setup, client_setup):
        """Server-routed direct messages are fanned out to all recipient sessions."""
        server = ChatServer()
        server_task = asyncio.create_task(server.start())
        await asyncio.sleep(0.1)

        km_alice = KeyManager("alice", data_dir=client_setup)
        km_bob = KeyManager("bob", data_dir=client_setup)

        client_alice_1 = ChatClient()
        client_alice_2 = ChatClient()
        client_bob = ChatClient()

        alice_1_received = asyncio.Event()
        alice_2_received = asyncio.Event()

        try:
            await client_alice_1.connect()
            await client_alice_1.register("alice", "pass1", km_alice.get_registration_bundle())

            await client_alice_2.connect()
            logged_in = await client_alice_2.login("alice", "pass1")
            assert logged_in

            await client_bob.connect()
            await client_bob.register("bob", "pass2", km_bob.get_registration_bundle())

            async def alice_1_handler(msg):
                if isinstance(msg, DeliverMessage):
                    payload = BaseMessage.from_json(msg.payload_json)
                    if isinstance(payload, SendMessage) and payload.msg_id == "fanout-1":
                        alice_1_received.set()
                return True

            async def alice_2_handler(msg):
                if isinstance(msg, DeliverMessage):
                    payload = BaseMessage.from_json(msg.payload_json)
                    if isinstance(payload, SendMessage) and payload.msg_id == "fanout-1":
                        alice_2_received.set()
                return True

            client_alice_1.set_message_handler(alice_1_handler)
            client_alice_2.set_message_handler(alice_2_handler)
            await client_alice_1.start_receiving()
            await client_alice_2.start_receiving()

            await client_bob.send_chat_message(
                recipient="alice",
                ciphertext_b64=base64.b64encode(b'{"ciphertext":"fanout"}').decode(),
                msg_id="fanout-1",
            )

            await asyncio.wait_for(
                asyncio.gather(alice_1_received.wait(), alice_2_received.wait()),
                timeout=2.0,
            )
        finally:
            await client_alice_1.disconnect()
            await client_alice_2.disconnect()
            await client_bob.disconnect()
            server_task.cancel()
            try:
                await server_task
            except asyncio.CancelledError:
                pass

    async def test_disconnect_of_one_session_keeps_other_session_active(self, server_setup, client_setup):
        """Disconnecting one recipient session should not remove other active sessions."""
        server = ChatServer()
        server_task = asyncio.create_task(server.start())
        await asyncio.sleep(0.1)

        km_alice = KeyManager("alice", data_dir=client_setup)
        km_bob = KeyManager("bob", data_dir=client_setup)

        client_alice_1 = ChatClient()
        client_alice_2 = ChatClient()
        client_bob = ChatClient()

        alice_2_received = asyncio.Event()

        try:
            await client_alice_1.connect()
            await client_alice_1.register("alice", "pass1", km_alice.get_registration_bundle())

            await client_alice_2.connect()
            logged_in = await client_alice_2.login("alice", "pass1")
            assert logged_in

            await client_bob.connect()
            await client_bob.register("bob", "pass2", km_bob.get_registration_bundle())

            async def alice_2_handler(msg):
                if isinstance(msg, DeliverMessage):
                    payload = BaseMessage.from_json(msg.payload_json)
                    if isinstance(payload, SendMessage) and payload.msg_id == "isolation-1":
                        alice_2_received.set()
                return True

            client_alice_2.set_message_handler(alice_2_handler)
            await client_alice_2.start_receiving()

            await client_alice_1.disconnect()
            await asyncio.sleep(0.2)

            alice_handlers = server.connected_clients.get("alice")
            assert alice_handlers is not None
            assert len(alice_handlers) == 1

            await client_bob.send_chat_message(
                recipient="alice",
                ciphertext_b64=base64.b64encode(b'{"ciphertext":"isolation"}').decode(),
                msg_id="isolation-1",
            )

            await asyncio.wait_for(alice_2_received.wait(), timeout=2.0)
        finally:
            await client_alice_1.disconnect()
            await client_alice_2.disconnect()
            await client_bob.disconnect()
            server_task.cancel()
            try:
                await server_task
            except asyncio.CancelledError:
                pass

    async def test_receive_loop_stops_after_server_side_socket_drop(self, server_setup, client_setup):
        """Receive loop should stop cleanly when server closes the client socket."""
        server = ChatServer()
        server_task = asyncio.create_task(server.start())
        await asyncio.sleep(0.1)

        km_alice = KeyManager("alice", data_dir=client_setup)
        client_alice = ChatClient()

        try:
            await client_alice.connect()
            await client_alice.register("alice", "pass1", km_alice.get_registration_bundle())

            async def noop_handler(_msg):
                return True

            client_alice.set_message_handler(noop_handler)
            await client_alice.start_receiving()
            assert client_alice._receive_task is not None

            server_handlers = server.connected_clients.get("alice")
            assert server_handlers
            server_handler = next(iter(server_handlers))

            server_handler.writer.close()
            await server_handler.writer.wait_closed()

            await asyncio.wait_for(client_alice._receive_task, timeout=2.0)
            assert client_alice._running is False
        finally:
            await client_alice.disconnect()
            server_task.cancel()
            try:
                await server_task
            except asyncio.CancelledError:
                pass

    async def test_direct_delivery_after_server_bootstrap(self, server_setup, client_setup):
        """After bootstrap via server, routed send uses server relay by default."""
        server = ChatServer()
        server_task = asyncio.create_task(server.start())
        await asyncio.sleep(0.1)

        km_alice = KeyManager("alice", data_dir=client_setup)
        km_bob = KeyManager("bob", data_dir=client_setup)

        try:
            client_alice = ChatClient()
            await client_alice.connect()
            await client_alice.ensure_direct_listener()
            alice_p2p_host, alice_p2p_port = client_alice.get_advertised_endpoint()
            await client_alice.register(
                "alice",
                "pass1",
                km_alice.get_registration_bundle(),
                p2p_host=alice_p2p_host,
                p2p_port=alice_p2p_port,
            )

            client_bob = ChatClient()
            await client_bob.connect()
            await client_bob.ensure_direct_listener()
            bob_p2p_host, bob_p2p_port = client_bob.get_advertised_endpoint()
            await client_bob.register(
                "bob",
                "pass2",
                km_bob.get_registration_bundle(),
                p2p_host=bob_p2p_host,
                p2p_port=bob_p2p_port,
            )

            received_payloads: list[str] = []

            async def bob_handler(msg):
                if isinstance(msg, DeliverMessage):
                    received_payloads.append(msg.payload_json)

            client_bob.set_message_handler(bob_handler)
            await client_bob.start_receiving()

            bob_bundle = await client_alice.fetch_bundle("bob")
            assert bob_bundle is not None
            assert bob_bundle.get("p2p_host")
            assert bob_bundle.get("p2p_port")

            route, refreshed_bundle = await client_alice.send_chat_message_routed(
                recipient="bob",
                sender="alice",
                ciphertext_b64=base64.b64encode(b'{"ciphertext":"demo"}').decode(),
                msg_id="direct-1",
                peer_host=bob_bundle["p2p_host"],
                peer_port=int(bob_bundle["p2p_port"]),
            )

            assert route == "server"
            assert refreshed_bundle is None
            await asyncio.sleep(0.2)
            assert len(received_payloads) == 1

            parsed = BaseMessage.from_json(received_payloads[0])
            assert isinstance(parsed, SendMessage)
            assert parsed.msg_id == "direct-1"
            assert parsed.sender == "alice"
            assert parsed.recipient == "bob"

            await client_alice.disconnect()
            await client_bob.disconnect()
        finally:
            server_task.cancel()
            try:
                await server_task
            except asyncio.CancelledError:
                pass

    async def test_direct_send_requires_application_ack(self, client_setup):
        """Direct send must wait for app-level ACK, not just TCP write success."""
        no_ack_event = asyncio.Event()

        async def no_ack_handler(reader, writer):
            await recv_message(reader, None)  # Consume inbound message
            no_ack_event.set()
            writer.close()
            await writer.wait_closed()

        server = await asyncio.start_server(no_ack_handler, "127.0.0.1", 0)
        port = server.sockets[0].getsockname()[1]

        client = ChatClient()
        try:
            success = await client.send_chat_message_direct(
                peer_host="127.0.0.1",
                peer_port=port,
                recipient="bob",
                sender="alice",
                ciphertext_b64=base64.b64encode(b'{"ciphertext":"demo"}').decode(),
                msg_id="ack-required",
                timeout=1.0,
            )
            assert no_ack_event.is_set()
            assert success is False
        finally:
            server.close()
            await server.wait_closed()

    async def test_direct_listener_rejects_wrong_recipient(self, client_setup):
        """Direct listener should reject messages not addressed to local user."""
        client = ChatClient()
        client.username = "bob"
        await client.ensure_direct_listener(host="127.0.0.1", port=0)
        _, port = client.get_advertised_endpoint()

        reader, writer = await asyncio.open_connection("127.0.0.1", port)
        try:
            await send_message(
                writer,
                None,
                SendMessage(
                    msg_id="bad-recipient",
                    sender="alice",
                    recipient="charlie",
                    ciphertext_b64=base64.b64encode(b'{"ciphertext":"demo"}').decode(),
                ),
            )
            ack = await recv_message(reader, None)
            assert isinstance(ack, DirectAck)
            assert ack.msg_id == "bad-recipient"
            assert ack.accepted is False
            assert ack.reason == "wrong_recipient"
        finally:
            writer.close()
            await writer.wait_closed()
            await client.disconnect()

    async def test_routed_send_falls_back_to_direct_when_server_send_fails(self, server_setup, client_setup):
        """Routed send should use direct path when server send fails."""
        server = ChatServer()
        server_task = asyncio.create_task(server.start())
        await asyncio.sleep(0.1)

        km_alice = KeyManager("alice", data_dir=client_setup)
        km_bob = KeyManager("bob", data_dir=client_setup)

        try:
            client_alice = ChatClient()
            await client_alice.connect()
            await client_alice.ensure_direct_listener()
            alice_host, alice_port = client_alice.get_advertised_endpoint()
            await client_alice.register(
                "alice",
                "pass1",
                km_alice.get_registration_bundle(),
                p2p_host=alice_host,
                p2p_port=alice_port,
            )

            client_bob = ChatClient()
            await client_bob.connect()
            await client_bob.ensure_direct_listener()
            bob_host, bob_port = client_bob.get_advertised_endpoint()
            await client_bob.register(
                "bob",
                "pass2",
                km_bob.get_registration_bundle(),
                p2p_host=bob_host,
                p2p_port=bob_port,
            )

            received_payloads: list[str] = []

            async def bob_handler(msg):
                if isinstance(msg, DeliverMessage):
                    received_payloads.append(msg.payload_json)
                return True

            client_bob.set_message_handler(bob_handler)
            await client_bob.start_receiving()

            async def fail_server_send(*args, **kwargs):
                raise ConnectionError("server unavailable")

            client_alice.send_chat_message = fail_server_send  # type: ignore[method-assign]

            route, refreshed_bundle = await client_alice.send_chat_message_routed(
                recipient="bob",
                sender="alice",
                ciphertext_b64=base64.b64encode(b'{"ciphertext":"demo"}').decode(),
                msg_id="fallback-direct-1",
                peer_host=bob_host,
                peer_port=bob_port,
            )

            assert route == "direct"
            assert refreshed_bundle is None
            await asyncio.sleep(0.2)
            assert len(received_payloads) == 1

            await client_alice.disconnect()
            await client_bob.disconnect()
        finally:
            server_task.cancel()
            try:
                await server_task
            except asyncio.CancelledError:
                pass

    async def test_routed_send_uses_direct_after_server_only_disconnect(self, server_setup, client_setup):
        """After server-only disconnect, routed send should still use direct endpoint fallback."""
        server = ChatServer()
        server_task = asyncio.create_task(server.start())
        await asyncio.sleep(0.1)

        km_alice = KeyManager("alice", data_dir=client_setup)
        km_bob = KeyManager("bob", data_dir=client_setup)

        try:
            client_alice = ChatClient()
            await client_alice.connect()
            await client_alice.ensure_direct_listener()
            alice_host, alice_port = client_alice.get_advertised_endpoint()
            await client_alice.register(
                "alice",
                "pass1",
                km_alice.get_registration_bundle(),
                p2p_host=alice_host,
                p2p_port=alice_port,
            )

            client_bob = ChatClient()
            await client_bob.connect()
            await client_bob.ensure_direct_listener()
            bob_host, bob_port = client_bob.get_advertised_endpoint()
            await client_bob.register(
                "bob",
                "pass2",
                km_bob.get_registration_bundle(),
                p2p_host=bob_host,
                p2p_port=bob_port,
            )

            received_payloads: list[str] = []

            async def bob_handler(msg):
                if isinstance(msg, DeliverMessage):
                    received_payloads.append(msg.payload_json)
                return True

            client_bob.set_message_handler(bob_handler)
            await client_bob.start_receiving()

            await client_alice.disconnect_server_only()
            route, refreshed_bundle = await client_alice.send_chat_message_routed(
                recipient="bob",
                sender="alice",
                ciphertext_b64=base64.b64encode(b'{"ciphertext":"demo"}').decode(),
                msg_id="disconnect-direct-1",
                peer_host=bob_host,
                peer_port=bob_port,
            )

            assert route == "direct"
            assert refreshed_bundle is None
            await asyncio.sleep(0.2)
            assert len(received_payloads) == 1

            await client_alice.disconnect()
            await client_bob.disconnect()
        finally:
            server_task.cancel()
            try:
                await server_task
            except asyncio.CancelledError:
                pass

    async def test_routed_send_after_disconnect_without_endpoint_fails_clearly(self, server_setup, client_setup):
        """After server-only disconnect, missing direct endpoint should raise a clear error."""
        server = ChatServer()
        server_task = asyncio.create_task(server.start())
        await asyncio.sleep(0.1)

        km_alice = KeyManager("alice", data_dir=client_setup)
        client_alice = ChatClient()
        try:
            await client_alice.connect()
            await client_alice.ensure_direct_listener()
            p2p_host, p2p_port = client_alice.get_advertised_endpoint()
            await client_alice.register(
                "alice",
                "pass1",
                km_alice.get_registration_bundle(),
                p2p_host=p2p_host,
                p2p_port=p2p_port,
            )

            await client_alice.disconnect_server_only()

            with pytest.raises(
                ConnectionError,
                match="Server disconnected and no direct endpoint is available for this contact",
            ):
                await client_alice.send_chat_message_routed(
                    recipient="bob",
                    sender="alice",
                    ciphertext_b64=base64.b64encode(b'{"ciphertext":"demo"}').decode(),
                    msg_id="disconnect-no-endpoint-1",
                )
        finally:
            await client_alice.disconnect()
            server_task.cancel()
            try:
                await server_task
            except asyncio.CancelledError:
                pass

    async def test_disconnect_server_only_keeps_direct_listener(self, server_setup, client_setup):
        """Disconnecting from server should keep direct listener active for demo fallback."""
        server = ChatServer()
        server_task = asyncio.create_task(server.start())
        await asyncio.sleep(0.1)

        km_alice = KeyManager("alice", data_dir=client_setup)

        client_alice = ChatClient()
        try:
            await client_alice.connect()
            await client_alice.ensure_direct_listener()
            p2p_host, p2p_port = client_alice.get_advertised_endpoint()
            await client_alice.register(
                "alice",
                "pass1",
                km_alice.get_registration_bundle(),
                p2p_host=p2p_host,
                p2p_port=p2p_port,
            )

            await client_alice.disconnect_server_only()
            assert not client_alice.connected
            assert client_alice.authenticated

            reader, writer = await asyncio.open_connection("127.0.0.1", p2p_port)
            try:
                await send_message(
                    writer,
                    None,
                    SendMessage(
                        msg_id="demo-disconnect",
                        sender="bob",
                        recipient="alice",
                        ciphertext_b64=base64.b64encode(b'{"ciphertext":"demo"}').decode(),
                    ),
                )
                ack = await recv_message(reader, None)
                assert isinstance(ack, DirectAck)
                assert ack.msg_id == "demo-disconnect"
            finally:
                writer.close()
                await writer.wait_closed()
        finally:
            await client_alice.disconnect()
            server_task.cancel()
            try:
                await server_task
            except asyncio.CancelledError:
                pass

    async def test_reconnect_requires_relogin_after_server_only_disconnect(self, server_setup, client_setup):
        """Reconnecting after server-only disconnect should require a fresh login."""
        server = ChatServer()
        server_task = asyncio.create_task(server.start())
        await asyncio.sleep(0.1)

        km_alice = KeyManager("alice", data_dir=client_setup)
        client_alice = ChatClient()
        try:
            await client_alice.connect()
            await client_alice.ensure_direct_listener()
            p2p_host, p2p_port = client_alice.get_advertised_endpoint()
            await client_alice.register(
                "alice",
                "pass1",
                km_alice.get_registration_bundle(),
                p2p_host=p2p_host,
                p2p_port=p2p_port,
            )
            assert client_alice.authenticated

            await client_alice.disconnect_server_only()
            assert client_alice.authenticated

            await client_alice.connect()
            assert client_alice.connected
            assert not client_alice.authenticated

            p2p_host, p2p_port = client_alice.get_advertised_endpoint()
            logged_in = await client_alice.login(
                "alice",
                "pass1",
                p2p_host=p2p_host,
                p2p_port=p2p_port,
            )
            assert logged_in
            assert client_alice.authenticated
        finally:
            await client_alice.disconnect()
            server_task.cancel()
            try:
                await server_task
            except asyncio.CancelledError:
                pass

    async def test_server_api_calls_fail_cleanly_after_server_only_disconnect(self, server_setup, client_setup):
        """After server-only disconnect, server API methods should raise ConnectionError (not attribute errors)."""
        server = ChatServer()
        server_task = asyncio.create_task(server.start())
        await asyncio.sleep(0.1)

        km_alice = KeyManager("alice", data_dir=client_setup)
        client_alice = ChatClient()
        try:
            await client_alice.connect()
            await client_alice.ensure_direct_listener()
            p2p_host, p2p_port = client_alice.get_advertised_endpoint()
            await client_alice.register(
                "alice",
                "pass1",
                km_alice.get_registration_bundle(),
                p2p_host=p2p_host,
                p2p_port=p2p_port,
            )

            await client_alice.disconnect_server_only()

            with pytest.raises(ConnectionError, match="Not connected to server"):
                await client_alice.fetch_bundle("bob")

            with pytest.raises(ConnectionError, match="Not connected to server"):
                await client_alice.send_chat_message(
                    recipient="bob",
                    ciphertext_b64=base64.b64encode(b'{"ciphertext":"demo"}').decode(),
                    msg_id="clean-error-1",
                )
        finally:
            await client_alice.disconnect()
            server_task.cancel()
            try:
                await server_task
            except asyncio.CancelledError:
                pass


# Run with: pytest tests/test_integration.py -v
if __name__ == "__main__":
    pytest.main([__file__, "-v"])
