import sys

def attack(fctxt, pos, ptxtAtPos, newPtxtAtPos):
    ptxt_bytes = ptxtAtPos.encode('utf-8')
    new_ptxt_bytes = newPtxtAtPos.encode('utf-8')

    with open(fctxt, 'rb') as f:
        data = bytearray(f.read())

    offset = 16 + pos

    for i in range(len(ptxt_bytes)):
        if offset + i < len(data):
            data[offset + i] = data[offset + i] ^ ptxt_bytes[i] ^ new_ptxt_bytes[i]
            
    with open(fctxt + '.attck', 'wb') as f:
        f.write(data)

if __name__ in '__main__':
    attack(sys.argv[1], int(sys.argv[2]), sys.argv[3], sys.argv[4])