"""
Session Manager for E2EE messaging.

Handles:
- X3DH key agreement for new sessions
- Double Ratchet state management
- Session persistence
"""
from __future__ import annotations

import base64
import json
from dataclasses import asdict
from typing import TYPE_CHECKING

from src.crypto.asymmetric import (
    serialize_public_key,
    serialize_private_key,
    deserialize_x25519_private,
)
from src.crypto.x3dh import (
    KeyBundle,
    X3DHSenderResult,
    X3DHRecipientKeys,
    x3dh_sender,
    x3dh_recipient,
)
from src.crypto.ratchet import (
    RatchetState,
    MessageHeader,
    ratchet_init_sender,
    ratchet_init_recipient,
    ratchet_encrypt,
    ratchet_decrypt,
)

if TYPE_CHECKING:
    from src.client.key_manager import KeyManager
    from src.storage.client_db import ClientDB


class SessionManager:
    """Manages E2EE sessions with peers using X3DH and Double Ratchet.
    
    Each session maintains:
    - X3DH derived session key
    - Double Ratchet state for forward secrecy
    - Message ordering state
    """
    
    def __init__(self, key_manager: KeyManager, db: ClientDB):
        """Initialize session manager.
        
        Args:
            key_manager: Local key manager for identity keys
            db: Client database for session persistence
        """
        self.key_manager = key_manager
        self.db = db
        self._sessions: dict[str, RatchetState] = {}
    
    def has_session(self, peer: str) -> bool:
        """Check if we have an active session with a peer.
        
        Args:
            peer: Peer's username
            
        Returns:
            True if session exists
        """
        if peer in self._sessions:
            return True
        
        # Try to load from database
        state_json = self.db.get_session(peer)
        if state_json:
            self._sessions[peer] = self._deserialize_state(state_json)
            return True
        
        return False
    
    def initiate_session(
        self,
        peer: str,
        peer_bundle: dict,
    ) -> tuple[bytes, str, int | None]:
        """Initiate a new session with a peer using X3DH.
        
        Args:
            peer: Peer's username
            peer_bundle: Peer's public key bundle from server
            
        Returns:
            Tuple of (initial_ciphertext_for_header, ephemeral_pub_b64, opk_id)
        """
        keys = self.key_manager.keys
        
        # Build KeyBundle from server response
        recipient_bundle = KeyBundle(
            identity_key=base64.b64decode(peer_bundle["ik_ed_pub"]),
            identity_dh_key=base64.b64decode(peer_bundle["ik_dh_pub"]),
            signed_prekey=base64.b64decode(peer_bundle["spk_pub"]),
            signed_prekey_sig=base64.b64decode(peer_bundle["spk_sig"]),
            onetime_prekey=base64.b64decode(peer_bundle["opk_pub"]) if peer_bundle.get("opk_pub") else None,
            onetime_prekey_id=peer_bundle.get("opk_id"),
        )
        
        # Perform X3DH as sender
        x3dh_result: X3DHSenderResult = x3dh_sender(
            sender_identity_priv=keys.ik_ed_priv,
            sender_identity_x25519_priv=keys.ik_dh_priv,
            recipient_bundle=recipient_bundle,
        )
        
        # Initialize Double Ratchet as sender
        # Use the peer's SPK as the initial ratchet public key
        state = ratchet_init_sender(
            session_key=x3dh_result.session_key,
            recipient_ratchet_pub=recipient_bundle.signed_prekey,
        )
        
        # Store session
        self._sessions[peer] = state
        self._save_session(peer)
        
        return (
            x3dh_result.ephemeral_pub,
            base64.b64encode(x3dh_result.ephemeral_pub).decode(),
            x3dh_result.onetime_key_id,
        )
    
    def receive_initial_message(
        self,
        peer: str,
        peer_ik_ed_pub: bytes,
        peer_ik_dh_pub: bytes,
        ephemeral_pub: bytes,
        opk_id: int | None,
    ) -> None:
        """Handle receiving a first message and establish session.
        
        Args:
            peer: Peer's username
            peer_ik_ed_pub: Peer's Ed25519 identity public key
            peer_ik_dh_pub: Peer's X25519 identity public key
            ephemeral_pub: Peer's ephemeral public key
            opk_id: ID of OPK used (if any)
        """
        keys = self.key_manager.keys
        
        # Get OPK private key if used
        opk_priv = None
        if opk_id is not None:
            opk_priv = self.key_manager.get_opk_private(opk_id)
            if opk_priv is not None:
                # Mark OPK as used
                self.key_manager.mark_opk_used(opk_id)
        
        # Build recipient keys
        recipient_keys = X3DHRecipientKeys(
            identity_priv=keys.ik_ed_priv,
            identity_x25519_priv=keys.ik_dh_priv,
            signed_prekey_priv=keys.spk_priv,
            onetime_priv=opk_priv,
        )
        
        # Perform X3DH as recipient
        session_key = x3dh_recipient(
            recipient_keys=recipient_keys,
            sender_identity_pub_bytes=peer_ik_ed_pub,
            sender_identity_x25519_pub_bytes=peer_ik_dh_pub,
            sender_ephemeral_pub_bytes=ephemeral_pub,
            onetime_key_id=opk_id,
        )
        
        # Initialize Double Ratchet as recipient
        state = ratchet_init_recipient(
            session_key=session_key,
            recipient_ratchet_priv=keys.spk_priv,
        )
        
        # Store session
        self._sessions[peer] = state
        self._save_session(peer)
    
    def encrypt_message(self, peer: str, plaintext: str) -> dict:
        """Encrypt a message for a peer.
        
        Args:
            peer: Peer's username
            plaintext: Message text
            
        Returns:
            Dictionary with header and ciphertext (base64 encoded)
            
        Raises:
            ValueError: If no session exists with peer
        """
        if not self.has_session(peer):
            raise ValueError(f"No session with {peer}")
        
        state = self._sessions[peer]
        
        # Encrypt with ratchet
        header, nonce, ciphertext = ratchet_encrypt(
            state,
            plaintext.encode("utf-8"),
        )
        
        # Save updated state
        self._save_session(peer)
        
        return {
            "header": {
                "dh_pub": base64.b64encode(header.dh_pub).decode(),
                "pn": header.pn,
                "n": header.n,
            },
            "nonce": base64.b64encode(nonce).decode(),
            "ciphertext": base64.b64encode(ciphertext).decode(),
        }
    
    def decrypt_message(self, peer: str, encrypted: dict) -> str:
        """Decrypt a message from a peer.
        
        Args:
            peer: Peer's username
            encrypted: Dictionary with header, nonce, ciphertext
            
        Returns:
            Decrypted message text
            
        Raises:
            ValueError: If no session exists or decryption fails
        """
        if not self.has_session(peer):
            raise ValueError(f"No session with {peer}")
        
        state = self._sessions[peer]
        
        # Parse header
        header = MessageHeader(
            dh_pub=base64.b64decode(encrypted["header"]["dh_pub"]),
            pn=encrypted["header"]["pn"],
            n=encrypted["header"]["n"],
        )
        nonce = base64.b64decode(encrypted["nonce"])
        ciphertext = base64.b64decode(encrypted["ciphertext"])
        
        # Decrypt with ratchet
        plaintext = ratchet_decrypt(state, header, nonce, ciphertext)
        
        # Save updated state
        self._save_session(peer)
        
        return plaintext.decode("utf-8")
    
    def _save_session(self, peer: str) -> None:
        """Save session state to database."""
        if peer in self._sessions:
            state_json = self._serialize_state(self._sessions[peer])
            self.db.save_session(peer, state_json)
    
    def _serialize_state(self, state: RatchetState) -> str:
        """Serialize ratchet state to JSON."""
        data = {
            "dh_send_priv": base64.b64encode(
                serialize_private_key(state.dh_send_priv)
            ).decode() if state.dh_send_priv else None,
            "dh_send_pub": base64.b64encode(state.dh_send_pub).decode() if state.dh_send_pub else None,
            "dh_recv_pub": base64.b64encode(state.dh_recv_pub).decode() if state.dh_recv_pub else None,
            "root_key": base64.b64encode(state.root_key).decode(),
            "chain_key_send": base64.b64encode(state.chain_key_send).decode() if state.chain_key_send else None,
            "chain_key_recv": base64.b64encode(state.chain_key_recv).decode() if state.chain_key_recv else None,
            "send_count": state.send_count,
            "recv_count": state.recv_count,
            "prev_send_count": state.prev_send_count,
            "skipped": {
                f"{base64.b64encode(k[0]).decode()}:{k[1]}": base64.b64encode(v).decode()
                for k, v in state.skipped.items()
            },
        }
        return json.dumps(data)
    
    def _deserialize_state(self, state_json: str) -> RatchetState:
        """Deserialize ratchet state from JSON."""
        data = json.loads(state_json)
        
        dh_send_priv = None
        if data["dh_send_priv"]:
            dh_send_priv = deserialize_x25519_private(
                base64.b64decode(data["dh_send_priv"])
            )
        
        skipped = {}
        for k, v in data["skipped"].items():
            key_parts = k.rsplit(":", 1)
            dh_pub = base64.b64decode(key_parts[0])
            n = int(key_parts[1])
            skipped[(dh_pub, n)] = base64.b64decode(v)
        
        return RatchetState(
            dh_send_priv=dh_send_priv,
            dh_send_pub=base64.b64decode(data["dh_send_pub"]) if data["dh_send_pub"] else None,
            dh_recv_pub=base64.b64decode(data["dh_recv_pub"]) if data["dh_recv_pub"] else None,
            root_key=base64.b64decode(data["root_key"]),
            chain_key_send=base64.b64decode(data["chain_key_send"]) if data["chain_key_send"] else None,
            chain_key_recv=base64.b64decode(data["chain_key_recv"]) if data["chain_key_recv"] else None,
            send_count=data["send_count"],
            recv_count=data["recv_count"],
            prev_send_count=data["prev_send_count"],
            skipped=skipped,
        )
    
    def delete_session(self, peer: str) -> None:
        """Delete a session with a peer.
        
        Args:
            peer: Peer's username
        """
        if peer in self._sessions:
            del self._sessions[peer]
        self.db.delete_session(peer)