"""Provide client-side persistent storage abstractions.

Extend the shared storage backend with local client tables for contacts,
sessions, direct-message history, deduplication, and group state.
"""
from __future__ import annotations

import time
from typing import Any

from src.storage.secure_db import SecureDB
from src.storage.client_schema import CLIENT_SCHEMA


class ClientDB(SecureDB):
    """Store client-local contacts, sessions, messages, and group metadata."""
    
    def __init__(self, db_path: str, db_key: bytes):
        """Initialize the client database and ensure client tables exist.

        :param db_path: Path to the SQLite database file.
        :type db_path: str
        :param db_key: Opaque key material associated with this database.
        :type db_key: bytes
        :returns: None.
        :rtype: None
        """
        super().__init__(db_path, db_key)
        self._init_client_schema()
    
    def _init_client_schema(self) -> None:
        """Initialize client-specific tables."""
        cursor = self.conn.cursor()
        for statement in CLIENT_SCHEMA:
            cursor.execute(statement)
        self._migrate_client_schema(cursor)
        self.conn.commit()

    def _migrate_client_schema(self, cursor: Any) -> None:
        """Apply compatible schema migrations for existing client databases."""
        cursor.execute("PRAGMA table_info(contacts)")
        contact_columns = {row["name"] for row in cursor.fetchall()}
        if "spk_sig" not in contact_columns:
            cursor.execute("ALTER TABLE contacts ADD COLUMN spk_sig BLOB")
    
    # ==================== Contacts ====================
    
    def save_contact(
        self,
        username: str,
        ik_ed_pub: bytes,
        ik_dh_pub: bytes,
        spk_pub: bytes,
        spk_sig: bytes | None = None,
        p2p_host: str = "",
        p2p_port: int = 0,
        cert_pem: str | None = None,
    ) -> None:
        """Save or update one contact record.

        :param username: Contact username.
        :type username: str
        :param ik_ed_pub: Contact Ed25519 identity public key.
        :type ik_ed_pub: bytes
        :param ik_dh_pub: Contact X25519 identity public key.
        :type ik_dh_pub: bytes
        :param spk_pub: Contact signed prekey public key.
        :type spk_pub: bytes
        :param p2p_host: Optional advertised host for direct peer delivery.
        :type p2p_host: str
        :param p2p_port: Optional advertised port for direct peer delivery.
        :type p2p_port: int
        :param cert_pem: Optional contact certificate in PEM encoding.
        :type cert_pem: str | None
        :returns: None.
        :rtype: None
        """
        cursor = self.conn.cursor()
        cursor.execute(
            """INSERT OR REPLACE INTO contacts 
               (username, ik_ed_pub, ik_dh_pub, spk_pub, spk_sig, p2p_host, p2p_port, cert_pem, added_at)
               VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)""",
            (
                username,
                ik_ed_pub,
                ik_dh_pub,
                spk_pub,
                spk_sig,
                p2p_host,
                p2p_port,
                cert_pem,
                time.time(),
            )
        )
        self.conn.commit()
    
    def get_contact(self, username: str) -> dict | None:
        """Return one contact record by username.

        :param username: Contact username.
        :type username: str
        :returns: Contact data when present, otherwise ``None``.
        :rtype: dict | None
        """
        cursor = self.conn.cursor()
        cursor.execute(
            "SELECT * FROM contacts WHERE username = ?",
            (username,)
        )
        row = cursor.fetchone()
        return dict(row) if row else None
    
    def list_contacts(self) -> list[dict]:
        """Return all stored contacts ordered by username.

        :returns: Contact dictionaries.
        :rtype: list[dict]
        """
        cursor = self.conn.cursor()
        cursor.execute("SELECT * FROM contacts ORDER BY username")
        return [dict(row) for row in cursor.fetchall()]
    
    def delete_contact(self, username: str) -> bool:
        """Delete one contact by username.

        :param username: Contact username.
        :type username: str
        :returns: ``True`` when a row was deleted, else ``False``.
        :rtype: bool
        """
        cursor = self.conn.cursor()
        cursor.execute(
            "DELETE FROM contacts WHERE username = ?",
            (username,)
        )
        self.conn.commit()
        return cursor.rowcount > 0
    
    def update_contact_spk(self, username: str, spk_pub: bytes) -> bool:
        """Update the signed prekey for one contact.

        :param username: Contact username.
        :type username: str
        :param spk_pub: New signed prekey public key.
        :type spk_pub: bytes
        :returns: ``True`` when a row was updated, else ``False``.
        :rtype: bool
        """
        cursor = self.conn.cursor()
        cursor.execute(
            "UPDATE contacts SET spk_pub = ? WHERE username = ?",
            (spk_pub, username)
        )
        self.conn.commit()
        return cursor.rowcount > 0
    
    # ==================== Sessions ====================
    
    def save_session(self, peer_username: str, state_json: str) -> None:
        """Save or replace ratchet state for one peer.

        :param peer_username: Peer username.
        :type peer_username: str
        :param state_json: JSON-serialized ratchet state.
        :type state_json: str
        :returns: None.
        :rtype: None
        """
        cursor = self.conn.cursor()
        cursor.execute(
            """INSERT OR REPLACE INTO sessions 
               (peer_username, state_json, updated_at)
               VALUES (?, ?, ?)""",
            (peer_username, state_json, time.time())
        )
        self.conn.commit()
    
    def get_session(self, peer_username: str) -> str | None:
        """Return ratchet state for one peer.

        :param peer_username: Peer username.
        :type peer_username: str
        :returns: JSON ratchet state when present, otherwise ``None``.
        :rtype: str | None
        """
        cursor = self.conn.cursor()
        cursor.execute(
            "SELECT state_json FROM sessions WHERE peer_username = ?",
            (peer_username,)
        )
        row = cursor.fetchone()
        return row["state_json"] if row else None
    
    def delete_session(self, peer_username: str) -> None:
        """Delete ratchet state for one peer.

        :param peer_username: Peer username.
        :type peer_username: str
        :returns: None.
        :rtype: None
        """
        cursor = self.conn.cursor()
        cursor.execute(
            "DELETE FROM sessions WHERE peer_username = ?",
            (peer_username,)
        )
        self.conn.commit()
    
    # ==================== Messages ====================
    
    def save_message(
        self,
        peer: str,
        direction: str,
        plaintext: str,
        timestamp: float | None = None,
    ) -> None:
        """Append one direct-message entry to local history.

        :param peer: Username of the other party.
        :type peer: str
        :param direction: Direction label, usually ``sent`` or ``received``.
        :type direction: str
        :param plaintext: Decrypted message content.
        :type plaintext: str
        :param timestamp: Optional message timestamp; defaults to current time.
        :type timestamp: float | None
        :returns: None.
        :rtype: None
        """
        if timestamp is None:
            timestamp = time.time()
        
        cursor = self.conn.cursor()
        cursor.execute(
            """INSERT INTO messages (peer, direction, plaintext, timestamp)
               VALUES (?, ?, ?, ?)""",
            (peer, direction, plaintext, timestamp)
        )
        self.conn.commit()
    
    def get_messages(
        self,
        peer: str,
        limit: int = 50,
        offset: int = 0,
    ) -> list[dict]:
        """Return direct-message history with one peer.

        :param peer: Username of the other party.
        :type peer: str
        :param limit: Maximum number of rows to return.
        :type limit: int
        :param offset: Number of rows to skip from the start.
        :type offset: int
        :returns: Message dictionaries ordered from oldest to newest.
        :rtype: list[dict]
        """
        cursor = self.conn.cursor()
        cursor.execute(
            """SELECT * FROM messages 
               WHERE peer = ?
               ORDER BY timestamp ASC
               LIMIT ? OFFSET ?""",
            (peer, limit, offset)
        )
        return [dict(row) for row in cursor.fetchall()]

    def has_seen_message(self, msg_id: str) -> bool:
        """Return whether a message identifier was already processed.

        :param msg_id: Message identifier to check.
        :type msg_id: str
        :returns: ``True`` when the identifier is already recorded.
        :rtype: bool
        """
        if not msg_id:
            return False
        cursor = self.conn.cursor()
        cursor.execute(
            "SELECT 1 FROM message_dedup WHERE msg_id = ?",
            (msg_id,),
        )
        return cursor.fetchone() is not None

    def mark_message_seen(self, msg_id: str, seen_at: float | None = None) -> None:
        """Record one message identifier and prune stale dedup entries.

        :param msg_id: Message identifier to record.
        :type msg_id: str
        :param seen_at: Optional event time; defaults to current time.
        :type seen_at: float | None
        :returns: None.
        :rtype: None
        """
        if not msg_id:
            return
        if seen_at is None:
            seen_at = time.time()
        cursor = self.conn.cursor()
        cursor.execute(
            "INSERT OR REPLACE INTO message_dedup (msg_id, seen_at) VALUES (?, ?)",
            (msg_id, seen_at),
        )
        cursor.execute(
            "DELETE FROM message_dedup WHERE seen_at < ?",
            (seen_at - 86400.0,),
        )
        self.conn.commit()
    
    # ==================== Groups ====================
    
    def create_group(self, group_id: str, name: str, creator: str) -> None:
        """Create a new group record.

        :param group_id: Unique group identifier.
        :type group_id: str
        :param name: Human-readable group name.
        :type name: str
        :param creator: Username of the group creator.
        :type creator: str
        :returns: None.
        :rtype: None
        """
        cursor = self.conn.cursor()
        cursor.execute(
            """INSERT INTO groups (group_id, name, created_at, creator)
               VALUES (?, ?, ?, ?)""",
            (group_id, name, time.time(), creator)
        )
        self.conn.commit()
    
    def get_group(self, group_id: str) -> dict | None:
        """Return metadata for one group.

        :param group_id: Group identifier.
        :type group_id: str
        :returns: Group dictionary when present, otherwise ``None``.
        :rtype: dict | None
        """
        cursor = self.conn.cursor()
        cursor.execute(
            "SELECT * FROM groups WHERE group_id = ?",
            (group_id,)
        )
        row = cursor.fetchone()
        return dict(row) if row else None
    
    def list_groups(self) -> list[dict]:
        """Return all groups ordered by name.

        :returns: Group dictionaries.
        :rtype: list[dict]
        """
        cursor = self.conn.cursor()
        cursor.execute("SELECT * FROM groups ORDER BY name")
        return [dict(row) for row in cursor.fetchall()]
    
    def add_group_member(
        self,
        group_id: str,
        username: str,
        sender_key: bytes | None = None,
        chain_key: bytes | None = None,
    ) -> None:
        """Add or update one group member entry.

        :param group_id: Group identifier.
        :type group_id: str
        :param username: Member username.
        :type username: str
        :param sender_key: Optional sender key used for group decryption.
        :type sender_key: bytes | None
        :param chain_key: Optional current chain-key state.
        :type chain_key: bytes | None
        :returns: None.
        :rtype: None
        """
        cursor = self.conn.cursor()
        cursor.execute(
            """INSERT OR REPLACE INTO group_members 
               (group_id, username, sender_key, chain_key, message_index, joined_at)
               VALUES (?, ?, ?, ?, 0, ?)""",
            (group_id, username, sender_key, chain_key, time.time())
        )
        self.conn.commit()
    
    def get_group_members(self, group_id: str) -> list[dict]:
        """Return all members of one group.

        :param group_id: Group identifier.
        :type group_id: str
        :returns: Member dictionaries ordered by username.
        :rtype: list[dict]
        """
        cursor = self.conn.cursor()
        cursor.execute(
            "SELECT * FROM group_members WHERE group_id = ? ORDER BY username",
            (group_id,)
        )
        return [dict(row) for row in cursor.fetchall()]

    def remove_group_member(self, group_id: str, username: str) -> bool:
        """Remove one member from a group.

        :param group_id: Group identifier.
        :type group_id: str
        :param username: Member username.
        :type username: str
        :returns: ``True`` when a row was removed, else ``False``.
        :rtype: bool
        """
        cursor = self.conn.cursor()
        cursor.execute(
            "DELETE FROM group_members WHERE group_id = ? AND username = ?",
            (group_id, username),
        )
        self.conn.commit()
        return cursor.rowcount > 0
    
    def update_group_member_chain(
        self,
        group_id: str,
        username: str,
        chain_key: bytes,
        message_index: int,
    ) -> None:
        """Update one member chain-key state and message index.

        :param group_id: Group identifier.
        :type group_id: str
        :param username: Member username.
        :type username: str
        :param chain_key: Updated chain-key bytes.
        :type chain_key: bytes
        :param message_index: Updated message index.
        :type message_index: int
        :returns: None.
        :rtype: None
        """
        cursor = self.conn.cursor()
        cursor.execute(
            """UPDATE group_members 
               SET chain_key = ?, message_index = ?
               WHERE group_id = ? AND username = ?""",
            (chain_key, message_index, group_id, username)
        )
        self.conn.commit()
    
    def save_group_message(
        self,
        group_id: str,
        sender: str,
        plaintext: str,
        timestamp: float | None = None,
    ) -> None:
        """Append one group-message entry to local history.

        :param group_id: Group identifier.
        :type group_id: str
        :param sender: Sender username.
        :type sender: str
        :param plaintext: Decrypted message content.
        :type plaintext: str
        :param timestamp: Optional message timestamp; defaults to current time.
        :type timestamp: float | None
        :returns: None.
        :rtype: None
        """
        if timestamp is None:
            timestamp = time.time()
        
        cursor = self.conn.cursor()
        cursor.execute(
            """INSERT INTO group_messages (group_id, sender, plaintext, timestamp)
               VALUES (?, ?, ?, ?)""",
            (group_id, sender, plaintext, timestamp)
        )
        self.conn.commit()
    
    def get_group_messages(
        self,
        group_id: str,
        limit: int = 50,
        offset: int = 0,
    ) -> list[dict]:
        """Return group-message history for one group.

        :param group_id: Group identifier.
        :type group_id: str
        :param limit: Maximum number of rows to return.
        :type limit: int
        :param offset: Number of rows to skip from the start.
        :type offset: int
        :returns: Message dictionaries ordered from oldest to newest.
        :rtype: list[dict]
        """
        cursor = self.conn.cursor()
        cursor.execute(
            """SELECT * FROM group_messages 
               WHERE group_id = ?
               ORDER BY timestamp ASC
               LIMIT ? OFFSET ?""",
            (group_id, limit, offset)
        )
        return [dict(row) for row in cursor.fetchall()]
