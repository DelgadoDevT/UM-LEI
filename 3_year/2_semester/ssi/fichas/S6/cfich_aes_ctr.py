import os, sys
from cryptography.hazmat.primitives.ciphers import Cipher, algorithms, modes
from cryptography.hazmat.primitives import padding

def setupKey(filename):
    key = os.urandom(32)
    with open(filename, 'wb') as file:
        file.write(key)

def encryptAES_CBC(filename, fileKeyName):
    with open(fileKeyName, 'rb') as fileKey:
        key = fileKey.read()
   
    nonce = os.urandom(16)
    cipher = Cipher(algorithms.AES(key), modes.CBC(nonce))
    encryptor = cipher.encryptor()
    
    with open(filename, 'rb') as file:
        plaintext = file.read()

    padder = padding.PKCS7(128).padder()
    padded_data = padder.update(plaintext) + padder.finalize()

    ciphertext = encryptor.update(padded_data) + encryptor.finalize()
    
    with open(filename + '.enc', 'wb') as encFile:
        encFile.write(iv)
        encFile.write(ciphertext)

def decryptAES_CBC(filename, fileKeyName):
    with open(fileKeyName, 'rb') as fileKey:
        key = fileKey.read()
    
    with open(filename, 'rb') as encFile:
        nonce = encFile.read(16)
        ciphertext = encFile.read()

    cipher = Cipher(algorithms.AES(key), modes.CBC(nonce))
    decryptor = cipher.decryptor()

    padded_plaintext = decryptor.update(ciphertext) + decryptor.finalize()

    unpadder = padding.PKCS7(128).unpadder()
    plaintext = unpadder.update(padded_plaintext) + unpadder.finalize()

    with open(filename + '.dec', 'wb') as decFile:
        decFile.write(plaintext) 

def main():
    if sys.argv[1] == 'setup':
        setupKey(sys.argv[2])
    elif sys.argv[1] == 'enc':
        encryptAES_CBC(sys.argv[2], sys.argv[3])
    elif sys.argv[1] == 'dec':
        decryptAES_CBC(sys.argv[2], sys.argv[3])

if __name__ == '__main__':
    main()