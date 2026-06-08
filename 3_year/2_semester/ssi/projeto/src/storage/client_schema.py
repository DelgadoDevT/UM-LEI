"""
SQL schema definitions for the client-side database.

Stores:
- Contact information (public keys, certificates)
- Ratchet session states
- Message history
- Group information
"""

# Contacts table - stores known contacts and their public keys
CREATE_TABLE_CONTACTS = """
CREATE TABLE IF NOT EXISTS contacts (
    username    TEXT PRIMARY KEY,
    ik_ed_pub   BLOB NOT NULL,
    ik_dh_pub   BLOB NOT NULL,
    spk_pub     BLOB NOT NULL,
    spk_sig     BLOB,
    p2p_host    TEXT NOT NULL DEFAULT '',
    p2p_port    INTEGER NOT NULL DEFAULT 0,
    cert_pem    TEXT,
    added_at    REAL NOT NULL
);
"""

# Ratchet sessions - stores Double Ratchet state per peer
CREATE_TABLE_SESSIONS = """
CREATE TABLE IF NOT EXISTS sessions (
    peer_username   TEXT PRIMARY KEY,
    state_json      TEXT NOT NULL,
    updated_at      REAL NOT NULL
);
"""

# Message history - stores sent and received messages
CREATE_TABLE_MESSAGES = """
CREATE TABLE IF NOT EXISTS messages (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    peer        TEXT NOT NULL,
    direction   TEXT NOT NULL CHECK (direction IN ('sent', 'received')),
    plaintext   TEXT NOT NULL,
    timestamp   REAL NOT NULL
);
"""

CREATE_INDEX_MESSAGES_PEER = """
CREATE INDEX IF NOT EXISTS idx_messages_peer ON messages(peer, timestamp);
"""

# Message dedup table - tracks processed message IDs to avoid duplicates
CREATE_TABLE_MESSAGE_DEDUP = """
CREATE TABLE IF NOT EXISTS message_dedup (
    msg_id     TEXT PRIMARY KEY,
    seen_at    REAL NOT NULL
);
"""

# Groups table - stores group metadata
CREATE_TABLE_GROUPS = """
CREATE TABLE IF NOT EXISTS groups (
    group_id    TEXT PRIMARY KEY,
    name        TEXT NOT NULL,
    created_at  REAL NOT NULL,
    creator     TEXT NOT NULL
);
"""

# Group members - stores members and their sender keys
CREATE_TABLE_GROUP_MEMBERS = """
CREATE TABLE IF NOT EXISTS group_members (
    group_id        TEXT NOT NULL,
    username        TEXT NOT NULL,
    sender_key      BLOB,
    chain_key       BLOB,
    message_index   INTEGER DEFAULT 0,
    joined_at       REAL NOT NULL,
    PRIMARY KEY (group_id, username)
);
"""

# Group messages - stores group message history
CREATE_TABLE_GROUP_MESSAGES = """
CREATE TABLE IF NOT EXISTS group_messages (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    group_id    TEXT NOT NULL,
    sender      TEXT NOT NULL,
    plaintext   TEXT NOT NULL,
    timestamp   REAL NOT NULL
);
"""

CREATE_INDEX_GROUP_MESSAGES = """
CREATE INDEX IF NOT EXISTS idx_group_messages ON group_messages(group_id, timestamp);
"""

# All client schema statements
CLIENT_SCHEMA = [
    CREATE_TABLE_CONTACTS,
    CREATE_TABLE_SESSIONS,
    CREATE_TABLE_MESSAGES,
    CREATE_INDEX_MESSAGES_PEER,
    CREATE_TABLE_MESSAGE_DEDUP,
    CREATE_TABLE_GROUPS,
    CREATE_TABLE_GROUP_MEMBERS,
    CREATE_TABLE_GROUP_MESSAGES,
    CREATE_INDEX_GROUP_MESSAGES,
]
