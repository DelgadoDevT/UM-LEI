from multiprocessing import Process, Pipe
from cryptography.hazmat.primitives.serialization import Encoding, PublicFormat, load_pem_public_key
from cryptography.hazmat.primitives.asymmetric import dh

def alice_process(conn, parameters):
    alice_private_key = parameters.generate_private_key()
    alice_public_key = alice_private_key.public_key()
    
    conn.send(alice_public_key.public_bytes(Encoding.PEM, PublicFormat.SubjectPublicKeyInfo))

    bob_bytes = conn.recv()
    bob_public_key = load_pem_public_key(bob_bytes)

    shared_key = alice_private_key.exchange(bob_public_key)

    print(f"Alice Key: {shared_key.hex()}")

def bob_process(conn, parameters):
    alice_bytes = conn.recv()
    alice_public_key = load_pem_public_key(alice_bytes)

    bob_private_key = parameters.generate_private_key()
    bob_public_key = bob_private_key.public_key()
    
    conn.send(bob_public_key.public_bytes(Encoding.PEM, PublicFormat.SubjectPublicKeyInfo))

    shared_key = bob_private_key.exchange(alice_public_key)

    print(f"Bob Key: {shared_key.hex()}")

if __name__ == '__main__':
    parameters = dh.generate_parameters(generator=2, key_size=2048)
    parent_conn, child_conn = Pipe()
    p1 = Process(target=alice_process, args=(parent_conn, parameters,))
    p2 = Process(target=bob_process, args=(child_conn, parameters,))
    p1.start(); p2.start()
    p1.join(); p2.join()