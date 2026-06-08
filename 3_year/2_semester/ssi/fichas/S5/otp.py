import sys
import os

def generateKey(ksize, filepath):
    key = os.urandom(int(ksize / 8))
    with open(filepath, 'wb') as file:
        file.write(key)

def encryptOTP(plaintextPath, keyPath):
    with open(keyPath, 'rb') as keyFile:
        key = keyFile.read()

    with open(plaintextPath, 'r') as plaintextFile:
        plaintext = plaintextFile.read()

    ciphertext_bytes = bytes([a ^ b for a, b in zip(key, plaintext.encode())])

    with open(plaintextPath + '.enc', 'wb') as output:
        output.write(ciphertext_bytes)

def decryptOTP(ciphertextPath, keyPath):
    with open(keyPath, 'rb') as keyFile:
        key = keyFile.read()
    
    with open(ciphertextPath, 'rb') as ciphertextFile:
        ciphertext = ciphertextFile.read()

    plaintext = bytes([a ^ b for a, b in zip(key, ciphertext)])
    
    with open(ciphertextPath + '.dec', 'w') as output:
        output.write(plaintext.decode())

def main():
    if sys.argv[1] == 'setup':
        generateKey(int(sys.argv[2]), sys.argv[3])
    elif sys.argv[1] == 'enc':
        encryptOTP(sys.argv[2], sys.argv[3]) 
    elif sys.argv[1] == 'dec':
        decryptOTP(sys.argv[2], sys.argv[3]) 
    else:
        print("Wrong usage")

    return 0

if __name__ == '__main__':
    main()