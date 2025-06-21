CC = gcc
# CFLAGS = -Wall -O3 -g -Iinclude -std=c11 -D_POSIX_C_SOURCE=200809L -Werror=discarded-qualifiers $(shell pkg-config --cflags glib-2.0) #debug
CFLAGS = -Wall -O3 -Iinclude -std=c11 -D_POSIX_C_SOURCE=200809L -Werror=discarded-qualifiers $(shell pkg-config --cflags glib-2.0) #normal

LDLIBS = $(shell pkg-config --libs glib-2.0) -pthread -lm

# Define the target for the entire project
all: folders dserver dclient

# Targets for the executables
dserver: bin/dserver
dclient: bin/dclient

# Create necessary folders
folders:
	@mkdir -p src include obj bin tmp doc

# Build the server executable
bin/dserver: obj/dserver.o \
             obj/operation.o \
             obj/operation_handler.o \
             obj/metadata.o \
             obj/utils.o \
             obj/freeQueue.o \
             obj/cache.o
	$(CC) $^ -o $@ $(LDLIBS)

# Build the client executable
bin/dclient: obj/dclient.o \
             obj/operation.o \
             obj/utils.o
	$(CC) $^ -o $@ $(LDLIBS)

# Rule to build object files
obj/%.o: src/%.c
	$(CC) $(CFLAGS) -c $< -o $@

# Clean target
clean:
	rm -f obj/* tmp/*

# Doxygen documentation generation
.PHONY: doc
doc:
	doxygen Doxyfile

# clean documentation
docclean:
	rm -rf doc/*