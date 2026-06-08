"""Tests for protocol serialization, transport framing and handshake."""

from __future__ import annotations

import base64
import socket
import threading

from src.crypto.asymmetric import generate_ed25519_keypair
from src.protocol.handshake import client_handshake, server_handshake
from src.protocol.messages import (
    BaseMessage,
    BundleResponse,
    ClientAuth,
    ChatMessage,
    ClientHello,
    DirectAck,
    FetchBundle,
    KeyBundle,
    MsgType,
    OfflineMessages,
    Register,
    SendMessage,
    ServerHello,
)
from src.protocol.transport import recv_message, send_message


def test_message_dispatch_from_json_for_chat_and_bundle() -> None:
    """Ensure JSON dispatch picks ChatMessage and KeyBundle correctly."""
    chat = ChatMessage(
        sender="alice",
        recipient="bob",
        ratchet_dh_pub=base64.b64encode(b"x" * 32).decode("ascii"),
        nonce=base64.b64encode(b"n" * 12).decode("ascii"),
        ciphertext=base64.b64encode(b"cipher").decode("ascii"),
    )
    parsed_chat = BaseMessage.from_json(chat.to_json())
    assert isinstance(parsed_chat, ChatMessage)
    assert parsed_chat.sender == "alice"

    bundle = KeyBundle(
        username="bob",
        ik_ed_pub=base64.b64encode(b"a" * 32).decode("ascii"),
        ik_dh_pub=base64.b64encode(b"b" * 32).decode("ascii"),
        spk_pub=base64.b64encode(b"c" * 32).decode("ascii"),
        spk_sig=base64.b64encode(b"d" * 64).decode("ascii"),
    )
    parsed_bundle = BaseMessage.from_json(bundle.to_json())
    assert isinstance(parsed_bundle, KeyBundle)
    assert parsed_bundle.username == "bob"


def test_chat_message_sign_and_verify() -> None:
    """Ensure chat signatures validate and fail after payload tampering."""
    priv, pub = generate_ed25519_keypair()
    msg = ChatMessage(
        sender="alice",
        recipient="bob",
        ratchet_dh_pub=base64.b64encode(b"r" * 32).decode("ascii"),
        nonce=base64.b64encode(b"n" * 12).decode("ascii"),
        ciphertext=base64.b64encode(b"payload").decode("ascii"),
    )
    msg.sign(priv)
    assert msg.verify(pub)

    msg.ciphertext = base64.b64encode(b"tampered").decode("ascii")
    assert not msg.verify(pub)


def test_transport_send_and_receive_encrypted_message() -> None:
    """Ensure encrypted framed transport roundtrip returns expected message type."""
    left, right = socket.socketpair()
    key = b"k" * 32
    outgoing = ChatMessage(
        sender="alice",
        recipient="bob",
        ratchet_dh_pub=base64.b64encode(b"r" * 32).decode("ascii"),
        nonce=base64.b64encode(b"n" * 12).decode("ascii"),
        ciphertext=base64.b64encode(b"encrypted").decode("ascii"),
        type=MsgType.SEND_MSG,
    )

    try:
        send_message(left, key, outgoing)
        incoming = recv_message(right, key)
        assert isinstance(incoming, ChatMessage)
        assert incoming.sender == "alice"
        assert incoming.recipient == "bob"
    finally:
        left.close()
        right.close()


def test_client_server_handshake_derives_complementary_keys() -> None:
    """Ensure client and server derive complementary directional transport keys."""
    client_sock, server_sock = socket.socketpair()
    server_result: dict[str, bytes | None] = {"send": None, "recv": None}

    def _run_server() -> None:
        send_key, recv_key, _ = server_handshake(server_sock, None, None)
        server_result["send"] = send_key
        server_result["recv"] = recv_key

    thread = threading.Thread(target=_run_server)
    thread.start()
    try:
        client_send, client_recv = client_handshake(client_sock)
        thread.join(timeout=2)
        assert server_result["send"] == client_recv
        assert server_result["recv"] == client_send
    finally:
        client_sock.close()
        server_sock.close()


