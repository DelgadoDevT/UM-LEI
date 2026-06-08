CC=gcc
CFLAGS= -Wall -g
DEPS = metaindex.h
OBJ = metanode.o metaindex.o

LIBS=`pkg-config --cflags --libs glib-2.0` -lpthread

%.o: %.c $(DEPS)
	$(CC) -c -o $@ $< $(CFLAGS) $(LIBS)

metanode: $(OBJ)
	$(CC) -o $@ $^ $(CFLAGS) $(LIBS)

.PHONY: clean

clean:
	rm -f *.o metanode *.txt