from __future__ import annotations

import base64
import json
from types import SimpleNamespace

import pytest
from rich.table import Table

from src.client.cli import ChatCLI


@pytest.fixture
def cli(tmp_path):
    instance = ChatCLI(data_dir=str(tmp_path))
    instance.client.username = "alice"
    return instance


@pytest.mark.asyncio
async def test_group_command_without_args_reports_usage(cli):
    errors: list[str] = []
    cli._print_error = lambda msg: errors.append(msg)  # type: ignore[method-assign]

    await cli._cmd_group([])

    assert errors == [
        "Usage: /group <create|list|members|invite|remove|accept|reject|invites|msg|history> ..."
    ]


@pytest.mark.asyncio
async def test_group_command_unknown_subcommand_reports_error(cli):
    errors: list[str] = []
    cli._print_error = lambda msg: errors.append(msg)  # type: ignore[method-assign]

    await cli._cmd_group(["unknown"])

    assert errors == ["Unknown group command: unknown"]


@pytest.mark.asyncio
async def test_group_create_usage_error(cli):
    errors: list[str] = []
    cli._print_error = lambda msg: errors.append(msg)  # type: ignore[method-assign]

    await cli._cmd_group(["create"])

    assert errors == ["Usage: /group create <name>"]


@pytest.mark.asyncio
async def test_group_create_success_path(cli):
    calls: list[str] = []
    states: list[tuple[str, str, str]] = []

    class StubGroupManager:
        def create_group(self, name: str) -> str:
            calls.append(name)
            return "g-1234"

    cli.group_manager = StubGroupManager()  # type: ignore[assignment]
    cli._print_state = lambda ctx, msg, level="info": states.append((ctx, msg, level))  # type: ignore[method-assign]

    await cli._cmd_group(["create", "study"])

    assert calls == ["study"]
    assert states == [("Group", "Created 'study' with ID g-1234", "success")]


@pytest.mark.asyncio
async def test_group_invites_when_empty(cli):
    states: list[tuple[str, str, str]] = []
    printed: list[object] = []
    cli._print_state = lambda ctx, msg, level="info": states.append((ctx, msg, level))  # type: ignore[method-assign]
    cli.console.print = lambda obj, *args, **kwargs: printed.append(obj)  # type: ignore[method-assign]

    await cli._cmd_group(["invites"])

    assert printed == []
    assert states == [("Group invites", "No pending invites", "info")]


@pytest.mark.asyncio
async def test_group_invites_with_pending_entries(cli):
    cli._pending_invites = {
        "g1": {"group_name": "Study Group", "inviter": "bob", "sender_key_b64": "x"},
        "g2": {"group_name": "Ops Team", "inviter": "carol", "sender_key_b64": "y"},
    }
    states: list[tuple[str, str, str]] = []
    printed: list[object] = []
    cli._print_state = lambda ctx, msg, level="info": states.append((ctx, msg, level))  # type: ignore[method-assign]
    cli.console.print = lambda obj, *args, **kwargs: printed.append(obj)  # type: ignore[method-assign]

    await cli._cmd_group(["invites"])

    assert len(printed) == 1
    table = printed[0]
    assert isinstance(table, Table)
    assert table.columns[0]._cells == ["g1", "g2"]  # type: ignore[attr-defined]
    assert table.columns[1]._cells == ["Study Group", "Ops Team"]  # type: ignore[attr-defined]
    assert table.columns[2]._cells == ["bob", "carol"]  # type: ignore[attr-defined]
    assert states == [("Group invites", "Use /group accept <id> or /group reject <id>", "info")]


@pytest.mark.asyncio
async def test_group_reject_missing_invite_error(cli):
    errors: list[str] = []
    cli._print_error = lambda msg: errors.append(msg)  # type: ignore[method-assign]

    await cli._cmd_group(["reject", "g-missing"])

    assert errors == ["No pending invite for group g-missing"]


@pytest.mark.asyncio
async def test_group_reject_success_removes_pending_invite(cli):
    cli._pending_invites = {
        "g1": {"group_name": "Study Group", "inviter": "bob", "sender_key_b64": "unused"}
    }
    states: list[tuple[str, str, str]] = []
    cli._print_state = lambda ctx, msg, level="info": states.append((ctx, msg, level))  # type: ignore[method-assign]

    await cli._cmd_group(["reject", "g1"])

    assert "g1" not in cli._pending_invites
    assert states == [("Group invite", "Rejected invite for 'Study Group'", "info")]


@pytest.mark.asyncio
async def test_group_accept_missing_invite_error(cli):
    errors: list[str] = []
    cli._print_error = lambda msg: errors.append(msg)  # type: ignore[method-assign]

    await cli._cmd_group(["accept", "g-missing"])

    assert errors == ["No pending invite for group g-missing"]


