import sys

def main(inp):
    f = open(inp[1], "r")
    content = f.readlines()
    lines = len(content)
    words = sum(len(line.split()) for line in content)
    chars = sum(len(line) for line in content)

    print(f"Lines: {lines}; Words: {words}; Characters: {chars}")

    
if __name__ == "__main__":
    main(sys.argv)
