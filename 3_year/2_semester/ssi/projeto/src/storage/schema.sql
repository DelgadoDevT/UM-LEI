-- This file defines the SQL statements for creating the server-side database tables.

CREATE TABLE IF NOT EXISTS users (
    username TEXT PRIMARY KEY,
    pw_hash  TEXT NOT NULL,
    ik_ed_pub  BLOB NOT NULL,
    ik_dh_pub  BLOB NOT NULL,
    spk_pub    BLOB NOT NULL,
    spk_sig    BLOB NOT NULL,
    created_at REAL NOT NULL
);

CREATE TABLE IF NOT EXISTS onetime_prekeys (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    username    TEXT NOT NULL REFERENCES users(username),
    opk_id      INTEGER NOT NULL,
    opk_pub     BLOB NOT NULL,
    used        INTEGER NOT NULL DEFAULT 0,
    UNIQUE(username, opk_id)
);

CREATE TABLE IF NOT EXISTS certificates (
    username   TEXT PRIMARY KEY REFERENCES users(username),
    cert_pem   TEXT NOT NULL,
    issued_at  REAL NOT NULL
);

CREATE TABLE IF NOT EXISTS offline_messages (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    recipient    TEXT NOT NULL REFERENCES users(username),
    sender       TEXT NOT NULL,
    payload_json TEXT NOT NULL,
    queued_at    REAL NOT NULL
);