@pytest.mark.asyncio
async def test_group_accept_success_removes_pending_and_sends_key(cli):
    inviter_sender_key = b"inviter-sender-key"
    my_sender_key = b"my-sender-key"
    cli._pending_invites = {
        "g1": {
            "group_name": "Study Group",
            "inviter": "bob",
            "sender_key_b64": base64.b64encode(inviter_sender_key).decode(),
        }
    }
    states: list[tuple[str, str, str]] = []
    warnings: list[str] = []
    send_calls: list[tuple[str, str]] = []

    class StubGroupManager:
        def __init__(self):
            self.join_calls: list[tuple[str, str, str]] = []
            self.receive_calls: list[tuple[str, str, bytes]] = []

        def join_group(self, group_id: str, name: str, creator: str) -> bytes:
            self.join_calls.append((group_id, name, creator))
            return my_sender_key

        def receive_sender_key(self, group_id: str, username: str, sender_key: bytes) -> None:
            self.receive_calls.append((group_id, username, sender_key))

    stub_group_manager = StubGroupManager()
    cli.group_manager = stub_group_manager  # type: ignore[assignment]
    cli._print_state = lambda ctx, msg, level="info": states.append((ctx, msg, level))  # type: ignore[method-assign]
    cli._print_warning = lambda msg: warnings.append(msg)  # type: ignore[method-assign]

    async def fake_send_encrypted_to(username: str, text: str) -> bool:
        send_calls.append((username, text))
        return True

    cli._send_encrypted_to = fake_send_encrypted_to  # type: ignore[method-assign]

    await cli._cmd_group(["accept", "g1"])

    assert "g1" not in cli._pending_invites
    assert stub_group_manager.join_calls == [("g1", "Study Group", "bob")]
    assert stub_group_manager.receive_calls == [("g1", "bob", inviter_sender_key)]
    assert len(send_calls) == 1
    assert send_calls[0][0] == "bob"
    payload = json.loads(send_calls[0][1])
    assert payload["type"] == "group_sender_key"
    assert payload["group_id"] == "g1"
    assert payload["sender_key_b64"] == base64.b64encode(my_sender_key).decode()
    assert warnings == []
    assert ("Group", "Joined 'Study Group'", "success") in states
    assert ("Group key exchange", "Shared your sender key with bob", "info") in states


@pytest.mark.asyncio
async def test_group_members_missing_group_error(cli):
    cli.db = SimpleNamespace(get_group=lambda _group_id: None)  # type: ignore[assignment]
    errors: list[str] = []
    cli._print_error = lambda msg: errors.append(msg)  # type: ignore[method-assign]

    await cli._cmd_group(["members", "g-missing"])

    assert errors == ["Group g-missing not found"]


@pytest.mark.asyncio
async def test_group_members_success_table_render(cli):
    cli.db = SimpleNamespace(get_group=lambda _group_id: {"name": "Study Group"})  # type: ignore[assignment]
    cli.group_manager = SimpleNamespace(get_group_members=lambda _group_id: ["alice", "bob"])  # type: ignore[assignment]
    printed: list[object] = []
    cli.console.print = lambda obj, *args, **kwargs: printed.append(obj)  # type: ignore[method-assign]

    await cli._cmd_group(["members", "g1"])

    assert len(printed) == 1
    table = printed[0]
    assert isinstance(table, Table)
    assert table.title == "Members of 'Study Group'"
    assert table.columns[0]._cells == ["alice", "bob"]  # type: ignore[attr-defined]


@pytest.mark.asyncio
async def test_group_history_missing_group_error(cli):
    cli.db = SimpleNamespace(get_group=lambda _group_id: None)  # type: ignore[assignment]
    errors: list[str] = []
    cli._print_error = lambda msg: errors.append(msg)  # type: ignore[method-assign]

    await cli._cmd_group(["history", "g-missing"])

    assert errors == ["Group g-missing not found"]


@pytest.mark.asyncio
async def test_group_history_no_messages_path(cli):
    cli.db = SimpleNamespace(
        get_group=lambda _group_id: {"name": "Study Group"},
        get_group_messages=lambda _group_id, limit=20: [],
    )  # type: ignore[assignment]
    infos: list[str] = []
    cli._print_info = lambda msg: infos.append(msg)  # type: ignore[method-assign]

    await cli._cmd_group(["history", "g1"])

    assert infos == ["No messages in group 'Study Group'"]


@pytest.mark.parametrize(
    ("group", "username_to_remove", "members", "expected_error"),
    [
        ({"name": "Study Group", "creator": "bob"}, "carol", ["alice", "carol"], "Only the group creator can remove members"),
        ({"name": "Study Group", "creator": "alice"}, "alice", ["alice", "bob"], "Creator self-removal is not supported"),
        ({"name": "Study Group", "creator": "alice"}, "carol", ["alice", "bob"], "User 'carol' is not a member of this group"),
    ],
)
@pytest.mark.asyncio
async def test_group_remove_guardrails(cli, group, username_to_remove, members, expected_error):
    errors: list[str] = []

    class StubGroupManager:
        def __init__(self):
            self.remove_called = False
            self.rotate_called = False

        def get_group_members(self, _group_id: str) -> list[str]:
            return members

        def remove_member(self, _group_id: str, _username: str) -> None:
            self.remove_called = True

        def rotate_own_sender_key(self, _group_id: str) -> bytes:
            self.rotate_called = True
            return b"x" * 32

    stub_group_manager = StubGroupManager()
    cli.db = SimpleNamespace(get_group=lambda _group_id: group)  # type: ignore[assignment]
    cli.group_manager = stub_group_manager  # type: ignore[assignment]
    cli._print_error = lambda msg: errors.append(msg)  # type: ignore[method-assign]

    await cli._cmd_group(["remove", "g1", username_to_remove])

    assert errors == [expected_error]
    assert stub_group_manager.remove_called is False
    assert stub_group_manager.rotate_called is False
