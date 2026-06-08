"""Shared client application service layer.

This module centralizes orchestration logic that was previously embedded
directly in the CLI so both CLI and GUI can reuse the same behavior.
"""
from __future__ import annotations

import base64
import json
import uuid
from dataclasses import dataclass
from typing import Callable

import config

from src.client.client import ChatClient
from src.client.contact_manager import ContactManager
from src.client.session_manager import SessionManager


@dataclass(slots=True)
class OutboundDeliveryContext:
    """Resolved delivery metadata for one outbound message."""

    ephemeral_pub_b64: str = ""
    opk_id: int | None = None
    peer_host: str = ""
    peer_port: int = 0


@dataclass(slots=True)
class OutboundMessagePayload:
    """Prepared encrypted payload for routed delivery."""

    recipient: str
    msg_id: str
    ciphertext_b64: str
    context: OutboundDeliveryContext


class ClientAppService:
    """Encapsulate reusable client orchestration flows.

    The service intentionally keeps manager/transport ownership outside
    itself, so callers remain responsible for lifecycle and UI concerns.
    """

    def __init__(
        self,
        client: ChatClient,
        session_manager: SessionManager | None,
        contact_manager: ContactManager | None,
        preferred_p2p_port: Callable[[str], int],
    ):
        self.client = client
        self.session_manager = session_manager
        self.contact_manager = contact_manager
        self.preferred_p2p_port = preferred_p2p_port

    def _require_contact_manager(self) -> ContactManager:
        if self.contact_manager is None:
            raise RuntimeError("Contact manager is not initialized")
        return self.contact_manager

    def _require_session_manager(self) -> SessionManager:
        if self.session_manager is None:
            raise RuntimeError("Session manager is not initialized")
        return self.session_manager

    async def fetch_and_cache_contact(self, username: str, verify: bool = True) -> dict | None:
        """Fetch a contact bundle from the server and cache it locally."""
        contact_manager = self._require_contact_manager()
        bundle = await self.client.fetch_bundle(username)
        if not bundle:
            return None
        contact_manager.add_contact(username, bundle, verify=verify)
        return bundle

    def build_cached_bundle_for_session(self, username: str) -> tuple[dict | None, str]:
        """Build a minimal peer bundle from cached contact data."""
        contact_manager = self._require_contact_manager()
        contact = contact_manager.get_contact(username)
        if not contact:
            return None, (
                f"No cached contact bundle for '{username}' while server is disconnected. "
                f"Reconnect and run /add {username}."
            )

        required_fields = ("ik_ed_pub", "ik_dh_pub", "spk_pub", "spk_sig")
        missing_fields = [field for field in required_fields if not contact.get(field)]
        if missing_fields:
            return None, (
                f"Cached contact bundle for '{username}' is missing required fields "
                f"({', '.join(missing_fields)}). Reconnect and run /add {username}."
            )

        bundle = {field: contact[field] for field in required_fields}
        if contact.get("p2p_host"):
            bundle["p2p_host"] = contact["p2p_host"]
        if int(config.CLIENT_P2P_PORT or 0) == 0:
            bundle["p2p_port"] = self.preferred_p2p_port(username)
        elif contact.get("p2p_port"):
            bundle["p2p_port"] = int(contact["p2p_port"])
        return bundle, ""

    async def prepare_outbound_payload(self, recipient: str, plaintext: str) -> OutboundMessagePayload:
        """Resolve session state and encrypt an outbound message."""
        session_manager = self._require_session_manager()
        contact_manager = self._require_contact_manager()
        context = OutboundDeliveryContext()

        if not session_manager.has_session(recipient):
            if not self.client.connected:
                cached_bundle, error = self.build_cached_bundle_for_session(recipient)
                if not cached_bundle:
                    raise ValueError(error)
                context.peer_host = cached_bundle.get("p2p_host", "") or ""
                context.peer_port = int(cached_bundle.get("p2p_port", 0) or 0)
                _, context.ephemeral_pub_b64, context.opk_id = session_manager.initiate_session(
                    recipient,
                    cached_bundle,
                )
            else:
                if not contact_manager.has_contact(recipient):
                    bundle = await self.client.fetch_bundle(recipient)
                    if not bundle:
                        raise LookupError(f"User '{recipient}' not found")
                    contact_manager.add_contact(recipient, bundle, verify=True)

                bundle = await self.client.fetch_bundle(recipient)
                if not bundle:
                    raise LookupError(f"Failed to get keys for '{recipient}'")
                contact_manager.add_contact(recipient, bundle, verify=True)
                context.peer_host = bundle.get("p2p_host", "") or ""
                context.peer_port = int(bundle.get("p2p_port", 0) or 0)
                _, context.ephemeral_pub_b64, context.opk_id = session_manager.initiate_session(
                    recipient,
                    bundle,
                )
        else:
            contact = contact_manager.get_contact(recipient)
            if self.client.connected:
                try:
                    refreshed_bundle = await self.client.fetch_bundle(recipient)
                    if refreshed_bundle:
                        contact_manager.add_contact(recipient, refreshed_bundle, verify=True)
                        contact = contact_manager.get_contact(recipient) or contact
                except Exception:
                    pass
            if contact:
                context.peer_host = contact.get("p2p_host", "") or ""
                context.peer_port = int(contact.get("p2p_port", 0) or 0)
                if not self.client.connected and int(config.CLIENT_P2P_PORT or 0) == 0:
                    context.peer_port = self.preferred_p2p_port(recipient)

        encrypted = session_manager.encrypt_message(recipient, plaintext)
        ciphertext_b64 = base64.b64encode(json.dumps(encrypted).encode()).decode()

        return OutboundMessagePayload(
            recipient=recipient,
            msg_id=uuid.uuid4().hex,
            ciphertext_b64=ciphertext_b64,
            context=context,
        )
