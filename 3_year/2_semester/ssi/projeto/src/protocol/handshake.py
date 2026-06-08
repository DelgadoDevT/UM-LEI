"""Transport handshake helpers for deriving directional transport keys."""

from __future__ import annotations

import base64
import json
import socket
import time
from typing import Any

from src.crypto.asymmetric import (
    deserialize_ed25519_public,
    deserialize_x25519_public,
    generate_x25519_keypair,
    serialize_public_key,
    x25519_exchange,
)
from src.crypto.certificates import cert_to_pem
from src.crypto.kdf import hkdf_derive
from src.crypto.passwords import hash_password, verify_password
from src.rich_logging import log_security, log_warn

from .messages import AuthFail, AuthOK, ClientAuth, ClientHello, MsgType, Register, ServerHello
from .transport import recv_message, recv_raw, send_message, send_raw


def _derive_transport_keys(shared_secret: bytes, *, salt: bytes, info: bytes) -> tuple[bytes, bytes]:
    """Derive directional 32-byte transport keys from shared handshake secret."""
    material = hkdf_derive(
        input_key_material=shared_secret,
        length=64,
        salt=salt,
        info=info,
    )
    return material[:32], material[32:]


def client_handshake(sock: socket.socket) -> tuple[bytes, bytes]:
    """Execute client-side transport handshake and return send/recv transport keys."""
    client_priv, client_pub = generate_x25519_keypair()
    client_hello = {
        "type": MsgType.CLIENT_HELLO.value,
        "timestamp": time.time(),
        "client_eph_pub": base64.b64encode(serialize_public_key(client_pub)).decode("ascii"),
    }
    send_raw(sock, json.dumps(client_hello, separators=(",", ":")).encode("utf-8"))

    server_hello_raw = recv_raw(sock)
    server_hello = json.loads(server_hello_raw.decode("utf-8"))
    if server_hello.get("type") != MsgType.SERVER_HELLO.value:
        raise ConnectionError("Expected SERVER_HELLO")

    server_pub = deserialize_x25519_public(base64.b64decode(server_hello["server_eph_pub"]))
    shared = x25519_exchange(client_priv, server_pub)
    return _derive_transport_keys(shared, salt=b"\x00" * 32, info=b"TransportKey")


def _server_handshake_sync(sock: socket.socket, ca_private_key, ca_cert) -> tuple[bytes, bytes, str | None]:
    """Execute legacy server-side transport handshake for socket-based tests."""
    del ca_private_key
    del ca_cert

    client_hello_raw = recv_raw(sock)
    client_hello = json.loads(client_hello_raw.decode("utf-8"))
    if client_hello.get("type") != MsgType.CLIENT_HELLO.value:
        raise ConnectionError("Expected CLIENT_HELLO")

    client_pub = deserialize_x25519_public(base64.b64decode(client_hello["client_eph_pub"]))
    server_priv, server_pub = generate_x25519_keypair()

    server_hello = {
        "type": MsgType.SERVER_HELLO.value,
        "timestamp": time.time(),
        "server_eph_pub": base64.b64encode(serialize_public_key(server_pub)).decode("ascii"),
    }
    send_raw(sock, json.dumps(server_hello, separators=(",", ":")).encode("utf-8"))

    shared = x25519_exchange(server_priv, client_pub)
    send_key, recv_key = _derive_transport_keys(shared, salt=b"\x00" * 32, info=b"TransportKey")
    return recv_key, send_key, None


async def _server_handshake_async(reader, writer, db, ca) -> tuple[bytes | None, bytes | None, str | None]:
    """Execute async server-side handshake with registration/authentication flow."""
    try:
        client_hello_msg = await recv_message(reader, None)
        if not isinstance(client_hello_msg, ClientHello):
            raise ConnectionError("Handshake failed: expected ClientHello")

        client_eph_pub = deserialize_x25519_public(base64.b64decode(client_hello_msg.ephemeral_pub_b64))
        server_eph_priv, server_eph_pub = generate_x25519_keypair()

        shared_secret = x25519_exchange(server_eph_priv, client_eph_pub)
        transcript = client_hello_msg.ephemeral_pub_b64.encode("utf-8") + serialize_public_key(server_eph_pub)
        key_c2s, key_s2c = _derive_transport_keys(shared_secret, salt=b"", info=transcript)

        server_hello_msg = ServerHello(
            ephemeral_pub_b64=base64.b64encode(serialize_public_key(server_eph_pub)).decode("ascii"),
            ca_cert_pem=ca.get_ca_cert_pem().decode(),
        )
        await send_message(writer, None, server_hello_msg)

        auth_msg = await recv_message(reader, key_c2s)

        if isinstance(auth_msg, Register):
            if db.get_user_hash(auth_msg.username):
                await send_message(writer, key_s2c, AuthFail())
                return None, None, None

            password_secret = auth_msg.password or auth_msg.password_hash
            if not password_secret:
                await send_message(writer, key_s2c, AuthFail())
                return None, None, None

            try:
                bundle = json.loads(auth_msg.key_bundle_json)
                ik_ed_pub_bytes = base64.b64decode(bundle["ik_ed_pub"])
                db.store_user(
                    username=auth_msg.username,
                    pw_hash=hash_password(password_secret),
                    ik_ed_pub=ik_ed_pub_bytes,
                    ik_dh_pub=base64.b64decode(bundle["ik_dh_pub"]),
                    spk_pub=base64.b64decode(bundle["spk_pub"]),
                    spk_sig=base64.b64decode(bundle["spk_sig"]),
                    p2p_host=auth_msg.p2p_host or "",
                    p2p_port=auth_msg.p2p_port or 0,
                )

                user_cert = ca.issue_user_cert(deserialize_ed25519_public(ik_ed_pub_bytes), auth_msg.username)
                db.store_certificate(auth_msg.username, cert_to_pem(user_cert).decode())
            except Exception:
                # Keep client UX deterministic: auth/register failures should surface as AuthFail.
                await send_message(writer, key_s2c, AuthFail())
                raise

            await send_message(writer, key_s2c, AuthOK())
            log_security("auth.register", "User registered and certificate issued", user=auth_msg.username)
            return key_c2s, key_s2c, auth_msg.username

        if isinstance(auth_msg, ClientAuth):
            stored_hash = db.get_user_hash(auth_msg.username)
            password_secret = auth_msg.password or auth_msg.password_hash
            if not stored_hash or not password_secret or not verify_password(password_secret, stored_hash):
                await send_message(writer, key_s2c, AuthFail())
                return None, None, None

            db.update_user_endpoint(
                auth_msg.username,
                auth_msg.p2p_host or "",
                auth_msg.p2p_port or 0,
            )

            await send_message(writer, key_s2c, AuthOK())
            log_security("auth.login", "User login successful", user=auth_msg.username)
            return key_c2s, key_s2c, auth_msg.username

        raise ConnectionError("Handshake failed: expected Register or ClientAuth")

    except Exception as exc:
        log_warn(
            "handshake.failed",
            "Handshake/authentication failed",
            peer=writer.get_extra_info("peername"),
            error=type(exc).__name__,
            detail=str(exc),
        )
        writer.close()
        await writer.wait_closed()
        return None, None, None


def server_handshake(*args: Any, **kwargs: Any):
    """Dispatch to sync or async server handshake based on the first argument type."""
    if args and isinstance(args[0], socket.socket):
        return _server_handshake_sync(*args, **kwargs)
    return _server_handshake_async(*args, **kwargs)
