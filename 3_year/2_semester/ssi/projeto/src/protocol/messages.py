"""Wire message dataclasses and serialization helpers."""

from __future__ import annotations

import json
import time
from dataclasses import asdict, dataclass, field, fields
from enum import Enum
from typing import Any

from cryptography.hazmat.primitives.asymmetric.ed25519 import (
    Ed25519PrivateKey,
    Ed25519PublicKey,
)

from src.crypto.asymmetric import ed25519_sign, ed25519_verify


class MsgType(str, Enum):
    """All protocol message types exchanged between clients and server."""

    CLIENT_HELLO = "CLIENT_HELLO"
    SERVER_HELLO = "SERVER_HELLO"
    CLIENT_AUTH = "CLIENT_AUTH"
    AUTH_OK = "AUTH_OK"
    AUTH_FAIL = "AUTH_FAIL"

    REGISTER = "REGISTER"
    UPLOAD_KEYS = "UPLOAD_KEYS"
    FETCH_BUNDLE = "FETCH_BUNDLE"
    BUNDLE_RESPONSE = "BUNDLE_RESPONSE"
    BUNDLE_NOT_FOUND = "BUNDLE_NOT_FOUND"
    REPLENISH_OPK = "REPLENISH_OPK"

    SEND_MSG = "SEND_MSG"
    DELIVER_MSG = "DELIVER_MSG"
    FETCH_OFFLINE = "FETCH_OFFLINE"
    OFFLINE_MSGS = "OFFLINE_MSGS"
    OFFLINE_MESSAGES = "OFFLINE_MESSAGES"
    ACK = "ACK"

    CREATE_GROUP = "CREATE_GROUP"
    GROUP_MSG = "GROUP_MSG"
    GROUP_INVITE = "GROUP_INVITE"


@dataclass
class BaseMessage:
    """Base message envelope with type and timestamp."""

    type: MsgType
    timestamp: float = field(default_factory=time.time)

    def to_dict(self) -> dict[str, Any]:
        """Serialize dataclass payload to dictionary with string message type."""
        payload = asdict(self)
        payload["type"] = self.type.value
        return payload

    def to_json(self) -> str:
        """Serialize message payload to canonical compact JSON string."""
        return json.dumps(self.to_dict(), separators=(",", ":"), sort_keys=True)

    @classmethod
    def from_json(cls, raw: str) -> "BaseMessage":
        """Deserialize JSON payload and dispatch into concrete message class."""
        data = json.loads(raw)
        if "type" not in data:
            raise ValueError("Missing message type")

        msg_type = MsgType(data["type"])
        data["type"] = msg_type

        target = _dispatch_message_class(msg_type, data)
        allowed_fields = {f.name for f in fields(target)}
        filtered = {key: value for key, value in data.items() if key in allowed_fields}
        return target(**filtered)


@dataclass
class ChatMessage(BaseMessage):
    """Encrypted end-to-end chat message envelope."""

    type: MsgType = MsgType.SEND_MSG
    sender: str = ""
    recipient: str = ""
    x3dh_ik_pub: str | None = None
    x3dh_ek_pub: str | None = None
    x3dh_opk_id: int | None = None
    ratchet_dh_pub: str = ""
    ratchet_pn: int = 0
    ratchet_n: int = 0
    nonce: str = ""
    ciphertext: str = ""
    sig: str = ""

    def signed_payload_bytes(self) -> bytes:
        """Return canonical payload bytes used for signature operations."""
        payload = self.to_dict()
        payload.pop("sig", None)
        return json.dumps(payload, separators=(",", ":"), sort_keys=True).encode("utf-8")

    def sign(self, private_key: Ed25519PrivateKey) -> str:
        """Sign message payload and store base64 signature in-place."""
        import base64

        self.sig = base64.b64encode(ed25519_sign(private_key, self.signed_payload_bytes())).decode("ascii")
        return self.sig

    def verify(self, public_key: Ed25519PublicKey) -> bool:
        """Verify stored signature against current canonical payload bytes."""
        import base64

        if not self.sig:
            return False
        try:
            signature = base64.b64decode(self.sig)
        except Exception:
            return False
        return ed25519_verify(public_key, signature, self.signed_payload_bytes())


@dataclass
class KeyBundle(BaseMessage):
    """Structured key bundle used by older protocol flow."""

    type: MsgType = MsgType.BUNDLE_RESPONSE
    username: str = ""
    ik_ed_pub: str = ""
    ik_dh_pub: str = ""
    spk_pub: str = ""
    spk_sig: str = ""
    opk_pub: str | None = None
    opk_id: int | None = None
    cert_pem: str | None = None
    p2p_host: str = ""
    p2p_port: int = 0


