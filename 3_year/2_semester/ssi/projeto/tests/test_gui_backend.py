"""Contract tests for the GUI FastAPI backend."""
from __future__ import annotations

from fastapi.testclient import TestClient

from src.client.gui_backend import build_app


class FakeController:
    """Lightweight async controller stub for API contract validation."""

    def __init__(self):
        self.calls: list[str] = []
        self.last_payload: dict | None = None
        self.shutdown_called = False

    async def run_locked(self, coro):
        self.calls.append("run_locked")
        return await coro

    def state_snapshot(self):
        self.calls.append("state_snapshot")
        return {
            "connected": True,
            "authenticated": True,
            "username": "alice",
            "p2p_endpoint": {"host": "127.0.0.1", "port": 32001},
            "pending_invites": [],
        }

    async def connect(self):
        self.calls.append("connect")
        return self.state_snapshot()

    async def disconnect(self):
        self.calls.append("disconnect")
        return self.state_snapshot()

    async def register(self, username: str, password: str):
        self.calls.append("register")
        if username == "bad":
            raise ValueError("Registration failed")
        self.last_payload = {"username": username, "password": password}
        return self.state_snapshot()

    async def login(self, username: str, password: str):
        self.calls.append("login")
        self.last_payload = {"username": username, "password": password}
        return self.state_snapshot()

    def get_contacts(self):
        self.calls.append("get_contacts")
        return {"contacts": [{"username": "bob", "has_session": True}]}

    async def add_contact(self, username: str):
        self.calls.append("add_contact")
        if username == "missing":
            raise ValueError("User not found")
        return self.get_contacts()

    async def send_message(self, username: str, text: str):
        self.calls.append("send_message")
        self.last_payload = {"username": username, "text": text}
        return {"route": "server"}

    def get_history(self, username: str, limit: int = 50):
        self.calls.append("get_history")
        if username == "unknown":
            raise ValueError("Not logged in")
        return {
            "messages": [
                {
                    "peer": username,
                    "direction": "received",
                    "plaintext": "hello",
                    "timestamp": 1.0,
                }
            ]
        }

    async def fetch_offline(self):
        self.calls.append("fetch_offline")
        return {"total": 0, "processed": 0, "failed": 0}

    def list_groups(self):
        self.calls.append("list_groups")
        return {
            "groups": [
                {
                    "group_id": "g1",
                    "name": "project",
                    "members": ["alice", "bob"],
                    "member_count": 2,
                }
            ]
        }

    async def create_group(self, name: str):
        self.calls.append("create_group")
        return {"group_id": "g1", "name": name}

    def list_group_invites(self):
        self.calls.append("list_group_invites")
        return {"invites": []}

    async def accept_group_invite(self, group_id: str):
        self.calls.append("accept_group_invite")
        return {"group_id": group_id, "group_name": "project", "key_exchange_sent": True}

    def reject_group_invite(self, group_id: str):
        self.calls.append("reject_group_invite")
        if group_id == "missing":
            raise ValueError("No pending invite")
        return {"group_id": group_id, "group_name": "project"}

    def group_members(self, group_id: str):
        self.calls.append("group_members")
        return {"group_id": group_id, "name": "project", "members": ["alice", "bob"]}

    async def invite_group_member(self, group_id: str, username: str):
        self.calls.append("invite_group_member")
        return {"group_id": group_id, "username": username}

    async def remove_group_member(self, group_id: str, username: str):
        self.calls.append("remove_group_member")
        return {"group_id": group_id, "username": username, "failed_members": []}

    async def send_group_message(self, group_id: str, text: str):
        self.calls.append("send_group_message")
        return {"group_id": group_id, "sent_count": 1, "total_targets": 1, "failed_members": []}

    def get_group_history(self, group_id: str, limit: int = 50):
        self.calls.append("get_group_history")
        return {
            "group_id": group_id,
            "name": "project",
            "messages": [{"sender": "alice", "plaintext": "hello", "timestamp": 1.0}],
        }

    async def next_event(self, timeout: float = 25.0):
        self.calls.append("next_event")
        return {"type": "message", "sender": "bob", "text": "hello"}

    async def shutdown(self):
        self.calls.append("shutdown")
        self.shutdown_called = True


def build_test_client() -> tuple[TestClient, FakeController]:
    controller = FakeController()
    app = build_app(controller=controller)
    client = TestClient(app)
    return client, controller


def test_index_page_is_served():
    client, _ = build_test_client()
    response = client.get("/")
    assert response.status_code == 200
    assert "secureflow" in response.text.lower()


def test_state_and_connect_endpoints_use_controller():
    client, controller = build_test_client()

    state_response = client.get("/api/state")
    connect_response = client.post("/api/connect")

    assert state_response.status_code == 200
    assert state_response.json()["username"] == "alice"
    assert connect_response.status_code == 200
    assert "run_locked" in controller.calls
    assert "connect" in controller.calls


def test_register_error_is_mapped_to_http_400():
    client, _ = build_test_client()

    response = client.post(
        "/api/register",
        json={"username": "bad", "password": "password123"},
    )

    assert response.status_code == 400
    assert response.json()["detail"] == "Registration failed"


def test_messages_and_history_endpoints_roundtrip_payloads():
    client, controller = build_test_client()

    message_response = client.post(
        "/api/messages",
        json={"username": "bob", "text": "hi"},
    )
    history_response = client.get("/api/history/bob?limit=20")

    assert message_response.status_code == 200
    assert message_response.json()["route"] == "server"
    assert history_response.status_code == 200
    assert history_response.json()["messages"][0]["plaintext"] == "hello"
    assert controller.last_payload == {"username": "bob", "text": "hi"}


def test_groups_endpoints_expose_expected_shapes():
    client, _ = build_test_client()

    groups_response = client.get("/api/groups")
    create_response = client.post("/api/groups", json={"name": "project"})
    members_response = client.get("/api/groups/g1/members")
    group_message_response = client.post("/api/groups/g1/messages", json={"text": "hello group"})

    assert groups_response.status_code == 200
    assert groups_response.json()["groups"][0]["group_id"] == "g1"
    assert create_response.status_code == 200
    assert create_response.json()["group_id"] == "g1"
    assert members_response.status_code == 200
    assert members_response.json()["members"] == ["alice", "bob"]
    assert group_message_response.status_code == 200
    assert group_message_response.json()["sent_count"] == 1


def test_websocket_stream_delivers_events():
    client, _ = build_test_client()

    with client.websocket_connect("/ws/events") as ws:
        event = ws.receive_json()

    assert event["type"] == "message"
    assert event["sender"] == "bob"


def test_shutdown_hook_calls_controller_shutdown():
    client, controller = build_test_client()
    with client:
        response = client.get("/api/state")
        assert response.status_code == 200

    assert controller.shutdown_called is True
