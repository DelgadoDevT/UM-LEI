from __future__ import annotations

import asyncio
import base64

import pytest

from src.client.client import ChatClient
from src.protocol.messages import DirectAck, SendMessage
from src.protocol.transport import recv_message, send_message

pytestmark = pytest.mark.asyncio(loop_scope="function")


async def _send_and_receive_ack(port: int, message: SendMessage | DirectAck) -> DirectAck:
    reader, writer = await asyncio.open_connection("127.0.0.1", port)
    try:
        await send_message(writer, None, message)
        ack = await recv_message(reader, None)
        assert isinstance(ack, DirectAck)
        return ack
    finally:
        writer.close()
        await writer.wait_closed()


@pytest.mark.parametrize(
    ("msg_id", "ciphertext_b64"),
    [
        ("", base64.b64encode(b'{"ciphertext":"demo"}').decode()),
        ("missing-payload", ""),
    ],
    ids=["missing-msg-id", "missing-payload"],
)
async def test_direct_listener_rejects_invalid_payload_fields(msg_id: str, ciphertext_b64: str):
    client = ChatClient()
    client.username = "bob"

    async def _handler(_):
        return True

    client.set_message_handler(_handler)
    await client.ensure_direct_listener(host="127.0.0.1", port=0)
    _, port = client.get_advertised_endpoint()

    try:
        ack = await _send_and_receive_ack(
            port,
            SendMessage(
                msg_id=msg_id,
                sender="alice",
                recipient="bob",
                ciphertext_b64=ciphertext_b64,
            ),
        )
        assert ack.msg_id == msg_id
        assert ack.accepted is False
        assert ack.reason == "invalid_payload"
    finally:
        await client.disconnect()


async def test_direct_listener_rejects_when_no_handler_configured():
    client = ChatClient()
    client.username = "bob"
    await client.ensure_direct_listener(host="127.0.0.1", port=0)
    _, port = client.get_advertised_endpoint()

    try:
        ack = await _send_and_receive_ack(
            port,
            SendMessage(
                msg_id="no-handler",
                sender="alice",
                recipient="bob",
                ciphertext_b64=base64.b64encode(b'{"ciphertext":"demo"}').decode(),
            ),
        )
        assert ack.msg_id == "no-handler"
        assert ack.accepted is False
        assert ack.reason == "no_handler"
    finally:
        await client.disconnect()


async def test_direct_listener_rejects_wrong_message_type():
    client = ChatClient()
    client.username = "bob"
    await client.ensure_direct_listener(host="127.0.0.1", port=0)
    _, port = client.get_advertised_endpoint()

    try:
        ack = await _send_and_receive_ack(
            port,
            DirectAck(msg_id="not-a-send", accepted=True, reason="ok"),
        )
        assert ack.accepted is False
        assert ack.reason == "invalid_type"
    finally:
        await client.disconnect()


async def test_send_chat_message_direct_returns_false_on_negative_ack():
    got_message = asyncio.Event()

    async def negative_ack_handler(reader: asyncio.StreamReader, writer: asyncio.StreamWriter):
        try:
            msg = await recv_message(reader, None)
            assert isinstance(msg, SendMessage)
            got_message.set()
            await send_message(
                writer,
                None,
                DirectAck(msg_id=msg.msg_id, accepted=False, reason="rejected"),
            )
        finally:
            writer.close()
            await writer.wait_closed()

    server = await asyncio.start_server(negative_ack_handler, "127.0.0.1", 0)
    port = server.sockets[0].getsockname()[1]

    client = ChatClient()
    try:
        success = await client.send_chat_message_direct(
            peer_host="127.0.0.1",
            peer_port=port,
            recipient="bob",
            sender="alice",
            ciphertext_b64=base64.b64encode(b'{"ciphertext":"demo"}').decode(),
            msg_id="negative-ack",
            timeout=1.0,
        )

        assert got_message.is_set()
        assert success is False
    finally:
        server.close()
        await server.wait_closed()
        await client.disconnect()
