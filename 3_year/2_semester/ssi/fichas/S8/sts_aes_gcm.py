import os
from multiprocessing import Process, Pipe
from cryptography import x509
from cryptography.hazmat.primitives import hashes, serialization
from cryptography.hazmat.primitives.asymmetric import dh, padding
from cryptography.hazmat.primitives.kdf.hkdf import HKDF
from cryptography.hazmat.primitives.ciphers.aead import AESGCM
from cryptography.hazmat.primitives.serialization import Encoding, PublicFormat, load_pem_public_key

def mkpair(x, y):
    len_x = len(x)
    len_x_bytes = len_x.to_bytes(2, "little")
    return len_x_bytes + x + y

def unpair(xy):
    len_x = int.from_bytes(xy[:2], "little")
    x = xy[2 : len_x + 2]
    y = xy[len_x + 2 :]
    return x, y

def load_identity(name):
    with open(f"{name}.crt", "rb") as f:
        cert = x509.load_pem_x509_certificate(f.read())
    with open(f"{name}.key", "rb") as f:
        key = serialization.load_pem_private_key(f.read(), password=None)
    return cert, key

def alice_process(conn, parameters, ca_cert):
    alice_cert, alice_priv_key = load_identity("Alice")
    
    alice_dh_priv = parameters.generate_private_key()
    alice_pub_bytes = alice_dh_priv.public_key().public_bytes(Encoding.PEM, PublicFormat.SubjectPublicKeyInfo)
    
    conn.send(mkpair(alice_pub_bytes, alice_cert.public_bytes(serialization.Encoding.PEM)))
    
    bob_data = conn.recv()
    bob_pub_bytes, bob_cert_bytes = unpair(bob_data)
    bob_pub_key = load_pem_public_key(bob_pub_bytes)
    bob_cert = x509.load_pem_x509_certificate(bob_cert_bytes)

    ca_cert.public_key().verify(
        bob_cert.signature, bob_cert.tbs_certificate_bytes,
        padding.PKCS1v15(), bob_cert.signature_hash_algorithm
    )

    shared_secret = alice_dh_priv.exchange(bob_pub_key)
    aes_key = HKDF(hashes.SHA256(), 32, None, b"sts-session").derive(shared_secret)
    aesgcm = AESGCM(aes_key)

    bob_sig_data = conn.recv()
    n_b, ct_b = unpair(bob_sig_data)
    bob_sig = aesgcm.decrypt(n_b, ct_b, None)
    
    bob_cert.public_key().verify(
        bob_sig, mkpair(bob_pub_bytes, alice_pub_bytes),
        padding.PSS(mgf=padding.MGF1(hashes.SHA256()), salt_length=padding.PSS.MAX_LENGTH),
        hashes.SHA256()
    )

    alice_sig = alice_priv_key.sign(
        mkpair(alice_pub_bytes, bob_pub_bytes),
        padding.PSS(mgf=padding.MGF1(hashes.SHA256()), salt_length=padding.PSS.MAX_LENGTH),
        hashes.SHA256()
    )
    n_a = os.urandom(12)
    conn.send(mkpair(n_a, aesgcm.encrypt(n_a, alice_sig, None)))

    n_m = os.urandom(12)
    conn.send(mkpair(n_m, aesgcm.encrypt(n_m, b"Hello, World!", None)))

def bob_process(conn, parameters, ca_cert):
    bob_cert, bob_priv_key = load_identity("Bob")
    
    alice_data = conn.recv()
    alice_pub_bytes, alice_cert_bytes = unpair(alice_data)
    alice_pub_key = load_pem_public_key(alice_pub_bytes)
    alice_cert = x509.load_pem_x509_certificate(alice_cert_bytes)

    ca_cert.public_key().verify(
        alice_cert.signature, alice_cert.tbs_certificate_bytes,
        padding.PKCS1v15(), alice_cert.signature_hash_algorithm
    )

    bob_dh_priv = parameters.generate_private_key()
    bob_pub_bytes = bob_dh_priv.public_key().public_bytes(Encoding.PEM, PublicFormat.SubjectPublicKeyInfo)
    
    conn.send(mkpair(bob_pub_bytes, bob_cert.public_bytes(serialization.Encoding.PEM)))

    shared_secret = bob_dh_priv.exchange(alice_pub_key)
    aes_key = HKDF(hashes.SHA256(), 32, None, b"sts-session").derive(shared_secret)
    aesgcm = AESGCM(aes_key)

    bob_sig = bob_priv_key.sign(
        mkpair(bob_pub_bytes, alice_pub_bytes),
        padding.PSS(mgf=padding.MGF1(hashes.SHA256()), salt_length=padding.PSS.MAX_LENGTH),
        hashes.SHA256()
    )
    n_b = os.urandom(12)
    conn.send(mkpair(n_b, aesgcm.encrypt(n_b, bob_sig, None)))

    alice_sig_data = conn.recv()
    n_a, ct_a = unpair(alice_sig_data)
    alice_sig = aesgcm.decrypt(n_a, ct_a, None)

    alice_cert.public_key().verify(
        alice_sig, mkpair(alice_pub_bytes, bob_pub_bytes),
        padding.PSS(mgf=padding.MGF1(hashes.SHA256()), salt_length=padding.PSS.MAX_LENGTH),
        hashes.SHA256()
    )

    n_m, ct_m = unpair(conn.recv())
    print(aesgcm.decrypt(n_m, ct_m, None).decode())

if __name__ == '__main__':
    with open("CA.crt", "rb") as f:
        ca_cert = x509.load_pem_x509_certificate(f.read())
    parameters = dh.generate_parameters(generator=2, key_size=2048)
    parent_conn, child_conn = Pipe()
    p1 = Process(target=alice_process, args=(parent_conn, parameters, ca_cert))
    p2 = Process(target=bob_process, args=(child_conn, parameters, ca_cert))
    p1.start(); p2.start()
    p1.join(); p2.join()