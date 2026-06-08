"""Provide persistent storage helpers for server-side state.

Wrap SQLite operations for users, certificates, one-time prekeys, and offline
messages behind a small synchronous API used by server components.
"""
import sqlite3
import time
from src.storage import schema

class SecureDB:
    """Store and retrieve chat-server data in SQLite.

    Manage schema initialization and CRUD operations used by authentication,
    bundle retrieval, certificate lookup, and offline message queuing.

    :ivar conn: Open SQLite connection used by all storage operations.
    :vartype conn: sqlite3.Connection
    :ivar _key: Database key material reserved for storage hardening.
    :vartype _key: bytes
    """

    def __init__(self, db_path, db_key):
        """Initialize the database connection and schema.

        :param db_path: File path to the SQLite database.
        :type db_path: str
        :param db_key: Opaque key material associated with this database.
        :type db_key: bytes
        :returns: None.
        :rtype: None
        :raises sqlite3.Error: If SQLite cannot open or configure the database.
        """
        self.conn = sqlite3.connect(db_path, check_same_thread=False)
        self.conn.row_factory = sqlite3.Row
        self._key = db_key
        self._init_schema()

    def _init_schema(self):
        """Create required tables and apply lightweight schema migrations."""
        cursor = self.conn.cursor()
        cursor.execute(schema.CREATE_TABLE_USERS)
        cursor.execute(schema.CREATE_TABLE_ONETIME_PREKEYS)
        cursor.execute(schema.CREATE_TABLE_CERTIFICATES)
        cursor.execute(schema.CREATE_TABLE_OFFLINE_MESSAGES)

        # Backward-compatible migration for older databases that predate
        # peer endpoint columns on `users`.
        self._migrate_users_table(cursor)

        self.conn.commit()

    def _migrate_users_table(self, cursor):
        """Add missing columns to `users` for legacy database files."""
        cursor.execute("PRAGMA table_info(users)")
        existing_columns = {row["name"] for row in cursor.fetchall()}

        if "p2p_host" not in existing_columns:
            cursor.execute("ALTER TABLE users ADD COLUMN p2p_host TEXT NOT NULL DEFAULT ''")

        if "p2p_port" not in existing_columns:
            cursor.execute("ALTER TABLE users ADD COLUMN p2p_port INTEGER NOT NULL DEFAULT 0")

    def store_user(
        self,
        username,
        pw_hash,
        ik_ed_pub,
        ik_dh_pub,
        spk_pub,
        spk_sig,
        p2p_host="",
        p2p_port=0,
    ):
        """Insert a new user identity and signed-prekey metadata.

        :param username: Unique account username.
        :type username: str
        :param pw_hash: Password hash stored for authentication.
        :type pw_hash: str
        :param ik_ed_pub: Base64-encoded Ed25519 identity public key.
        :type ik_ed_pub: str
        :param ik_dh_pub: Base64-encoded X25519 identity public key.
        :type ik_dh_pub: str
        :param spk_pub: Base64-encoded signed prekey public key.
        :type spk_pub: str
        :param spk_sig: Signature over the signed prekey.
        :type spk_sig: str
        :param p2p_host: Optional host used for direct peer connectivity.
        :type p2p_host: str
        :param p2p_port: Optional port used for direct peer connectivity.
        :type p2p_port: int
        :returns: None.
        :rtype: None
        :raises sqlite3.IntegrityError: If the username already exists.
        :raises sqlite3.Error: If persistence fails.
        """
        cursor = self.conn.cursor()
        cursor.execute(
            "INSERT INTO users (username, pw_hash, ik_ed_pub, ik_dh_pub, spk_pub, spk_sig, p2p_host, p2p_port, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
            (username, pw_hash, ik_ed_pub, ik_dh_pub, spk_pub, spk_sig, p2p_host, p2p_port, time.time())
        )
        self.conn.commit()

    def get_user_hash(self, username):
        """Return the stored password hash for a user.

        :param username: Username to query.
        :type username: str
        :returns: Stored password hash when the user exists, else ``None``.
        :rtype: str | None
        :raises sqlite3.Error: If the lookup query fails.
        """
        cursor = self.conn.cursor()
        cursor.execute("SELECT pw_hash FROM users WHERE username = ?", (username,))
        row = cursor.fetchone()
        return row['pw_hash'] if row else None

    def update_user_hash(self, username, pw_hash):
        """Update the stored password hash for one user.

        :param username: Username to update.
        :type username: str
        :param pw_hash: New password hash representation.
        :type pw_hash: str
        :returns: None.
        :rtype: None
        """
        cursor = self.conn.cursor()
        cursor.execute(
            "UPDATE users SET pw_hash = ? WHERE username = ?",
            (pw_hash, username),
        )
        self.conn.commit()

    def get_user_bundle(self, username):
        """Build and return a delivery bundle for a user.

        Fetch long-term public keys, consume one unused one-time prekey, and
        attach the latest certificate PEM when present.

        :param username: Target username whose bundle should be returned.
        :type username: str
        :returns: Bundle dictionary for delivery, or ``None`` when absent.
        :rtype: dict[str, object] | None
        :raises sqlite3.Error: If any underlying query fails.
        """
        cursor = self.conn.cursor()
        cursor.execute(
            "SELECT ik_ed_pub, ik_dh_pub, spk_pub, spk_sig, p2p_host, p2p_port FROM users WHERE username = ?",
            (username,)
        )
        user_row = cursor.fetchone()
        if not user_row:
            return None
        
        bundle = dict(user_row)
        
        opk = self.pop_onetime_prekey(username)
        bundle['opk_id'] = opk['opk_id'] if opk else None
        bundle['opk_pub'] = opk['opk_pub'] if opk else None
            
        bundle['cert_pem'] = self.get_certificate(username)
        
        return bundle

    def update_user_endpoint(self, username, p2p_host, p2p_port):
        """Update the peer-to-peer endpoint metadata for a user.

        :param username: Username to update.
        :type username: str
        :param p2p_host: New peer host value.
        :type p2p_host: str
        :param p2p_port: New peer port value.
        :type p2p_port: int
        :returns: None.
        :rtype: None
        :raises sqlite3.Error: If the update operation fails.
        """
        cursor = self.conn.cursor()
        cursor.execute(
            "UPDATE users SET p2p_host = ?, p2p_port = ? WHERE username = ?",
            (p2p_host, p2p_port, username),
        )
        self.conn.commit()

    def store_onetime_prekeys(self, username, opks):
        """Persist one-time prekeys for later bundle consumption.

        :param username: Owner username for all provided prekeys.
        :type username: str
        :param opks: Iterable of ``(opk_id, opk_pub)`` pairs to store.
        :type opks: list[tuple[int, str]]
        :returns: None.
        :rtype: None
        :raises sqlite3.Error: If insert or commit fails.
        """
        cursor = self.conn.cursor()
        keys_to_store = [(username, opk_id, opk_pub) for opk_id, opk_pub in opks]
        cursor.executemany(
            "INSERT INTO onetime_prekeys (username, opk_id, opk_pub) VALUES (?, ?, ?)",
            keys_to_store
        )
        self.conn.commit()

    def pop_onetime_prekey(self, username):
        """Atomically reserve and return one unused one-time prekey.

        :param username: Username whose prekey queue is consumed.
        :type username: str
        :returns: Selected prekey row as a dictionary, or ``None`` when empty.
        :rtype: dict[str, object] | None
        :raises Exception: Propagated database error after transaction rollback.
        """
        cursor = self.conn.cursor()
        cursor.execute("BEGIN EXCLUSIVE")
        try:
            cursor.execute(
                "SELECT id, opk_id, opk_pub FROM onetime_prekeys "
                "WHERE username = ? AND used = 0 ORDER BY id LIMIT 1",
                (username,)
            )
            row = cursor.fetchone()
            if not row:
                self.conn.commit()
                return None
            
            row_id = row['id']
            cursor.execute("UPDATE onetime_prekeys SET used = 1 WHERE id = ?", (row_id,))
            self.conn.commit()
            return dict(row)
        except Exception as e:
            self.conn.rollback()
            raise e

    def store_certificate(self, username, cert_pem):
        """Store or replace the PEM certificate associated with a user.

        :param username: Username owning the certificate.
        :type username: str
        :param cert_pem: Certificate serialized in PEM format.
        :type cert_pem: str
        :returns: None.
        :rtype: None
        :raises sqlite3.Error: If insert or commit fails.
        """
        cursor = self.conn.cursor()
        cursor.execute(
            "INSERT OR REPLACE INTO certificates (username, cert_pem, issued_at) VALUES (?, ?, ?)",
            (username, cert_pem, time.time())
        )
        self.conn.commit()

    def get_certificate(self, username):
        """Return a stored PEM certificate for a user.

        :param username: Username whose certificate is queried.
        :type username: str
        :returns: Stored certificate PEM when present, else ``None``.
        :rtype: str | None
        :raises sqlite3.Error: If the query fails.
        """
        cursor = self.conn.cursor()
        cursor.execute("SELECT cert_pem FROM certificates WHERE username = ?", (username,))
        row = cursor.fetchone()
        return row['cert_pem'] if row else None

    def store_offline_message(self, recipient, sender, payload_json):
        """Queue one encrypted payload for offline recipient delivery.

        :param recipient: Username that should receive the message later.
        :type recipient: str
        :param sender: Username that produced the message.
        :type sender: str
        :param payload_json: Serialized message payload JSON string.
        :type payload_json: str
        :returns: None.
        :rtype: None
        :raises sqlite3.Error: If insert or commit fails.
        """
        cursor = self.conn.cursor()
        cursor.execute(
            "INSERT INTO offline_messages (recipient, sender, payload_json, queued_at) VALUES (?, ?, ?, ?)",
            (recipient, sender, payload_json, time.time())
        )
        self.conn.commit()

    def pop_offline_messages(self, recipient):
        """Atomically return and clear queued offline messages.

        :param recipient: Username whose queue should be drained.
        :type recipient: str
        :returns: Ordered list of serialized payload JSON strings.
        :rtype: list[str]
        :raises Exception: Propagated database error after transaction rollback.
        """
        cursor = self.conn.cursor()
        cursor.execute("BEGIN EXCLUSIVE")
        try:
            cursor.execute(
                "SELECT payload_json FROM offline_messages "
                "WHERE recipient = ? ORDER BY queued_at ASC",
                (recipient,)
            )
            rows = cursor.fetchall()
            if not rows:
                self.conn.commit()
                return []
            
            cursor.execute("DELETE FROM offline_messages WHERE recipient = ?", (recipient,))
            self.conn.commit()
            return [row['payload_json'] for row in rows]
        except Exception as e:
            self.conn.rollback()
            raise e
        
    def close(self):
        """Close the underlying SQLite connection.

        :returns: None.
        :rtype: None
        """
        self.conn.close()
