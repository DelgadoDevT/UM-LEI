import sys
import hashpumpy
import binascii

def attack_sha256(fich, ext):
    with open(fich, 'rb') as f:
        content = f.read()
    
    with open(fich + '.mac', 'rb') as f:
        raw_mac = f.read()

    with open(ext, 'rb') as f:
        data_to_add = f.read()

    hex_mac = binascii.hexlify(raw_mac).decode()

    new_hex_mac, new_message = hashpumpy.hashpump(hex_mac, content, data_to_add, 32)

    with open(fich + '.ext', 'wb') as f:
        f.write(new_message)

    with open(fich + '.ext.mac', 'wb') as f:
        f.write(binascii.unhexlify(new_hex_mac))

if __name__ == '__main__':
    attack_sha256(sys.argv[1], sys.argv[2])