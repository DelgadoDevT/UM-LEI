import os
from multiprocessing import Process, Pipe
from cryptography.hazmat.primitives.serialization import Encoding, PublicFormat, load_pem_public_key
from cryptography.hazmat.primitives.asymmetric import dh
from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives.kdf.hkdf import HKDF
from cryptography.hazmat.primitives.ciphers.aead import AESGCM

def alice_process(conn, parameters):
    alice_private_key = parameters.generate_private_key()
    alice_public_key = alice_private_key.public_key()
    
    conn.send(alice_public_key.public_bytes(Encoding.PEM, PublicFormat.SubjectPublicKeyInfo))
    bob_bytes = conn.recv()
    bob_public_key = load_pem_public_key(bob_bytes)

    shared_secret = alice_private_key.exchange(bob_public_key)
    aes_key = HKDF(
        algorithm=hashes.SHA256(),
        length=32,
        salt=None,
        info=b"dh-aes-gcm",
    ).derive(shared_secret)

    aesgcm = AESGCM(aes_key)
    nonce = os.urandom(12)
    message = b"Hello, World!"
    ciphertext = aesgcm.encrypt(nonce, message, None)
    
    conn.send((nonce, ciphertext))

def bob_process(conn, parameters):
    alice_bytes = conn.recv()
    alice_public_key = load_pem_public_key(alice_bytes)

    bob_private_key = parameters.generate_private_key()
    bob_public_key = bob_private_key.public_key()
    
    conn.send(bob_public_key.public_bytes(Encoding.PEM, PublicFormat.SubjectPublicKeyInfo))

    shared_secret = bob_private_key.exchange(alice_public_key)
    aes_key = HKDF(
        algorithm=hashes.SHA256(),
        length=32,
        salt=None,
        info=b"dh-aes-gcm",
    ).derive(shared_secret)

    nonce, ciphertext = conn.recv()
    aesgcm = AESGCM(aes_key)
    
    decrypted_message = aesgcm.decrypt(nonce, ciphertext, None)
    print(decrypted_message.decode())

if __name__ == '__main__':
    parameters = dh.generate_parameters(generator=2, key_size=2048)
    parent_conn, child_conn = Pipe()
    p1 = Process(target=alice_process, args=(parent_conn, parameters,))
    p2 = Process(target=bob_process, args=(child_conn, parameters,))
    p1.start(); p2.start()
    p1.join(); p2.join()