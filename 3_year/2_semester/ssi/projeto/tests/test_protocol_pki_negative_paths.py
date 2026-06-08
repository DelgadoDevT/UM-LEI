from __future__ import annotations

import base64

import pytest

from src.client.contact_manager import ContactManager
from src.crypto.asymmetric import (
    generate_ed25519_keypair,
    generate_x25519_keypair,
    serialize_public_key,
)
from src.crypto.certificates import (
    cert_to_pem,
    create_ca_certificate,
    issue_user_certificate,
)
from src.crypto.passwords import hash_password
from src.protocol import handshake as handshake_mod
from src.protocol.messages import AuthFail, AuthOK, ClientAuth, ClientHello, Register, ServerHello


class FakeWriter:
    def __init__(self) -> None:
        self.closed = False
        self.wait_closed_called = False

    def get_extra_info(self, _name: str):
        return ("127.0.0.1", 9999)

    def close(self) -> None:
        self.closed = True

    async def wait_closed(self) -> None:
        self.wait_closed_called = True


class FakeCA:
    def get_ca_cert_pem(self) -> bytes:
        return b"-----BEGIN CERTIFICATE-----\nFAKE\n-----END CERTIFICATE-----"


class FakeHandshakeDB:
    def __init__(self, stored_hash: str | None = None) -> None:
        self.stored_hash = stored_hash

    def get_user_hash(self, _username: str) -> str | None:
        return self.stored_hash

    def update_user_endpoint(self, *_args, **_kwargs) -> None:
        return None


def _valid_client_hello() -> ClientHello:
    _, client_pub = generate_x25519_keypair()
    return ClientHello(ephemeral_pub_b64=base64.b64encode(serialize_public_key(client_pub)).decode("ascii"))


async def _run_handshake(monkeypatch: pytest.MonkeyPatch, incoming: list, db: FakeHandshakeDB):
    sent_messages: list[tuple[bytes | None, object]] = []
    queue = iter(incoming)

    async def fake_recv_message(_channel, _transport_key):
        return next(queue)

    async def fake_send_message(_writer, transport_key, msg):
        sent_messages.append((transport_key, msg))

    monkeypatch.setattr(handshake_mod, "recv_message", fake_recv_message)
    monkeypatch.setattr(handshake_mod, "send_message", fake_send_message)

    writer = FakeWriter()
    result = await handshake_mod._server_handshake_async(
        reader=object(),
        writer=writer,
        db=db,
        ca=FakeCA(),
    )
    return result, writer, sent_messages


@pytest.mark.asyncio
async def test_server_handshake_rejects_unexpected_auth_message_type(monkeypatch: pytest.MonkeyPatch) -> None:
    result, writer, sent = await _run_handshake(
        monkeypatch,
        [_valid_client_hello(), AuthOK()],
        FakeHandshakeDB(),
    )

    assert result == (None, None, None)
    assert writer.closed
    assert writer.wait_closed_called
    assert any(isinstance(msg, ServerHello) for _, msg in sent)


@pytest.mark.asyncio
async def test_server_handshake_register_with_malformed_bundle_sends_authfail(monkeypatch: pytest.MonkeyPatch) -> None:
    malformed_register = Register(
        username="alice",
        password="pw123",
        key_bundle_json="{not-valid-json",
    )

    result, writer, sent = await _run_handshake(
        monkeypatch,
        [_valid_client_hello(), malformed_register],
        FakeHandshakeDB(),
    )

    assert result == (None, None, None)
    assert writer.closed
    assert writer.wait_closed_called
    assert any(isinstance(msg, AuthFail) for _, msg in sent)


@pytest.mark.asyncio
async def test_server_handshake_clientauth_invalid_password_sends_authfail(monkeypatch: pytest.MonkeyPatch) -> None:
    auth = ClientAuth(username="alice", password="wrong-password")

    result, writer, sent = await _run_handshake(
        monkeypatch,
        [_valid_client_hello(), auth],
        FakeHandshakeDB(stored_hash=hash_password("correct-password")),
    )

    assert result == (None, None, None)
    assert not writer.closed
    assert any(isinstance(msg, AuthFail) for _, msg in sent)


class RecordingContactDB:
    def __init__(self) -> None:
        self.saved = False

    def save_contact(self, **_kwargs) -> None:
        self.saved = True


def test_add_contact_verify_rejects_certificate_cn_mismatch() -> None:
    ca_priv, _ = generate_ed25519_keypair()
    ca_cert = create_ca_certificate(ca_priv, "TestCA")

    _, user_ed_pub = generate_ed25519_keypair()
    bad_cert = issue_user_certificate(ca_priv, ca_cert, user_ed_pub, "mallory")

    _, ik_dh_pub = generate_x25519_keypair()
    _, spk_pub = generate_x25519_keypair()

    db = RecordingContactDB()
    manager = ContactManager(db, ca_cert_pem=cert_to_pem(ca_cert).decode())

    bundle = {
        "ik_ed_pub": base64.b64encode(serialize_public_key(user_ed_pub)).decode("ascii"),
        "ik_dh_pub": base64.b64encode(serialize_public_key(ik_dh_pub)).decode("ascii"),
        "spk_pub": base64.b64encode(serialize_public_key(spk_pub)).decode("ascii"),
        "spk_sig": base64.b64encode(b"s" * 64).decode("ascii"),
        "cert_pem": cert_to_pem(bad_cert).decode(),
    }

    with pytest.raises(ValueError, match="doesn't match username"):
        manager.add_contact("alice", bundle, verify=True)

    assert db.saved is False
