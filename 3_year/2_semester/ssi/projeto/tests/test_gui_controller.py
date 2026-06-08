"""Unit tests for GUI controller orchestration helpers."""
from __future__ import annotations

import tempfile
import shutil

import pytest

from src.client.gui_controller import GUIController


class TestGUIController:
    """Coverage for GUI controller state and invite behavior."""

    def test_state_snapshot_defaults_when_disconnected(self):
        controller = GUIController(data_dir=tempfile.mkdtemp())
        try:
            state = controller.state_snapshot()
            assert state["connected"] is False
            assert state["authenticated"] is False
            assert state["username"] is None
            assert state["pending_invites"] == []
            assert state["p2p_endpoint"]["port"] == 0
        finally:
            shutil.rmtree(controller.data_dir)

    def test_preferred_p2p_port_is_stable(self):
        controller = GUIController(data_dir=tempfile.mkdtemp())
        try:
            alice_first = controller._preferred_p2p_port("alice")
            alice_second = controller._preferred_p2p_port("alice")
            bob_port = controller._preferred_p2p_port("bob")

            assert alice_first == alice_second
            assert 30000 <= alice_first < 45000
            assert alice_first != bob_port
        finally:
            shutil.rmtree(controller.data_dir)

    def test_list_group_invites_uses_pending_map(self):
        controller = GUIController(data_dir=tempfile.mkdtemp())
        try:
            controller._pending_invites["g1"] = {
                "inviter": "bob",
                "group_name": "project",
                "sender_key_b64": "abc",
            }

            invites = controller.list_group_invites()
            assert invites["invites"] == [
                {
                    "group_id": "g1",
                    "group_name": "project",
                    "inviter": "bob",
                }
            ]
        finally:
            shutil.rmtree(controller.data_dir)

    def test_reject_group_invite_missing_raises(self):
        controller = GUIController(data_dir=tempfile.mkdtemp())
        try:
            with pytest.raises(ValueError, match="No pending invite"):
                controller.reject_group_invite("missing")
        finally:
            shutil.rmtree(controller.data_dir)

    def test_get_contacts_returns_empty_when_not_authenticated(self):
        controller = GUIController(data_dir=tempfile.mkdtemp())
        try:
            assert controller.get_contacts() == {"contacts": []}
        finally:
            shutil.rmtree(controller.data_dir)
