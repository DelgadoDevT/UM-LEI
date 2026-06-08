"""
Key management for E2EE client.

Handles generation and storage of:
- Identity Key pairs (Ed25519 for signing, X25519 for DH)
- Signed Prekey (SPK) with signature
- One-time Prekeys (OPKs) for X3DH
"""
from __future__ import annotations

import base64
import json
import os
from dataclasses import dataclass
from typing import TYPE_CHECKING

from src.crypto.asymmetric import (
    generate_ed25519_keypair,
    generate_x25519_keypair,
    ed25519_sign,
    serialize_public_key,
    serialize_private_key,
    deserialize_ed25519_private,
    deserialize_x25519_private,
)

if TYPE_CHECKING:
    from cryptography.hazmat.primitives.asymmetric.ed25519 import (
        Ed25519PrivateKey,
        Ed25519PublicKey,
    )
    from cryptography.hazmat.primitives.asymmetric.x25519 import (
        X25519PrivateKey,
        X25519PublicKey,
    )


@dataclass
class LocalKeyBundle:
    """Local key bundle with both private and public keys."""
    
    # Identity keys
    ik_ed_priv: Ed25519PrivateKey
    ik_ed_pub: Ed25519PublicKey
    ik_dh_priv: X25519PrivateKey
    ik_dh_pub: X25519PublicKey
    
    # Signed prekey
    spk_priv: X25519PrivateKey
    spk_pub: X25519PublicKey
    spk_sig: bytes
    
    # One-time prekeys: list of (id, private_key, public_key)
    opks: list[tuple[int, X25519PrivateKey, X25519PublicKey]]


