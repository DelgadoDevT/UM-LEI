"""Asymmetric primitives used by X3DH and message authentication."""

from __future__ import annotations

from cryptography.exceptions import InvalidSignature
from cryptography.hazmat.primitives.asymmetric.ed25519 import (
	Ed25519PrivateKey,
	Ed25519PublicKey,
)
from cryptography.hazmat.primitives.asymmetric.x25519 import (
	X25519PrivateKey,
	X25519PublicKey,
)
from cryptography.hazmat.primitives.serialization import (
	Encoding,
	NoEncryption,
	PrivateFormat,
	PublicFormat,
)


def generate_ed25519_keypair() -> tuple[Ed25519PrivateKey, Ed25519PublicKey]:
	"""Generate a long-term Ed25519 keypair for signatures."""
	private_key = Ed25519PrivateKey.generate()
	return private_key, private_key.public_key()


def generate_x25519_keypair() -> tuple[X25519PrivateKey, X25519PublicKey]:
	"""Generate a long-term X25519 keypair for Diffie-Hellman."""
	private_key = X25519PrivateKey.generate()
	return private_key, private_key.public_key()


def ed25519_sign(private_key: Ed25519PrivateKey, data: bytes) -> bytes:
	"""Sign arbitrary bytes and return the raw 64-byte signature."""
	return private_key.sign(data)


def ed25519_verify(public_key: Ed25519PublicKey, signature: bytes, data: bytes) -> bool:
	"""Verify Ed25519 signature and return False on failure without raising."""
	try:
		public_key.verify(signature, data)
	except InvalidSignature:
		return False
	return True


def x25519_exchange(private_key: X25519PrivateKey, peer_public_key: X25519PublicKey) -> bytes:
	"""Compute the 32-byte X25519 Diffie-Hellman shared secret."""
	return private_key.exchange(peer_public_key)


def serialize_public_key(key: Ed25519PublicKey | X25519PublicKey) -> bytes:
	"""Serialize Ed25519 or X25519 public key into raw 32-byte format."""
	return key.public_bytes(encoding=Encoding.Raw, format=PublicFormat.Raw)


def deserialize_ed25519_public(raw: bytes) -> Ed25519PublicKey:
	"""Load an Ed25519 public key from raw 32-byte representation."""
	if len(raw) != 32:
		raise ValueError("Ed25519 public key must be 32 bytes")
	return Ed25519PublicKey.from_public_bytes(raw)


def deserialize_x25519_public(raw: bytes) -> X25519PublicKey:
	"""Load an X25519 public key from raw 32-byte representation."""
	if len(raw) != 32:
		raise ValueError("X25519 public key must be 32 bytes")
	return X25519PublicKey.from_public_bytes(raw)


def serialize_private_key(key: Ed25519PrivateKey | X25519PrivateKey) -> bytes:
	"""Serialize Ed25519/X25519 private key into raw bytes for secure local storage."""
	return key.private_bytes(
		encoding=Encoding.Raw,
		format=PrivateFormat.Raw,
		encryption_algorithm=NoEncryption(),
	)


def deserialize_ed25519_private(raw: bytes) -> Ed25519PrivateKey:
	"""Load an Ed25519 private key from raw 32-byte representation."""
	if len(raw) != 32:
		raise ValueError("Ed25519 private key must be 32 bytes")
	return Ed25519PrivateKey.from_private_bytes(raw)


def deserialize_x25519_private(raw: bytes) -> X25519PrivateKey:
	"""Load an X25519 private key from raw 32-byte representation."""
	if len(raw) != 32:
		raise ValueError("X25519 private key must be 32 bytes")
	return X25519PrivateKey.from_private_bytes(raw)
