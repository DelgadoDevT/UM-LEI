"""Additional storage-focused tests for Person B scope."""

from __future__ import annotations

import json
import os
import sqlite3
import tempfile
from pathlib import Path

import pytest

from src.storage.secure_db import SecureDB


@pytest.fixture
def temp_db_path():
	"""Provide a temporary SQLite path for storage tests."""
	with tempfile.TemporaryDirectory() as tmpdir:
		yield Path(tmpdir) / "storage_test.db"


@pytest.fixture
def test_db(temp_db_path):
	"""Provide an initialized SecureDB instance for storage tests."""
	db = SecureDB(str(temp_db_path), os.urandom(32))
	yield db
	db.close()


def test_offline_storage_handles_large_queue_limit_without_loss(test_db):
	"""Large offline queues should be fully persisted and delivered once."""
	recipient = "offline_user"
	limit = 300
	for i in range(limit):
		test_db.store_offline_message(
			recipient,
			f"sender_{i % 7}",
			json.dumps({"seq": i, "ciphertext": f"ct_{i}"}),
		)

	messages = test_db.pop_offline_messages(recipient)
	assert len(messages) == limit
	assert test_db.pop_offline_messages(recipient) == []


def test_offline_storage_preserves_fifo_at_limit_boundary(test_db):
	"""Offline message retrieval must preserve queued order at a boundary size."""
	recipient = "ordered_user"
	limit = 128
	for i in range(limit):
		test_db.store_offline_message(recipient, "alice", json.dumps({"seq": i}))

	retrieved = test_db.pop_offline_messages(recipient)
	seqs = [json.loads(raw)["seq"] for raw in retrieved]
	assert seqs == list(range(limit))


def test_local_database_corruption_is_detected_on_reopen(temp_db_path):
	"""Corrupted local DB files should fail safely with a database error."""
	db = SecureDB(str(temp_db_path), os.urandom(32))
	db.store_user("alice", "pw_hash", os.urandom(32), os.urandom(32), os.urandom(32), os.urandom(64))
	db.close()

	with open(temp_db_path, "r+b") as f:
		f.seek(0)
		f.write(b"CORRUPTED_SQLITE_HEADER")

	with pytest.raises(sqlite3.DatabaseError):
		SecureDB(str(temp_db_path), os.urandom(32))


def test_legacy_users_table_is_auto_migrated(temp_db_path):
	"""Legacy users schema without P2P columns should be upgraded on open."""
	conn = sqlite3.connect(temp_db_path)
	conn.execute(
		"""
		CREATE TABLE users (
			username TEXT PRIMARY KEY,
			pw_hash  TEXT NOT NULL,
			ik_ed_pub  BLOB NOT NULL,
			ik_dh_pub  BLOB NOT NULL,
			spk_pub    BLOB NOT NULL,
			spk_sig    BLOB NOT NULL,
			created_at REAL NOT NULL
		)
		"""
	)
	conn.execute(
		"""
		CREATE TABLE certificates (
			username   TEXT PRIMARY KEY,
			cert_pem   TEXT NOT NULL,
			issued_at  REAL NOT NULL
		)
		"""
	)
	conn.execute(
		"""
		CREATE TABLE onetime_prekeys (
			id          INTEGER PRIMARY KEY AUTOINCREMENT,
			username    TEXT NOT NULL,
			opk_id      INTEGER NOT NULL,
			opk_pub     BLOB NOT NULL,
			used        INTEGER NOT NULL DEFAULT 0,
			UNIQUE(username, opk_id)
		)
		"""
	)
	conn.execute(
		"""
		CREATE TABLE offline_messages (
			id           INTEGER PRIMARY KEY AUTOINCREMENT,
			recipient    TEXT NOT NULL,
			sender       TEXT NOT NULL,
			payload_json TEXT NOT NULL,
			queued_at    REAL NOT NULL
		)
		"""
	)
	conn.commit()
	conn.close()

	db = SecureDB(str(temp_db_path), os.urandom(32))
	try:
		columns = {
			row["name"]
			for row in db.conn.execute("PRAGMA table_info(users)").fetchall()
		}
		assert "p2p_host" in columns
		assert "p2p_port" in columns

		db.store_user(
			"legacy_user",
			"pw_hash",
			os.urandom(32),
			os.urandom(32),
			os.urandom(32),
			os.urandom(64),
			p2p_host="127.0.0.1",
			p2p_port=5555,
		)
	finally:
		db.close()
