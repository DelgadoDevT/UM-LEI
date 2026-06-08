"""
Contact Manager for E2EE chat client.

Handles:
- Contact storage and retrieval
- Certificate verification
- Bundle caching
"""
from __future__ import annotations

import base64
from typing import TYPE_CHECKING

from src.crypto.certificates import pem_to_cert, verify_certificate, get_cert_username

if TYPE_CHECKING:
    from src.storage.client_db import ClientDB


class ContactManager:
    """Manages contacts and their public key material.
    
    Stores contact public keys and certificates locally,
    verifying certificates against the CA root.
    """
    
    def __init__(self, db: ClientDB, ca_cert_pem: str | None = None):
        """Initialize contact manager.
        
        Args:
            db: Client database for persistence
            ca_cert_pem: PEM-encoded CA root certificate for verification
        """
        self.db = db
        self.ca_cert = None
        if ca_cert_pem:
            self.set_ca_cert(ca_cert_pem)
    
    def set_ca_cert(self, ca_cert_pem: str) -> None:
        """Set the CA certificate for verification.
        
        Args:
            ca_cert_pem: PEM-encoded CA certificate
        """
        self.ca_cert = pem_to_cert(ca_cert_pem.encode())
    
    def add_contact(
        self,
        username: str,
        bundle: dict,
        verify: bool = True,
    ) -> bool:
        """Add a contact with their key bundle.
        
        Args:
            username: Contact's username
            bundle: Key bundle from server (with cert_pem)
            verify: Whether to verify certificate (default True)
            
        Returns:
            True if contact added successfully
            
        Raises:
            ValueError: If certificate verification fails
        """
        # Extract keys from bundle
        ik_ed_pub = base64.b64decode(bundle["ik_ed_pub"])
        ik_dh_pub = base64.b64decode(bundle["ik_dh_pub"])
        spk_pub = base64.b64decode(bundle["spk_pub"])
        spk_sig_b64 = bundle.get("spk_sig")
        spk_sig = base64.b64decode(spk_sig_b64) if spk_sig_b64 else None
        cert_pem = bundle.get("cert_pem")
        
        # Verify certificate if requested
        if verify and cert_pem and self.ca_cert:
            cert = pem_to_cert(cert_pem.encode())
            
            if not verify_certificate(cert, self.ca_cert):
                raise ValueError(f"Certificate verification failed for {username}")
            
            # Check that certificate CN matches username
            cert_cn = get_cert_username(cert)
            if cert_cn != username:
                raise ValueError(
                    f"Certificate CN '{cert_cn}' doesn't match username '{username}'"
                )
        
        # Save contact
        self.db.save_contact(
            username=username,
            ik_ed_pub=ik_ed_pub,
            ik_dh_pub=ik_dh_pub,
            spk_pub=spk_pub,
            spk_sig=spk_sig,
            p2p_host=bundle.get("p2p_host", "") or "",
            p2p_port=int(bundle.get("p2p_port", 0) or 0),
            cert_pem=cert_pem,
        )
        
        return True
    
    def get_contact(self, username: str) -> dict | None:
        """Get a contact's information.
        
        Args:
            username: Contact's username
            
        Returns:
            Contact dictionary with base64-encoded keys, or None
        """
        contact = self.db.get_contact(username)
        if not contact:
            return None
        
        return {
            "username": contact["username"],
            "ik_ed_pub": base64.b64encode(contact["ik_ed_pub"]).decode(),
            "ik_dh_pub": base64.b64encode(contact["ik_dh_pub"]).decode(),
            "spk_pub": base64.b64encode(contact["spk_pub"]).decode(),
            "spk_sig": (
                base64.b64encode(contact["spk_sig"]).decode()
                if contact.get("spk_sig")
                else ""
            ),
            "p2p_host": contact.get("p2p_host", "") or "",
            "p2p_port": int(contact.get("p2p_port", 0) or 0),
            "cert_pem": contact["cert_pem"],
        }
    
    def has_contact(self, username: str) -> bool:
        """Check if a contact exists.
        
        Args:
            username: Contact's username
            
        Returns:
            True if contact exists
        """
        return self.db.get_contact(username) is not None
    
    def list_contacts(self) -> list[str]:
        """List all contact usernames.
        
        Returns:
            List of usernames
        """
        contacts = self.db.list_contacts()
        return [c["username"] for c in contacts]
    
    def remove_contact(self, username: str) -> bool:
        """Remove a contact.
        
        Args:
            username: Contact's username
            
        Returns:
            True if contact was removed, False if not found
        """
        return self.db.delete_contact(username)
    
    def update_contact_spk(
        self,
        username: str,
        new_spk_pub: bytes,
    ) -> bool:
        """Update a contact's signed prekey.
        
        Args:
            username: Contact's username
            new_spk_pub: New SPK public key bytes
            
        Returns:
            True if contact was updated, False if not found
        """
        return self.db.update_contact_spk(username, new_spk_pub)
