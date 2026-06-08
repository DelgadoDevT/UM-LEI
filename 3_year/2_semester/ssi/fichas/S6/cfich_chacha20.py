import struct, os, sys
from cryptography.hazmat.primitives.ciphers import Cipher, algorithms

def setupKey(filename):
    key = os.urandom(32)
    with open(filename, 'wb') as file:
        file.write(key)

def encryptChaCha20(filename, fileKeyName):
    with open(fileKeyName, 'rb') as fileKey:
        key = fileKey.read()
   
    nonce = os.urandom(8)
    counter = 0
    full_nonce = struct.pack("<Q", counter) + nonce
    algorithm = algorithms.ChaCha20(key, full_nonce)
    cipher = Cipher(algorithm, mode=None)
    encryptor = cipher.encryptor()
    
    with open(filename, 'rb') as file:
        plaintext = file.read()
    
    cryptography = encryptor.update(plaintext)
    with open(filename + '.enc', 'wb') as encFile:
        encFile.write(full_nonce)
        encFile.write(cryptography)

def decryptChaCha20(filename, fileKeyName):
    with open(fileKeyName, 'rb') as fileKey:
        key = fileKey.read()
    
    with open(filename, 'rb') as encFile:
        full_nonce = encFile.read(16)
        ciphertext = encFile.read()

    algorithm = algorithms.ChaCha20(key, full_nonce)
    cipher = Cipher(algorithm, mode=None)
    decryptor = cipher.decryptor()
    plaintext = decryptor.update(ciphertext)

    with open(filename + '.dec', 'wb') as decFile:
        decFile.write(plaintext)  

def main():
    if sys.argv[1] == 'setup':
        setupKey(sys.argv[2])
    elif sys.argv[1] == 'enc':
        encryptChaCha20(sys.argv[2], sys.argv[3])
    elif sys.argv[1] == 'dec':
        decryptChaCha20(sys.argv[2], sys.argv[3])

if __name__ == '__main__':
    main()