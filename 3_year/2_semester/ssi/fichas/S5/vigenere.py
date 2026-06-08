import sys

def preproc(str):
    l = []
    for c in str:
        if c.isalpha():
            l.append(c.upper())
    return "".join(l)

def processVigenere(mode, tkey, text):
    ptext = list(text)
    for i in range(0, len(text)):
        if mode == 0:
            ptext[i] = (chr(((ord(ptext[i]) - 65 + (ord(tkey[i]) - 65)) % 26) + 65))
        else:
            ptext[i] = (chr(((ord(ptext[i]) - 65 - (ord(tkey[i]) - 65)) % 26) + 65))

    return "".join(ptext)

def processKey(preKey, text):
    key = preKey
    while len(key) < len(text):
        key = key + preKey    
    return key

def main():
    if len(sys.argv) != 4:
        print("Wrong usage")
        return -1
    
    text = preproc(sys.argv[3])
    ptext = ''
    key = processKey(sys.argv[2], text)
    if sys.argv[1] == 'enc':
        ptext = processVigenere(0, key, text) 
    elif sys.argv[1] == 'dec':
        ptext = processVigenere(1, key, text)
    else:
        print("Wrong usage")

    print(ptext)
    return 0

if __name__ == "__main__": 
    main()
