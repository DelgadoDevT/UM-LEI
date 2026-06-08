"""
SQL schema definitions for the server-side database.

This module contains the CREATE TABLE statements as Python string constants
for use with the SecureDB class.
"""

CREATE_TABLE_USERS = """
CREATE TABLE IF NOT EXISTS users (
    username TEXT PRIMARY KEY,
    pw_hash  TEXT NOT NULL,
    ik_ed_pub  BLOB NOT NULL,
    ik_dh_pub  BLOB NOT NULL,
    spk_pub    BLOB NOT NULL,
    spk_sig    BLOB NOT NULL,
    p2p_host   TEXT NOT NULL DEFAULT '',
    p2p_port   INTEGER NOT NULL DEFAULT 0,
    created_at REAL NOT NULL
);
"""

CREATE_TABLE_ONETIME_PREKEYS = """
CREATE TABLE IF NOT EXISTS onetime_prekeys (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    username    TEXT NOT NULL REFERENCES users(username),
    opk_id      INTEGER NOT NULL,
    opk_pub     BLOB NOT NULL,
    used        INTEGER NOT NULL DEFAULT 0,
    UNIQUE(username, opk_id)
);
"""

CREATE_TABLE_CERTIFICATES = """
CREATE TABLE IF NOT EXISTS certificates (
    username   TEXT PRIMARY KEY REFERENCES users(username),
    cert_pem   TEXT NOT NULL,
    issued_at  REAL NOT NULL
);
"""

CREATE_TABLE_OFFLINE_MESSAGES = """
CREATE TABLE IF NOT EXISTS offline_messages (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    recipient    TEXT NOT NULL REFERENCES users(username),
    sender       TEXT NOT NULL,
    payload_json TEXT NOT NULL,
    queued_at    REAL NOT NULL
);
"""
