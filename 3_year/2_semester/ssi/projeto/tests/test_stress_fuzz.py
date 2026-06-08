"""Deterministic stress/fuzz-style robustness tests."""

from __future__ import annotations

import base64
import socket

import pytest

from src.client.cli import ChatCLI
from src.client.group_manager import GroupManager
from src.protocol.messages import DeliverMessage, SendMessage
from src.protocol.transport import recv_message, send_message
from src.storage.client_db import ClientDB


@pytest.mark.asyncio
async def test_process_delivered_message_malformed_payload_fuzz_samples(tmp_path) -> None:
    """Malformed payload_json samples should never crash message processing."""
    cli = ChatCLI(data_dir=str(tmp_path))
    cli.client.username = "alice"
    cli._print_error = lambda _msg: None  # type: ignore[method-assign]

    samples: list[object] = [
        "",
        " ",
        "{",
        "}",
        "[]",
        "null",
        "1234",
        '"text"',
        '{"sender":"bob"}',
        '{"sender":"bob","recipient":"alice"}',
        '{"sender":"bob","recipient":"alice","msg_id":""}',
        '{"sender":null,"recipient":"alice","msg_id":"m0"}',
        '{"sender":"bob","recipient":["alice"],"msg_id":"m1"}',
        '{"sender":"bob","recipient":"eve","msg_id":"m2"}',
        '{"sender":"bob","recipient":"alice","msg_id":"m3","ciphertext_b64":"!!!"}',
        '{"sender":"bob","recipient":"alice","msg_id":"m4","ciphertext_b64":"bm90LWpzb24="}',
        b"\xff\xfe",
        1234,
        None,
        object(),
    ]
    for i in range(40):
        fuzz_bytes = bytes(((i * 13 + j * 37) % 256 for j in range(16)))
        samples.append(fuzz_bytes.decode("latin1"))

    for sample in samples:
        msg = DeliverMessage(payload_json=sample)  # type: ignore[arg-type]
        result = await cli._process_delivered_message(msg)
        assert isinstance(result, bool)


def test_group_manager_decrypt_message_malformed_payloads_keep_state_stable(tmp_path) -> None:
    """Malformed group payload variants should fail cleanly without state corruption."""
    db_alice = ClientDB(str(tmp_path / "alice.db"), b"x" * 32)
    db_bob = ClientDB(str(tmp_path / "bob.db"), b"x" * 32)
    gm_alice = GroupManager(db_alice, "alice")
    gm_bob = GroupManager(db_bob, "bob")

    group_id = gm_alice.create_group("Stress Group")
    bob_sender_key = gm_bob.join_group(group_id, "Stress Group", "alice")
    alice_sender_key = gm_alice.get_my_sender_key(group_id)
    gm_alice.add_member(group_id, "bob", bob_sender_key)
    gm_bob.receive_sender_key(group_id, "alice", alice_sender_key)

    valid_payload = gm_alice.encrypt_message(group_id, "baseline")
    state_key = (group_id, "alice")
    baseline_state = gm_bob._member_states[state_key]
    baseline_index = baseline_state.message_index
    baseline_chain_key = baseline_state.chain_key

    malformed_payloads: list[dict] = []
    for field in ("group_id", "sender", "message_index", "nonce", "ciphertext"):
        payload = dict(valid_payload)
        payload.pop(field, None)
        malformed_payloads.append(payload)

    malformed_payloads.extend(
        [
            dict(valid_payload, message_index="0"),
            dict(valid_payload, sender="bob"),
            dict(valid_payload, sender="mallory"),
            dict(valid_payload, nonce="!!!"),
            dict(valid_payload, nonce=123),
            dict(valid_payload, ciphertext="!!!"),
            dict(valid_payload, ciphertext=None),
        ]
    )

    for payload in malformed_payloads:
        with pytest.raises(Exception):
            gm_bob.decrypt_message(payload)

        state = gm_bob._member_states[state_key]
        assert state.message_index == baseline_index
        assert state.chain_key == baseline_chain_key

    assert gm_bob.decrypt_message(valid_payload) == "baseline"


@pytest.mark.parametrize("transport_key", [None, b"k" * 32])
def test_transport_high_volume_socketpair_roundtrip_is_stable(transport_key: bytes | None) -> None:
    """Many framed socket roundtrips should remain stable and deterministic."""
    left, right = socket.socketpair()
    left.settimeout(2.0)
    right.settimeout(2.0)

    try:
        for i in range(300):
            ciphertext_b64 = base64.b64encode(f"payload-{i:04d}".encode("utf-8")).decode("ascii")
            outgoing = SendMessage(
                msg_id=f"msg-{i:04d}",
                sender="alice",
                recipient="bob",
                ciphertext_b64=ciphertext_b64,
                ephemeral_pub_b64="",
                opk_id=i,
            )

            send_message(left, transport_key, outgoing)
            incoming = recv_message(right, transport_key)

            assert isinstance(incoming, SendMessage)
            assert incoming.msg_id == outgoing.msg_id
            assert incoming.sender == "alice"
            assert incoming.recipient == "bob"
            assert incoming.ciphertext_b64 == ciphertext_b64
            assert incoming.opk_id == i
    finally:
        left.close()
        right.close()
