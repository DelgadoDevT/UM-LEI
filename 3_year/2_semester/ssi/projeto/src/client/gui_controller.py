"""Stateful async controller used by the desktop GUI backend.

The controller reuses existing client/crypto managers and emits structured
runtime events for the UI.
"""
from __future__ import annotations

import asyncio
import base64
import json
import os
import time
from datetime import datetime
from typing import Any, Awaitable, TypeVar

T = TypeVar("T")

from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives.kdf.pbkdf2 import PBKDF2HMAC

import config

from src.client.app_service import ClientAppService
from src.client.client import ChatClient
from src.client.contact_manager import ContactManager
from src.client.group_manager import GroupManager
from src.client.key_manager import KeyManager
from src.client.session_manager import SessionManager
from src.protocol.messages import BaseMessage, DeliverMessage, OfflineMessages
from src.storage.client_db import ClientDB


class GUIController:
    """High-level application controller for GUI clients."""

    def __init__(self, data_dir: str = "client_data"):
        self.data_dir = data_dir
        self.client = ChatClient()

        self.key_manager: KeyManager | None = None
        self.session_manager: SessionManager | None = None
        self.contact_manager: ContactManager | None = None
        self.group_manager: GroupManager | None = None
        self.db: ClientDB | None = None

        self._pending_invites: dict[str, dict[str, str]] = {}
        self._event_queue: asyncio.Queue[dict[str, Any]] | None = None
        self._op_lock: asyncio.Lock | None = None

    def _ensure_queue(self) -> asyncio.Queue[dict[str, Any]]:
        if self._event_queue is None:
            self._event_queue = asyncio.Queue()
        return self._event_queue

    def _ensure_lock(self) -> asyncio.Lock:
        if self._op_lock is None:
            self._op_lock = asyncio.Lock()
        return self._op_lock

    async def run_locked(self, coro: Awaitable[T]) -> T:
        async with self._ensure_lock():
            return await coro

    def _emit_event(self, event_type: str, **kwargs: Any) -> None:
        event = {"type": event_type, "timestamp": time.time(), **kwargs}
        try:
            self._ensure_queue().put_nowait(event)
        except Exception:
            pass

    async def next_event(self, timeout: float | None = None) -> dict[str, Any]:
        try:
            return await asyncio.wait_for(self._ensure_queue().get(), timeout=timeout)
        except (asyncio.TimeoutError, TimeoutError):
            return {"type": "ping", "timestamp": time.time()}

    def _service(self) -> ClientAppService:
        return ClientAppService(
            client=self.client,
            session_manager=self.session_manager,
            contact_manager=self.contact_manager,
            preferred_p2p_port=self._preferred_p2p_port,
        )

    def _preferred_p2p_port(self, username: str) -> int:
        configured_port = int(config.CLIENT_P2P_PORT or 0)
        if configured_port > 0:
            return configured_port
        import zlib

        return 30000 + (zlib.crc32(username.encode("utf-8")) % 15000)

    async def _ensure_user_direct_listener(self, username: str) -> None:
        preferred_port = self._preferred_p2p_port(username)
        try:
            await self.client.ensure_direct_listener(
                host=config.CLIENT_P2P_HOST,
                port=preferred_port,
                force_rebind=True,
            )
        except OSError:
            await self.client.ensure_direct_listener()

    def _db_salt_path(self, username: str) -> str:
        return os.path.join(self.data_dir, username, "db_salt.bin")

    def _load_or_create_db_salt(self, username: str) -> bytes:
        salt_path = self._db_salt_path(username)
        if os.path.exists(salt_path):
            with open(salt_path, "rb") as salt_file:
                salt = salt_file.read()
            if len(salt) != 16:
                raise ValueError(f"Invalid DB salt at {salt_path}")
            return salt

        os.makedirs(os.path.dirname(salt_path), exist_ok=True)
        salt = os.urandom(16)
        with open(salt_path, "wb") as salt_file:
            salt_file.write(salt)
        return salt

    def _derive_client_db_key(self, username: str, password: str) -> bytes:
        salt = self._load_or_create_db_salt(username)
        kdf = PBKDF2HMAC(
            algorithm=hashes.SHA256(),
            length=32,
            salt=salt,
            iterations=400_000,
        )
        return kdf.derive(password.encode("utf-8"))

    async def connect(self) -> dict[str, Any]:
        if self.client.connected:
            return self.state_snapshot()

        await self.client.connect()
        await self.client.ensure_direct_listener()
        self._emit_event("connection", status="connected")
        return self.state_snapshot()

    async def disconnect(self) -> dict[str, Any]:
        if self.client.connected:
            await self.client.disconnect_server_only()
        self._emit_event("connection", status="disconnected")
        return self.state_snapshot()

    async def shutdown(self) -> None:
        if self.client.connected:
            print("[DEBUG] Shutting down client connection...")
            await self.client.disconnect()
            print("[DEBUG] Client connection closed.")

    async def register(self, username: str, password: str) -> dict[str, Any]:
        if not self.client.connected:
            print(f"[DEBUG] Not connected. Attempting auto-reconnect for register...")
            await self.connect()
            
        if self.client.authenticated:
            raise ValueError("Already logged in.")
        if len(password) < 6:
            raise ValueError("Password must be at least 6 characters")

        self.key_manager = KeyManager(username, self.data_dir)
        bundle = self.key_manager.get_registration_bundle()
        await self._ensure_user_direct_listener(username)
        p2p_host, p2p_port = self.client.get_advertised_endpoint()
        success = await self.client.register(
            username,
            password,
            bundle,
            p2p_host=p2p_host,
            p2p_port=p2p_port,
        )
        if not success:
            print(f"[DEBUG] Registration failed for user '{username}' (server rejected request)")
            self.key_manager = None
            raise ValueError("Registration failed")

        print(f"[DEBUG] Registration success for '{username}', initializing local state...")
        await self._init_after_auth(username, password)
        self._emit_event("auth", status="registered", username=username)
        return self.state_snapshot()

    async def login(self, username: str, password: str) -> dict[str, Any]:
        if not self.client.connected:
            print(f"[DEBUG] Not connected. Attempting auto-reconnect for login...")
            await self.connect()

        if self.client.authenticated:
            raise ValueError("Already logged in.")

        key_file = os.path.join(self.data_dir, username, "identity.json")
        if not os.path.exists(key_file):
            print(f"[DEBUG] Login attempt for '{username}' failed: {key_file} not found")
            raise ValueError(f"No local keys for {username}. Register on this device first.")

        self.key_manager = KeyManager(username, self.data_dir)
        await self._ensure_user_direct_listener(username)
        p2p_host, p2p_port = self.client.get_advertised_endpoint()
        success = await self.client.login(
            username,
            password,
            p2p_host=p2p_host,
            p2p_port=p2p_port,
        )
        if not success:
            print(f"[DEBUG] Login failed for user '{username}' (invalid credentials or server error)")
            self.key_manager = None
            # The client might have been disconnected by the failure
            raise ValueError(f"Login rejected for '{username}'. Check your password and ensure the username casing matches your registration exactly.")

        print(f"[DEBUG] Login success for '{username}', initializing local state...")
        await self._init_after_auth(username, password)
        self._emit_event("auth", status="logged_in", username=username)
        return self.state_snapshot()

    async def _init_after_auth(self, username: str, password: str) -> None:
        db_path = os.path.join(self.data_dir, username, "client.db")
        os.makedirs(os.path.dirname(db_path), exist_ok=True)
        db_key = self._derive_client_db_key(username, password)
        self.db = ClientDB(db_path, db_key=db_key)

        self.session_manager = SessionManager(self.key_manager, self.db)
        self.contact_manager = ContactManager(self.db, self.client.ca_cert_pem)
        self.group_manager = GroupManager(self.db, username)

        self.client.set_message_handler(self._handle_incoming_message)
        await self.client.start_receiving()

    async def add_contact(self, username: str) -> dict[str, Any]:
        if not self.client.authenticated:
            raise ValueError("Not logged in")
        if username == self.client.username:
            raise ValueError("Cannot add yourself")

        already = self.contact_manager.has_contact(username)
        bundle = await self._service().fetch_and_cache_contact(username, verify=True)
        if not bundle:
            raise ValueError(f"User '{username}' not found")

        self._emit_event(
            "contact",
            action="updated" if already else "added",
            username=username,
        )
        return self.get_contacts()

    def get_contacts(self) -> dict[str, Any]:
        if not self.client.authenticated:
            return {"contacts": []}

        contacts = []
        for username in self.contact_manager.list_contacts():
            contacts.append(
                {
                    "username": username,
                    "has_session": self.session_manager.has_session(username),
                }
            )
        return {"contacts": contacts}

    async def send_message(self, username: str, text: str) -> dict[str, Any]:
        if not self.client.authenticated:
            raise ValueError("Not logged in")
        if not text.strip():
            raise ValueError("Message cannot be empty")

        payload = await self._service().prepare_outbound_payload(username, text)
        route, refreshed_bundle = await self.client.send_chat_message_routed(
            recipient=username,
            sender=self.client.username,
            ciphertext_b64=payload.ciphertext_b64,
            ephemeral_pub_b64=payload.context.ephemeral_pub_b64,
            opk_id=payload.context.opk_id,
            msg_id=payload.msg_id,
            peer_host=payload.context.peer_host,
            peer_port=payload.context.peer_port,
        )
        if refreshed_bundle:
            self.contact_manager.add_contact(username, refreshed_bundle, verify=True)

        self.db.save_message(username, "sent", text)
        self._emit_event(
            "message_sent",
            to=username,
            text=text,
            route=route,
        )
        return {"route": route}

    def get_history(self, username: str, limit: int = 50) -> dict[str, Any]:
        if not self.client.authenticated:
            raise ValueError("Not logged in")
        return {"messages": self.db.get_messages(username, limit=limit)}

    async def fetch_offline(self) -> dict[str, Any]:
        if not self.client.authenticated:
            raise ValueError("Not logged in")

        messages = await self.client.fetch_offline_messages()
        processed = 0
        failed = 0
        for msg_json in messages:
            try:
                ok = await self._process_delivered_message(
                    DeliverMessage(payload_json=msg_json),
                    source="offline",
                )
                if ok:
                    processed += 1
                else:
                    failed += 1
            except Exception:
                failed += 1

        self._emit_event(
            "offline_fetch",
            total=len(messages),
            processed=processed,
            failed=failed,
        )
        return {"total": len(messages), "processed": processed, "failed": failed}

    async def _send_encrypted_to(self, recipient: str, plaintext: str) -> bool:
        try:
            payload = await self._service().prepare_outbound_payload(recipient, plaintext)
            route, refreshed_bundle = await self.client.send_chat_message_routed(
                recipient=recipient,
                sender=self.client.username,
                ciphertext_b64=payload.ciphertext_b64,
                ephemeral_pub_b64=payload.context.ephemeral_pub_b64,
                opk_id=payload.context.opk_id,
                msg_id=payload.msg_id,
                peer_host=payload.context.peer_host,
                peer_port=payload.context.peer_port,
            )
            if refreshed_bundle:
                self.contact_manager.add_contact(recipient, refreshed_bundle, verify=True)
            self._emit_event("delivery", recipient=recipient, route=route)
            return True
        except Exception:
            return False

    async def _handle_incoming_message(self, msg: BaseMessage) -> bool | None:
        if isinstance(msg, OfflineMessages):
            if msg.messages:
                self._emit_event("offline_notice", count=len(msg.messages))
                for msg_json in msg.messages:
                    try:
                        await self._process_delivered_message(
                            DeliverMessage(payload_json=msg_json),
                            source="offline",
                        )
                    except Exception:
                        self._emit_event("error", message="Failed to process offline message")
            return True
        if isinstance(msg, DeliverMessage):
            return await self._process_delivered_message(msg, source="live")
        return None

    async def _process_delivered_message(self, msg: DeliverMessage, source: str = "live") -> bool:
        try:
            inner = json.loads(msg.payload_json)
            if not isinstance(inner, dict):
                return False

            sender = inner.get("sender")
            recipient = inner.get("recipient")
            msg_id = inner.get("msg_id", "")
            if not sender or not recipient or recipient != self.client.username or not msg_id:
                return False

            if self.db.has_seen_message(msg_id):
                return True

            ephemeral_pub_b64 = inner.get("ephemeral_pub_b64", "")
            opk_id = inner.get("opk_id")

            contact = self.contact_manager.get_contact(sender)
            if not contact:
                bundle = await self.client.fetch_bundle(sender)
                if not bundle:
                    return False
                self.contact_manager.add_contact(sender, bundle, verify=True)
                contact = self.contact_manager.get_contact(sender)
                if not contact:
                    return False

            if ephemeral_pub_b64 and not self.session_manager.has_session(sender):
                self.session_manager.receive_initial_message(
                    peer=sender,
                    peer_ik_ed_pub=base64.b64decode(contact["ik_ed_pub"]),
                    peer_ik_dh_pub=base64.b64decode(contact["ik_dh_pub"]),
                    ephemeral_pub=base64.b64decode(ephemeral_pub_b64),
                    opk_id=opk_id,
                )

            ciphertext_b64 = inner.get("ciphertext_b64", "")
            if not ciphertext_b64:
                return False

            encrypted = json.loads(base64.b64decode(ciphertext_b64).decode())
            if not isinstance(encrypted, dict):
                return False

            plaintext = self.session_manager.decrypt_message(sender, encrypted)

            if self._is_group_invite(plaintext):
                await self._handle_group_invite(sender, plaintext, source=source)
            elif self._is_group_rekey_announce(plaintext):
                await self._handle_group_rekey_announce(sender, plaintext, source=source)
            elif self._is_group_sender_key_update(plaintext):
                self._handle_group_sender_key_update(sender, plaintext, source=source)
            elif self._is_group_sender_key(plaintext):
                self._handle_group_sender_key(sender, plaintext, source=source)
            elif self._is_group_msg(plaintext):
                self._handle_group_msg(sender, plaintext, source=source)
            else:
                self.db.save_message(sender, "received", plaintext, inner.get("timestamp"))
                self._emit_event(
                    "message",
                    source=source,
                    sender=sender,
                    text=plaintext,
                    timestamp=inner.get("timestamp"),
                )

            self.db.mark_message_seen(msg_id)
            return True
        except Exception:
            self._emit_event("error", message="Failed to process inbound message")
            return False

    def _is_group_invite(self, plaintext: str) -> bool:
        try:
            data = json.loads(plaintext)
            return isinstance(data, dict) and data.get("type") == "group_invite"
        except Exception:
            return False

    def _is_group_sender_key(self, plaintext: str) -> bool:
        try:
            data = json.loads(plaintext)
            return isinstance(data, dict) and data.get("type") == "group_sender_key"
        except Exception:
            return False

    def _is_group_rekey_announce(self, plaintext: str) -> bool:
        try:
            data = json.loads(plaintext)
            return isinstance(data, dict) and data.get("type") == "group_rekey_announce"
        except Exception:
            return False

    def _is_group_sender_key_update(self, plaintext: str) -> bool:
        try:
            data = json.loads(plaintext)
            return isinstance(data, dict) and data.get("type") == "group_sender_key_update"
        except Exception:
            return False

    def _is_group_msg(self, plaintext: str) -> bool:
        try:
            data = json.loads(plaintext)
            return isinstance(data, dict) and data.get("type") == "group_msg"
        except Exception:
            return False

    async def _handle_group_invite(self, sender: str, plaintext: str, source: str = "live") -> None:
        data = json.loads(plaintext)
        group_id = data["group_id"]
        self._pending_invites[group_id] = {
            "inviter": sender,
            "group_name": data["group_name"],
            "sender_key_b64": data["sender_key_b64"],
        }
        self._emit_event(
            "group_invite",
            source=source,
            group_id=group_id,
            inviter=sender,
            group_name=data["group_name"],
        )

    async def accept_group_invite(self, group_id: str) -> dict[str, Any]:
        invite = self._pending_invites.get(group_id)
        if not invite:
            raise ValueError(f"No pending invite for group {group_id}")

        sender = invite["inviter"]
        group_name = invite["group_name"]
        sender_key_b64 = invite["sender_key_b64"]

        my_sender_key = self.group_manager.join_group(group_id, group_name, sender)
        self.group_manager.receive_sender_key(group_id, sender, base64.b64decode(sender_key_b64))

        response = json.dumps(
            {
                "type": "group_sender_key",
                "group_id": group_id,
                "sender_key_b64": base64.b64encode(my_sender_key).decode(),
            }
        )
        key_exchange_sent = await self._send_encrypted_to(sender, response)
        del self._pending_invites[group_id]

        self._emit_event(
            "group_joined",
            group_id=group_id,
            group_name=group_name,
            key_exchange_sent=key_exchange_sent,
        )
        return {
            "group_id": group_id,
            "group_name": group_name,
            "key_exchange_sent": key_exchange_sent,
        }

    def reject_group_invite(self, group_id: str) -> dict[str, Any]:
        invite = self._pending_invites.get(group_id)
        if not invite:
            raise ValueError(f"No pending invite for group {group_id}")

        group_name = invite["group_name"]
        del self._pending_invites[group_id]
        self._emit_event("group_invite_rejected", group_id=group_id, group_name=group_name)
        return {"group_id": group_id, "group_name": group_name}

    def _handle_group_msg(self, sender: str, plaintext: str, source: str = "live") -> None:
        data = json.loads(plaintext)
        group_id = data["group_id"]
        group_name = data["group_name"]
        decrypted = self.group_manager.decrypt_message(data["encrypted"])
        self.db.save_group_message(group_id, sender, decrypted)
        self._emit_event(
            "group_message",
            source=source,
            group_id=group_id,
            group_name=group_name,
            sender=sender,
            text=decrypted,
        )

    def _handle_group_sender_key(self, sender: str, plaintext: str, source: str = "live") -> None:
        data = json.loads(plaintext)
        group_id = data["group_id"]
        sender_key = base64.b64decode(data["sender_key_b64"])
        self.group_manager.receive_sender_key(group_id, sender, sender_key)

        group = self.db.get_group(group_id)
        group_name = group["name"] if group else group_id
        self._emit_event(
            "group_key_received",
            source=source,
            group_id=group_id,
            group_name=group_name,
            sender=sender,
        )

    async def _handle_group_rekey_announce(self, sender: str, plaintext: str, source: str = "live") -> None:
        data = json.loads(plaintext)
        group_id = data["group_id"]
        group_name = data.get("group_name", group_id)
        removed_username = data["removed_username"]
        remaining_members = data.get("remaining_members", [])
        sender_key_b64 = data["sender_key_b64"]
        rotation_epoch = data.get("rotation_epoch", "")

        if removed_username == self.client.username:
            self.group_manager.remove_member(group_id, removed_username)
            self._emit_event(
                "group_removed_self",
                source=source,
                group_id=group_id,
                group_name=group_name,
            )
            return

        if not self.db.get_group(group_id):
            self.db.create_group(group_id, group_name, sender)

        self.group_manager.remove_member(group_id, removed_username)
        self.group_manager.receive_sender_key(group_id, sender, base64.b64decode(sender_key_b64))

        my_rotated_key = self.group_manager.rotate_own_sender_key(group_id)
        sender_key_update = json.dumps(
            {
                "type": "group_sender_key_update",
                "group_id": group_id,
                "group_name": group_name,
                "sender_key_b64": base64.b64encode(my_rotated_key).decode(),
                "removed_username": removed_username,
                "rotation_epoch": rotation_epoch,
            }
        )

        failed_members: list[str] = []
        targets = [member for member in remaining_members if member != self.client.username]
        for member in targets:
            ok = await self._send_encrypted_to(member, sender_key_update)
            if not ok:
                failed_members.append(member)

        self._emit_event(
            "group_rekey",
            source=source,
            group_id=group_id,
            group_name=group_name,
            removed_username=removed_username,
            failed_members=failed_members,
        )

    def _handle_group_sender_key_update(self, sender: str, plaintext: str, source: str = "live") -> None:
        data = json.loads(plaintext)
        group_id = data["group_id"]
        group_name = data.get("group_name")
        removed_username = data.get("removed_username", "")
        sender_key_b64 = data["sender_key_b64"]

        if removed_username:
            self.group_manager.remove_member(group_id, removed_username)

        self.group_manager.receive_sender_key(group_id, sender, base64.b64decode(sender_key_b64))

        if not group_name:
            group = self.db.get_group(group_id)
            group_name = group["name"] if group else group_id

        self._emit_event(
            "group_key_updated",
            source=source,
            group_id=group_id,
            group_name=group_name,
            sender=sender,
            removed_username=removed_username,
        )

    def list_groups(self) -> dict[str, Any]:
        if not self.client.authenticated:
            raise ValueError("Not logged in")

        groups = self.group_manager.list_groups()
        result = []
        for group in groups:
            members = self.group_manager.get_group_members(group["group_id"])
            result.append(
                {
                    "group_id": group["group_id"],
                    "name": group["name"],
                    "members": members,
                    "member_count": len(members),
                }
            )
        return {"groups": result}

    def list_group_invites(self) -> dict[str, Any]:
        invites = []
        for group_id, invite in self._pending_invites.items():
            invites.append(
                {
                    "group_id": group_id,
                    "group_name": invite["group_name"],
                    "inviter": invite["inviter"],
                }
            )
        return {"invites": invites}

    async def create_group(self, name: str) -> dict[str, Any]:
        if not self.client.authenticated:
            raise ValueError("Not logged in")
        group_id = self.group_manager.create_group(name)
        self._emit_event("group_created", group_id=group_id, name=name)
        return {"group_id": group_id, "name": name}

    def group_members(self, group_id: str) -> dict[str, Any]:
        if not self.client.authenticated:
            raise ValueError("Not logged in")
        group = self.db.get_group(group_id)
        if not group:
            raise ValueError(f"Group {group_id} not found")
        members = self.group_manager.get_group_members(group_id)
        return {"group_id": group_id, "name": group["name"], "members": members}

    async def invite_group_member(self, group_id: str, username: str) -> dict[str, Any]:
        if not self.client.authenticated:
            raise ValueError("Not logged in")

        group = self.db.get_group(group_id)
        if not group:
            raise ValueError(f"Group {group_id} not found")
        if username == self.client.username:
            raise ValueError("You are already in the group")

        my_sender_key = self.group_manager.get_my_sender_key(group_id)
        if not my_sender_key:
            raise ValueError("You are not a member of this group")

        invite_msg = json.dumps(
            {
                "type": "group_invite",
                "group_id": group_id,
                "group_name": group["name"],
                "sender_key_b64": base64.b64encode(my_sender_key).decode(),
            }
        )
        success = await self._send_encrypted_to(username, invite_msg)
        if not success:
            raise ValueError(f"Failed to send invitation to {username}")

        self.group_manager.add_member(group_id, username)
        self._emit_event(
            "group_invitation_sent",
            group_id=group_id,
            group_name=group["name"],
            username=username,
        )
        return {"group_id": group_id, "username": username}

    async def remove_group_member(self, group_id: str, username: str) -> dict[str, Any]:
        if not self.client.authenticated:
            raise ValueError("Not logged in")

        group = self.db.get_group(group_id)
        if not group:
            raise ValueError(f"Group {group_id} not found")
        if group["creator"] != self.client.username:
            raise ValueError("Only the group creator can remove members")
        if username == self.client.username:
            raise ValueError("Creator self-removal is not supported")

        members = self.group_manager.get_group_members(group_id)
        if username not in members:
            raise ValueError(f"User '{username}' is not a member of this group")

        self.group_manager.remove_member(group_id, username)
        rotated_sender_key = self.group_manager.rotate_own_sender_key(group_id)
        remaining_members = [member for member in members if member != username]
        import uuid

        rotation_epoch = uuid.uuid4().hex

        announce_payload = json.dumps(
            {
                "type": "group_rekey_announce",
                "group_id": group_id,
                "group_name": group["name"],
                "removed_username": username,
                "remaining_members": remaining_members,
                "rotation_epoch": rotation_epoch,
                "sender_key_b64": base64.b64encode(rotated_sender_key).decode(),
            }
        )

        failed_members: list[str] = []
        targets = [member for member in remaining_members if member != self.client.username]
        for member in targets:
            ok = await self._send_encrypted_to(member, announce_payload)
            if not ok:
                failed_members.append(member)

        self._emit_event(
            "group_member_removed",
            group_id=group_id,
            group_name=group["name"],
            username=username,
            failed_members=failed_members,
        )
        return {"group_id": group_id, "username": username, "failed_members": failed_members}

    async def send_group_message(self, group_id: str, text: str) -> dict[str, Any]:
        if not self.client.authenticated:
            raise ValueError("Not logged in")

        group = self.db.get_group(group_id)
        if not group:
            raise ValueError(f"Group {group_id} not found")

        encrypted = self.group_manager.encrypt_message(group_id, text)
        self.db.save_group_message(group_id, self.client.username, text)

        members = self.group_manager.get_group_members(group_id)
        sent_count = 0
        failed_members: list[str] = []
        targets = [m for m in members if m != self.client.username]
        for member in targets:
            group_msg = json.dumps(
                {
                    "type": "group_msg",
                    "group_id": group_id,
                    "group_name": group["name"],
                    "encrypted": encrypted,
                }
            )
            success = await self._send_encrypted_to(member, group_msg)
            if success:
                sent_count += 1
            else:
                failed_members.append(member)

        self._emit_event(
            "group_message_sent",
            group_id=group_id,
            group_name=group["name"],
            sent_count=sent_count,
            total_targets=len(targets),
            failed_members=failed_members,
        )
        return {
            "group_id": group_id,
            "sent_count": sent_count,
            "total_targets": len(targets),
            "failed_members": failed_members,
        }

    def get_group_history(self, group_id: str, limit: int = 50) -> dict[str, Any]:
        if not self.client.authenticated:
            raise ValueError("Not logged in")

        group = self.db.get_group(group_id)
        if not group:
            raise ValueError(f"Group {group_id} not found")

        messages = self.db.get_group_messages(group_id, limit=limit)
        return {"group_id": group_id, "name": group["name"], "messages": messages}

    def state_snapshot(self) -> dict[str, Any]:
        p2p_host, p2p_port = self.client.get_advertised_endpoint()
        return {
            "connected": self.client.connected,
            "authenticated": self.client.authenticated,
            "username": self.client.username,
            "p2p_endpoint": {
                "host": p2p_host,
                "port": p2p_port,
            },
            "pending_invites": self.list_group_invites()["invites"],
        }