@dataclass
class ClientHello(BaseMessage):
    """Client handshake opener carrying ephemeral X25519 public key."""

    type: MsgType = MsgType.CLIENT_HELLO
    ephemeral_pub_b64: str = ""


@dataclass
class ServerHello(BaseMessage):
    """Server handshake reply with ephemeral key and CA certificate."""

    type: MsgType = MsgType.SERVER_HELLO
    ephemeral_pub_b64: str = ""
    ca_cert_pem: str = ""


@dataclass
class Register(BaseMessage):
    """Registration request carrying auth secret and uploaded public bundle."""

    type: MsgType = MsgType.REGISTER
    username: str = ""
    password: str = ""
    password_hash: str = ""
    key_bundle_json: str = ""
    p2p_host: str = ""
    p2p_port: int = 0


@dataclass
class ClientAuth(BaseMessage):
    """Login request carrying auth secret and current P2P endpoint metadata."""

    type: MsgType = MsgType.CLIENT_AUTH
    username: str = ""
    password: str = ""
    password_hash: str = ""
    p2p_host: str = ""
    p2p_port: int = 0


@dataclass
class AuthOK(BaseMessage):
    """Authentication success response."""

    type: MsgType = MsgType.AUTH_OK


@dataclass
class AuthFail(BaseMessage):
    """Authentication failure response."""

    type: MsgType = MsgType.AUTH_FAIL


@dataclass
class FetchBundle(BaseMessage):
    """Request public bundle for a target username."""

    type: MsgType = MsgType.FETCH_BUNDLE
    username: str = ""


@dataclass
class BundleResponse(BaseMessage):
    """Response carrying serialized public bundle for requested username."""

    type: MsgType = MsgType.BUNDLE_RESPONSE
    username: str = ""
    bundle_json: str = ""


@dataclass
class BundleNotFound(BaseMessage):
    """Response indicating requested username has no available bundle."""

    type: MsgType = MsgType.BUNDLE_NOT_FOUND
    username: str = ""


@dataclass
class SendMessage(BaseMessage):
    """Server-routed encrypted direct message payload."""

    type: MsgType = MsgType.SEND_MSG
    msg_id: str = ""
    recipient: str = ""
    sender: str = ""
    ciphertext_b64: str = ""
    ephemeral_pub_b64: str = ""
    opk_id: int | None = None


@dataclass
class DeliverMessage(BaseMessage):
    """Delivery envelope used by server/direct channels for inbound payload."""

    type: MsgType = MsgType.DELIVER_MSG
    payload_json: str = ""


@dataclass
class FetchOffline(BaseMessage):
    """Request to fetch and drain queued offline messages."""

    type: MsgType = MsgType.FETCH_OFFLINE


@dataclass
class OfflineMessages(BaseMessage):
    """Response carrying queued offline payloads for authenticated user."""

    type: MsgType = MsgType.OFFLINE_MESSAGES
    messages: list[str] = field(default_factory=list)


@dataclass
class DirectAck(BaseMessage):
    """Acknowledgement for direct peer-to-peer message delivery."""

    type: MsgType = MsgType.ACK
    msg_id: str = ""
    accepted: bool = False
    reason: str = ""


def _dispatch_message_class(msg_type: MsgType, data: dict[str, Any]) -> type[BaseMessage]:
    """Select the concrete dataclass type for parsed message payload."""
    if msg_type == MsgType.BUNDLE_RESPONSE:
        if "bundle_json" in data:
            return BundleResponse
        if "ik_ed_pub" in data:
            return KeyBundle
        return BaseMessage

    if msg_type == MsgType.DELIVER_MSG:
        if "payload_json" in data:
            return DeliverMessage
        return ChatMessage

    if msg_type == MsgType.SEND_MSG:
        if "ratchet_dh_pub" in data or "nonce" in data or "ciphertext" in data:
            return ChatMessage
        return SendMessage

    if msg_type in (MsgType.OFFLINE_MESSAGES, MsgType.OFFLINE_MSGS):
        return OfflineMessages

    dispatch: dict[MsgType, type[BaseMessage]] = {
        MsgType.CLIENT_HELLO: ClientHello,
        MsgType.SERVER_HELLO: ServerHello,
        MsgType.REGISTER: Register,
        MsgType.CLIENT_AUTH: ClientAuth,
        MsgType.AUTH_OK: AuthOK,
        MsgType.AUTH_FAIL: AuthFail,
        MsgType.FETCH_BUNDLE: FetchBundle,
        MsgType.BUNDLE_NOT_FOUND: BundleNotFound,
        MsgType.FETCH_OFFLINE: FetchOffline,
        MsgType.ACK: DirectAck,
    }
    return dispatch.get(msg_type, BaseMessage)