class KeyManager:
    """Manages cryptographic keys for a client identity.
    
    Generates and persists:
    - Ed25519 identity key (for signatures)
    - X25519 identity key (for X3DH DH1/DH2)
    - Signed prekey with Ed25519 signature
    - Pool of one-time prekeys for forward secrecy
    """
    
    def __init__(self, username: str, data_dir: str = "client_data"):
        """Initialize KeyManager for a user.
        
        Args:
            username: The user's identity
            data_dir: Directory to store key material
        """
        self.username = username
        self.data_dir = os.path.join(data_dir, username)
        self._keys: LocalKeyBundle | None = None
        
    @property
    def keys(self) -> LocalKeyBundle:
        """Get the current key bundle, loading or generating if needed."""
        if self._keys is None:
            if self._keys_exist():
                self._load_keys()
            else:
                self._generate_keys()
        return self._keys
    
    def _keys_exist(self) -> bool:
        """Check if keys have been persisted."""
        return os.path.exists(os.path.join(self.data_dir, "identity.json"))
    
    def _generate_keys(self, num_opks: int = 10) -> None:
        """Generate a fresh set of keys.
        
        Args:
            num_opks: Number of one-time prekeys to generate
        """
        # Generate Ed25519 identity key (for signing SPK)
        ik_ed_priv, ik_ed_pub = generate_ed25519_keypair()
        
        # Generate X25519 identity key (for X3DH DH1/DH2)
        ik_dh_priv, ik_dh_pub = generate_x25519_keypair()
        
        # Generate signed prekey
        spk_priv, spk_pub = generate_x25519_keypair()
        spk_pub_bytes = serialize_public_key(spk_pub)
        spk_sig = ed25519_sign(ik_ed_priv, spk_pub_bytes)
        
        # Generate one-time prekeys
        opks = []
        for i in range(num_opks):
            opk_priv, opk_pub = generate_x25519_keypair()
            opks.append((i, opk_priv, opk_pub))
        
        self._keys = LocalKeyBundle(
            ik_ed_priv=ik_ed_priv,
            ik_ed_pub=ik_ed_pub,
            ik_dh_priv=ik_dh_priv,
            ik_dh_pub=ik_dh_pub,
            spk_priv=spk_priv,
            spk_pub=spk_pub,
            spk_sig=spk_sig,
            opks=opks,
        )
        
        self._save_keys()
    
    def _save_keys(self) -> None:
        """Persist keys to disk."""
        os.makedirs(self.data_dir, exist_ok=True)
        
        keys = self._keys
        data = {
            "ik_ed_priv": base64.b64encode(serialize_private_key(keys.ik_ed_priv)).decode(),
            "ik_dh_priv": base64.b64encode(serialize_private_key(keys.ik_dh_priv)).decode(),
            "spk_priv": base64.b64encode(serialize_private_key(keys.spk_priv)).decode(),
            "spk_sig": base64.b64encode(keys.spk_sig).decode(),
            "opks": [
                {
                    "id": opk_id,
                    "priv": base64.b64encode(serialize_private_key(priv)).decode(),
                }
                for opk_id, priv, _ in keys.opks
            ],
        }
        
        key_file = os.path.join(self.data_dir, "identity.json")
        with open(key_file, "w") as f:
            json.dump(data, f, indent=2)
    
    def _load_keys(self) -> None:
        """Load keys from disk.
        
        Raises:
            ValueError: If keys file is corrupted or invalid
            FileNotFoundError: If keys file doesn't exist
        """
        key_file = os.path.join(self.data_dir, "identity.json")
        
        try:
            with open(key_file, "r") as f:
                data = json.load(f)
        except json.JSONDecodeError as e:
            raise ValueError(f"Corrupted keys file (invalid JSON): {e}")
        
        try:
            # Restore Ed25519 identity key
            ik_ed_priv = deserialize_ed25519_private(base64.b64decode(data["ik_ed_priv"]))
            ik_ed_pub = ik_ed_priv.public_key()
            
            # Restore X25519 identity key
            ik_dh_priv = deserialize_x25519_private(base64.b64decode(data["ik_dh_priv"]))
            ik_dh_pub = ik_dh_priv.public_key()
            
            # Restore signed prekey
            spk_priv = deserialize_x25519_private(base64.b64decode(data["spk_priv"]))
            spk_pub = spk_priv.public_key()
            spk_sig = base64.b64decode(data["spk_sig"])
            
            # Restore one-time prekeys
            opks = []
            for opk_data in data.get("opks", []):
                opk_id = opk_data["id"]
                opk_priv = deserialize_x25519_private(base64.b64decode(opk_data["priv"]))
                opk_pub = opk_priv.public_key()
                opks.append((opk_id, opk_priv, opk_pub))
        except KeyError as e:
            raise ValueError(f"Corrupted keys file (missing field): {e}")
        except Exception as e:
            raise ValueError(f"Failed to deserialize keys: {e}")
        
        self._keys = LocalKeyBundle(
            ik_ed_priv=ik_ed_priv,
            ik_ed_pub=ik_ed_pub,
            ik_dh_priv=ik_dh_priv,
            ik_dh_pub=ik_dh_pub,
            spk_priv=spk_priv,
            spk_pub=spk_pub,
            spk_sig=spk_sig,
            opks=opks,
        )
    
    def get_registration_bundle(self) -> dict:
        """Get the public key bundle for server registration.
        
        Returns:
            Dictionary with base64-encoded public keys and signature
        """
        keys = self.keys
        return {
            "ik_ed_pub": base64.b64encode(serialize_public_key(keys.ik_ed_pub)).decode(),
            "ik_dh_pub": base64.b64encode(serialize_public_key(keys.ik_dh_pub)).decode(),
            "spk_pub": base64.b64encode(serialize_public_key(keys.spk_pub)).decode(),
            "spk_sig": base64.b64encode(keys.spk_sig).decode(),
        }
    
    def get_onetime_prekeys_for_upload(self) -> list[tuple[int, str]]:
        """Get one-time prekeys for upload to server.
        
        Returns:
            List of (id, base64_public_key) tuples
        """
        keys = self.keys
        return [
            (opk_id, base64.b64encode(serialize_public_key(pub)).decode())
            for opk_id, _, pub in keys.opks
        ]
    
    def get_opk_private(self, opk_id: int) -> X25519PrivateKey | None:
        """Get the private key for a one-time prekey by ID.
        
        Args:
            opk_id: The ID of the one-time prekey
            
        Returns:
            The private key, or None if not found
        """
        for stored_id, priv, _ in self.keys.opks:
            if stored_id == opk_id:
                return priv
        return None
    
    def mark_opk_used(self, opk_id: int) -> None:
        """Mark a one-time prekey as used (remove it from the local store).
        
        Args:
            opk_id: The ID of the used one-time prekey
        """
        # Use self.keys property to ensure keys are loaded
        keys = self.keys
        keys.opks = [
            (stored_id, priv, pub)
            for stored_id, priv, pub in keys.opks
            if stored_id != opk_id
        ]
        self._save_keys()
    
    def generate_new_opks(self, count: int = 10, start_id: int | None = None) -> list[tuple[int, str]]:
        """Generate additional one-time prekeys.
        
        Args:
            count: Number of new OPKs to generate
            start_id: Starting ID (defaults to max existing + 1)
            
        Returns:
            List of (id, base64_public_key) tuples for upload
        """
        if start_id is None:
            existing_ids = [opk_id for opk_id, _, _ in self.keys.opks]
            start_id = max(existing_ids) + 1 if existing_ids else 0
        
        new_opks = []
        upload_keys = []
        
        for i in range(count):
            opk_id = start_id + i
            opk_priv, opk_pub = generate_x25519_keypair()
            new_opks.append((opk_id, opk_priv, opk_pub))
            upload_keys.append(
                (opk_id, base64.b64encode(serialize_public_key(opk_pub)).decode())
            )
        
        # Use self.keys property to ensure keys are loaded
        self.keys.opks.extend(new_opks)
        self._save_keys()
        
        return upload_keys