import sys

if len(sys.argv) > 1:
    f = open(sys.argv[1], "r", encoding="utf-8")
    print(f.read(), end='')
else:
    raise Exception("Usage ./myCat file")
