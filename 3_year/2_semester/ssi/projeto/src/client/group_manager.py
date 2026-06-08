"""
Group Manager for E2EE group messaging.

Implements Sender Keys approach:
- Each group member has a sender key
- Messages are encrypted with sender's chain key
- Forward secrecy via chain key ratcheting
"""
from __future__ import annotations

import base64
import os
import uuid
from dataclasses import dataclass
from typing import TYPE_CHECKING

from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives.hmac import HMAC

from src.crypto.primitives import aes_gcm_encrypt, aes_gcm_decrypt

if TYPE_CHECKING:
    from src.storage.client_db import ClientDB


@dataclass
class SenderKeyState:
    """State for a sender key in a group."""
    
    sender_key: bytes  # Initial sender key (32 bytes)
    chain_key: bytes   # Current chain key (32 bytes)
    message_index: int # Number of messages sent


class GroupManager:
    """Manages group messaging with Sender Keys.
    
    Sender Keys approach:
    1. Each member generates a sender key when joining
    2. Sender key is distributed to all members via 1:1 E2EE
    3. Each message uses a message key derived from chain key
    4. Chain key advances after each message (forward secrecy)
    5. Sender key is rotated when members leave
    """
    
    def __init__(self, db: ClientDB, username: str):
        """Initialize group manager.
        
        Args:
            db: Client database for persistence
            username: Local user's username
        """
        self.db = db
        self.username = username
        self._sender_states: dict[str, SenderKeyState] = {}  # group_id -> my state
        self._member_states: dict[tuple[str, str], SenderKeyState] = {}  # (group, member) -> state
    
    def create_group(self, name: str) -> str:
        """Create a new group.
        
        Args:
            name: Human-readable group name
            
        Returns:
            The new group's ID
        """
        group_id = str(uuid.uuid4())[:8]
        
        # Create group in database
        self.db.create_group(group_id, name, self.username)
        
        # Generate our sender key for this group
        sender_key = os.urandom(32)
        chain_key = self._derive_chain_key(sender_key, 0)
        
        self._sender_states[group_id] = SenderKeyState(
            sender_key=sender_key,
            chain_key=chain_key,
            message_index=0,
        )
        
        # Add ourselves as member
        self.db.add_group_member(group_id, self.username, sender_key, chain_key)
        
        return group_id
    
    def join_group(
        self,
        group_id: str,
        name: str,
        creator: str,
    ) -> bytes:
        """Join an existing group (when invited).
        
        Args:
            group_id: Group identifier
            name: Group name
            creator: Username of group creator
            
        Returns:
            Our sender key to distribute to other members
        """
        # Create group record if not exists
        if not self.db.get_group(group_id):
            self.db.create_group(group_id, name, creator)
        
        # Generate our sender key
        sender_key = os.urandom(32)
        chain_key = self._derive_chain_key(sender_key, 0)
        
        self._sender_states[group_id] = SenderKeyState(
            sender_key=sender_key,
            chain_key=chain_key,
            message_index=0,
        )
        
        # Add ourselves as member
        self.db.add_group_member(group_id, self.username, sender_key, chain_key)
        
        return sender_key
    
    def add_member(
        self,
        group_id: str,
        username: str,
        sender_key: bytes | None = None,
    ) -> bytes:
        """Add a member to a group.
        
        Args:
            group_id: Group identifier
            username: New member's username
            sender_key: Member's sender key (if already received)
            
        Returns:
            Our sender key for the new member to receive
            
        Raises:
            ValueError: If we are not a member of the group
        """
        # Ensure we have our sender state for this group
        if group_id not in self._sender_states:
            # Try to load from database
            members = self.db.get_group_members(group_id)
            for m in members:
                if m["username"] == self.username and m["sender_key"]:
                    self._sender_states[group_id] = SenderKeyState(
                        sender_key=m["sender_key"],
                        chain_key=m["chain_key"],
                        message_index=m["message_index"],
                    )
                    break
        
        if group_id not in self._sender_states:
            raise ValueError(f"Not a member of group {group_id}")
        
        chain_key = None
        if sender_key:
            chain_key = self._derive_chain_key(sender_key, 0)
            self._member_states[(group_id, username)] = SenderKeyState(
                sender_key=sender_key,
                chain_key=chain_key,
                message_index=0,
            )
        
        self.db.add_group_member(group_id, username, sender_key, chain_key)
        
        # Return our sender key
        return self._sender_states[group_id].sender_key

    def remove_member(self, group_id: str, username: str) -> bool:
        """Remove a member from local group state.

        Args:
            group_id: Group identifier
            username: Member username to remove

        Returns:
            True if membership existed and was removed, else False
        """
        removed = self.db.remove_group_member(group_id, username)
        self._member_states.pop((group_id, username), None)

        if username == self.username:
            self._sender_states.pop(group_id, None)
            stale_keys = [key for key in self._member_states if key[0] == group_id]
            for key in stale_keys:
                self._member_states.pop(key, None)

        return removed

    def rotate_own_sender_key(self, group_id: str) -> bytes:
        """Rotate local sender key for a group and persist reset chain state.

        Args:
            group_id: Group identifier

        Returns:
            New sender key bytes

        Raises:
            ValueError: If the local user is not a member of the group
        """
        if group_id not in self._sender_states:
            members = self.db.get_group_members(group_id)
            for member in members:
                if member["username"] == self.username and member["sender_key"]:
                    self._sender_states[group_id] = SenderKeyState(
                        sender_key=member["sender_key"],
                        chain_key=member["chain_key"],
                        message_index=member["message_index"],
                    )
                    break

        if group_id not in self._sender_states:
            raise ValueError(f"Not a member of group {group_id}")

        sender_key = os.urandom(32)
        chain_key = self._derive_chain_key(sender_key, 0)
        self._sender_states[group_id] = SenderKeyState(
            sender_key=sender_key,
            chain_key=chain_key,
            message_index=0,
        )
        self.db.add_group_member(group_id, self.username, sender_key, chain_key)
        return sender_key
    
    def receive_sender_key(
        self,
        group_id: str,
        username: str,
        sender_key: bytes,
    ) -> None:
        """Receive a member's sender key.
        
        Args:
            group_id: Group identifier
            username: Member's username
            sender_key: Member's sender key
        """
        chain_key = self._derive_chain_key(sender_key, 0)
        
        self._member_states[(group_id, username)] = SenderKeyState(
            sender_key=sender_key,
            chain_key=chain_key,
            message_index=0,
        )
        
        self.db.add_group_member(group_id, username, sender_key, chain_key)
    
    def encrypt_message(self, group_id: str, plaintext: str) -> dict:
        """Encrypt a message for the group.
        
        Args:
            group_id: Group identifier
            plaintext: Message text
            
        Returns:
            Encrypted message dictionary
        """
        if group_id not in self._sender_states:
            # Try to load from database
            members = self.db.get_group_members(group_id)
            for m in members:
                if m["username"] == self.username and m["sender_key"]:
                    self._sender_states[group_id] = SenderKeyState(
                        sender_key=m["sender_key"],
                        chain_key=m["chain_key"],
                        message_index=m["message_index"],
                    )
                    break
        
        if group_id not in self._sender_states:
            raise ValueError(f"Not a member of group {group_id}")
        
        state = self._sender_states[group_id]
        
        # Derive message key from chain key
        message_key = self._derive_message_key(state.chain_key)
        
        # Encrypt message
        nonce, ciphertext = aes_gcm_encrypt(
            message_key,
            plaintext.encode("utf-8"),
            aad=group_id.encode("utf-8"),
        )
        
        result = {
            "group_id": group_id,
            "sender": self.username,
            "message_index": state.message_index,
            "nonce": base64.b64encode(nonce).decode(),
            "ciphertext": base64.b64encode(ciphertext).decode(),
        }
        
        # Advance chain key
        state.chain_key = self._advance_chain_key(state.chain_key)
        state.message_index += 1
        
        # Persist state
        self.db.update_group_member_chain(
            group_id,
            self.username,
            state.chain_key,
            state.message_index,
        )
        
        return result
    
    def decrypt_message(self, encrypted: dict) -> str:
        """Decrypt a group message.
        
        Args:
            encrypted: Encrypted message dictionary with fields:
                - group_id: Group identifier
                - sender: Sender's username  
                - message_index: Message index for chain key derivation
                - nonce: Base64-encoded nonce
                - ciphertext: Base64-encoded ciphertext
            
        Returns:
            Decrypted message text
            
        Raises:
            ValueError: If required fields are missing, sender key not found,
                        or decryption fails
        """
        # Validate required fields
        required_fields = ["group_id", "sender", "message_index", "nonce", "ciphertext"]
        for field in required_fields:
            if field not in encrypted:
                raise ValueError(f"Missing required field: {field}")
        
        group_id = encrypted["group_id"]
        sender = encrypted["sender"]
        message_index = encrypted["message_index"]
        
        if not isinstance(message_index, int):
            raise ValueError(f"message_index must be an integer, got {type(message_index).__name__}")
        
        if sender == self.username:
            raise ValueError("Cannot decrypt own messages")
        
        # Get sender's state
        state_key = (group_id, sender)
        
        if state_key not in self._member_states:
            # Try to load from database
            members = self.db.get_group_members(group_id)
            for m in members:
                if m["username"] == sender and m["sender_key"]:
                    self._member_states[state_key] = SenderKeyState(
                        sender_key=m["sender_key"],
                        chain_key=m["chain_key"],
                        message_index=m["message_index"],
                    )
                    break
        
        if state_key not in self._member_states:
            raise ValueError(f"No sender key for {sender} in group {group_id}")
        
        state = self._member_states[state_key]
        
        # Handle out-of-order messages by advancing chain key
        while state.message_index < message_index:
            state.chain_key = self._advance_chain_key(state.chain_key)
            state.message_index += 1
        
        if state.message_index != message_index:
            raise ValueError(f"Message index mismatch: expected {state.message_index}, got {message_index}")
        
        # Derive message key
        message_key = self._derive_message_key(state.chain_key)
        
        # Decrypt
        nonce = base64.b64decode(encrypted["nonce"])
        ciphertext = base64.b64decode(encrypted["ciphertext"])
        
        plaintext = aes_gcm_decrypt(
            message_key,
            nonce,
            ciphertext,
            aad=group_id.encode("utf-8"),
        )
        
        # Advance chain key
        state.chain_key = self._advance_chain_key(state.chain_key)
        state.message_index += 1
        
        # Persist state
        self.db.update_group_member_chain(
            group_id,
            sender,
            state.chain_key,
            state.message_index,
        )
        
        return plaintext.decode("utf-8")
    
    def list_groups(self) -> list[dict]:
        """List all groups.
        
        Returns:
            List of group dictionaries
        """
        return self.db.list_groups()
    
    def get_group_members(self, group_id: str) -> list[str]:
        """Get members of a group.
        
        Args:
            group_id: Group identifier
            
        Returns:
            List of member usernames
        """
        members = self.db.get_group_members(group_id)
        return [m["username"] for m in members]
    
    def get_my_sender_key(self, group_id: str) -> bytes | None:
        """Get our sender key for a group.
        
        Args:
            group_id: Group identifier
            
        Returns:
            Sender key bytes, or None if not a member
        """
        if group_id in self._sender_states:
            return self._sender_states[group_id].sender_key
        
        # Try database
        members = self.db.get_group_members(group_id)
        for m in members:
            if m["username"] == self.username:
                return m["sender_key"]
        
        return None
    
    def _derive_chain_key(self, sender_key: bytes, index: int) -> bytes:
        """Derive initial chain key from sender key."""
        hmac = HMAC(sender_key, hashes.SHA256())
        hmac.update(f"ChainKey:{index}".encode())
        return hmac.finalize()
    
    def _derive_message_key(self, chain_key: bytes) -> bytes:
        """Derive message key from chain key."""
        hmac = HMAC(chain_key, hashes.SHA256())
        hmac.update(b"\x01")  # Message key constant
        return hmac.finalize()
    
    def _advance_chain_key(self, chain_key: bytes) -> bytes:
        """Advance chain key to next state."""
        hmac = HMAC(chain_key, hashes.SHA256())
        hmac.update(b"\x02")  # Chain key constant
        return hmac.finalize()
