"""Length-prefixed transport framing and encrypted message exchange."""

from __future__ import annotations

import base64
import json
import socket
import struct

from src.crypto.primitives import aes_gcm_decrypt, aes_gcm_encrypt

from .messages import BaseMessage


def _recv_exact(sock: socket.socket, n: int) -> bytes:
    """Receive exactly n bytes or raise ConnectionError on EOF."""
    chunks: list[bytes] = []
    remaining = n
    while remaining > 0:
        chunk = sock.recv(remaining)
        if not chunk:
            raise ConnectionError("Socket closed while receiving data")
        chunks.append(chunk)
        remaining -= len(chunk)
    return b"".join(chunks)


def send_raw(sock: socket.socket, data: bytes) -> None:
    """Send raw payload with 4-byte big-endian length prefix."""
    header = struct.pack("!I", len(data))
    sock.sendall(header + data)


def recv_raw(sock: socket.socket) -> bytes:
    """Receive a length-prefixed raw payload."""
    length = struct.unpack("!I", _recv_exact(sock, 4))[0]
    return _recv_exact(sock, length)


def _pack_payload(transport_key: bytes | None, payload: bytes) -> bytes:
    """Encrypt payload into transport envelope when a key is present."""
    if not transport_key:
        return payload

    nonce, ciphertext = aes_gcm_encrypt(transport_key, payload, aad=b"transport-v1")
    envelope = {
        "nonce": base64.b64encode(nonce).decode("ascii"),
        "ciphertext": base64.b64encode(ciphertext).decode("ascii"),
    }
    return json.dumps(envelope, separators=(",", ":")).encode("utf-8")


def _unpack_payload(transport_key: bytes | None, raw: bytes) -> bytes:
    """Decrypt transport envelope payload when a key is present."""
    if not transport_key:
        return raw

    envelope = json.loads(raw.decode("utf-8"))
    nonce = base64.b64decode(envelope["nonce"])
    ciphertext = base64.b64decode(envelope["ciphertext"])
    return aes_gcm_decrypt(transport_key, nonce, ciphertext, aad=b"transport-v1")


def send_message(channel, transport_key: bytes | None, msg: BaseMessage):
    """Send framed protocol message on a socket or asyncio StreamWriter."""
    payload = _pack_payload(transport_key, msg.to_json().encode("utf-8"))

    if isinstance(channel, socket.socket):
        send_raw(channel, payload)
        return None

    async def _send_async() -> None:
        length_prefix = struct.pack("!I", len(payload))
        channel.write(length_prefix)
        channel.write(payload)
        await channel.drain()

    return _send_async()


def recv_message(channel, transport_key: bytes | None):
    """Receive and deserialize framed protocol message from socket or StreamReader."""
    if isinstance(channel, socket.socket):
        raw = recv_raw(channel)
        plaintext = _unpack_payload(transport_key, raw)
        return BaseMessage.from_json(plaintext.decode("utf-8"))

    async def _recv_async() -> BaseMessage:
        length_prefix = await channel.readexactly(4)
        payload_length = struct.unpack("!I", length_prefix)[0]
        raw = await channel.readexactly(payload_length)
        plaintext = _unpack_payload(transport_key, raw)
        return BaseMessage.from_json(plaintext.decode("utf-8"))

    return _recv_async()
