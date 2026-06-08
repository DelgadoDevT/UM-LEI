from cryptography.hazmat.primitives import hashes
import sys
import os

def setup(fkey):
    with open(fkey, 'wb') as output:
        key = os.urandom(32)
        output.write(key)  

def mac(fich, fkey):
    with open(fich, 'rb') as fichF:
        content = fichF.read()

    digest = hashes.Hash(hashes.SHA256())

    with open(fkey, 'rb') as fkeyF:
        digest.update(fkeyF.read())

    digest.update(content)

    with open(fich + '.mac', 'wb') as outputMac:
        outputMac.write(digest.finalize())

def ver(fich, fkey):
    with open(fich, 'rb') as fichF:
        content = fichF.read()

    digest = hashes.Hash(hashes.SHA256())    

    with open(fkey, 'rb') as fkeyF:
        digest.update(fkeyF.read())

    digest.update(content)

    with open(fich + '.mac', 'rb') as outputMac:
        print(outputMac.read() == digest.finalize())

def main():
    if sys.argv[1] == 'setup':
        setup(sys.argv[2])
    elif sys.argv[1] == 'mac':
        mac(sys.argv[2], sys.argv[3])
    elif sys.argv[1] == 'ver':
        ver(sys.argv[2], sys.argv[3])

if __name__ == '__main__':
    main()