def test_send_message_serialization() -> None:
    """Ensure SendMessage survives JSON serialization/deserialization intact."""
    msg = SendMessage(
        msg_id="m-123",
        sender="alice",
        recipient="bob",
        ciphertext_b64=base64.b64encode(b"encrypted_payload").decode("ascii"),
        ephemeral_pub_b64=base64.b64encode(b"x" * 32).decode("ascii"),
        opk_id=42,
    )

    json_str = msg.to_json()
    parsed = BaseMessage.from_json(json_str)

    assert isinstance(parsed, SendMessage)
    assert parsed.msg_id == "m-123"
    assert parsed.sender == "alice"
    assert parsed.recipient == "bob"
    assert parsed.opk_id == 42
    assert parsed.type == MsgType.SEND_MSG


def test_bundle_response_serialization() -> None:
    """Ensure BundleResponse serialization preserves requested metadata."""
    bundle_data = {
        "ik_ed_pub": base64.b64encode(b"a" * 32).decode("ascii"),
        "ik_dh_pub": base64.b64encode(b"b" * 32).decode("ascii"),
        "spk_pub": base64.b64encode(b"c" * 32).decode("ascii"),
        "spk_sig": base64.b64encode(b"d" * 64).decode("ascii"),
    }

    msg = BundleResponse(
        username="bob",
        bundle_json=str(bundle_data),
    )

    json_str = msg.to_json()
    parsed = BaseMessage.from_json(json_str)

    assert isinstance(parsed, BundleResponse)
    assert parsed.username == "bob"
    assert parsed.type == MsgType.BUNDLE_RESPONSE


def test_direct_ack_serialization() -> None:
    """Ensure DirectAck serialization roundtrip keeps acknowledgement fields."""
    ack = DirectAck(msg_id="abc", accepted=True, reason="ok")
    parsed = BaseMessage.from_json(ack.to_json())
    assert isinstance(parsed, DirectAck)
    assert parsed.msg_id == "abc"
    assert parsed.accepted is True


def test_register_and_auth_include_p2p_endpoint() -> None:
    """Ensure Register and ClientAuth preserve advertised P2P endpoint fields."""
    register = Register(
        username="alice",
        password_hash="hash",
        key_bundle_json="{}",
        p2p_host="127.0.0.1",
        p2p_port=4242,
    )
    parsed_register = BaseMessage.from_json(register.to_json())
    assert isinstance(parsed_register, Register)
    assert parsed_register.p2p_host == "127.0.0.1"
    assert parsed_register.p2p_port == 4242

    auth = ClientAuth(
        username="alice",
        password_hash="hash",
        p2p_host="127.0.0.1",
        p2p_port=4343,
    )
    parsed_auth = BaseMessage.from_json(auth.to_json())
    assert isinstance(parsed_auth, ClientAuth)
    assert parsed_auth.p2p_host == "127.0.0.1"
    assert parsed_auth.p2p_port == 4343


def test_handshake_messages_serialization() -> None:
    """Ensure handshake message classes serialize and parse correctly."""
    client_hello = ClientHello(
        ephemeral_pub_b64=base64.b64encode(b"client_eph" + b"\x00" * 22).decode("ascii"),
    )

    json_str = client_hello.to_json()
    parsed = BaseMessage.from_json(json_str)

    assert isinstance(parsed, ClientHello)
    assert parsed.type == MsgType.CLIENT_HELLO

    server_hello = ServerHello(
        ephemeral_pub_b64=base64.b64encode(b"server_eph" + b"\x00" * 22).decode("ascii"),
        ca_cert_pem="-----BEGIN CERTIFICATE-----\ntest\n-----END CERTIFICATE-----",
    )

    json_str = server_hello.to_json()
    parsed = BaseMessage.from_json(json_str)

    assert isinstance(parsed, ServerHello)
    assert parsed.type == MsgType.SERVER_HELLO


def test_offline_messages_empty_list() -> None:
    """Ensure OfflineMessages supports empty payload lists correctly."""
    msg = OfflineMessages(messages=[])

    json_str = msg.to_json()
    parsed = BaseMessage.from_json(json_str)

    assert isinstance(parsed, OfflineMessages)
    assert parsed.messages == []
    assert parsed.type == MsgType.OFFLINE_MESSAGES


def test_fetch_bundle_serialization() -> None:
    """Ensure FetchBundle serialization roundtrip preserves username."""
    msg = FetchBundle(username="alice")

    json_str = msg.to_json()
    parsed = BaseMessage.from_json(json_str)

    assert isinstance(parsed, FetchBundle)
    assert parsed.username == "alice"
    assert parsed.type == MsgType.FETCH_BUNDLE
