ARCH = $(shell uname -m | sed 's/x86_64/x86/' | sed 's/aarch64/arm64/')

CC = gcc
CLANG = clang
BPFTOOL = bpftool

FS_OBJ = src/dedup_fs/dedup_fs.o src/dedup_fs/persistence.o src/dedup_fs/hashing.o src/dedup_fs/chunk_operations.o
EBPF_OBJ = src/phys_io_counter/phys_io_counter.bpf.o
EBPF_SKEL = src/phys_io_counter/phys_io_counter.skel.h
VMLINUX = include/phys_io_counter/vmlinux.h

.PHONY: all clean doc help fs ebpf docopen docclean

all: dedup_fs phys_io_counter

fs: dedup_fs

ebpf: phys_io_counter

dedup_fs: $(FS_OBJ)
	$(CC) -Wall -g $^ -o $@ `pkg-config fuse3 glib-2.0 --libs` -lcrypto -lpthread

%.o: %.c
	$(CC) -Wall -g `pkg-config fuse3 glib-2.0 --cflags` -Iinclude -c $< -o $@

phys_io_counter: src/phys_io_counter/phys_io_counter.c $(EBPF_SKEL)
	$(CC) -Wall -Iinclude/phys_io_counter -Isrc/phys_io_counter -Iinclude $< -o $@ `pkg-config libbpf --libs` -lelf -lz

$(EBPF_OBJ): src/phys_io_counter/phys_io_counter.bpf.c $(VMLINUX)
	$(CLANG) -target bpf -D __TARGET_ARCH_$(ARCH) -Iinclude/phys_io_counter -Wall -O2 -g -c $< -o $@
	llvm-strip -g $@

$(EBPF_SKEL): $(EBPF_OBJ)
	$(BPFTOOL) gen skeleton $< > $@

$(VMLINUX):
	mkdir -p $(dir $@)
	$(BPFTOOL) btf dump file /sys/kernel/btf/vmlinux format c > $@

clean:
	- rm -f dedup_fs phys_io_counter
	- rm -f src/dedup_fs/*.o src/phys_io_counter/*.o
	- rm -f scripts/passthrough/*.o scripts/passthrough/passthrough
	- rm -f src/phys_io_counter/*.skel.h 
	- rm -f include/phys_io_counter/vmlinux.h
	- rm -f scripts/results_w*.csv

docclean:
	- rm -rf docs

doc:
	mkdir -p docs
	doxygen Doxyfile || echo "Please run 'doxygen -g Doxyfile' first."

docopen: doc
	@echo "Opening Documentation..."
	@xdg-open docs/html/index.html || open docs/html/index.html || start docs/html/index.html

help:
	@echo "Targets:"
	@echo "  make / make all   - Build everything"
	@echo "  make fs           - Build filesystem only"
	@echo "  make ebpf         - Build eBPF tool only"
	@echo "  make clean        - Remove build artifacts"
	@echo "  make doc          - Generate doxygen docs"
	@echo "  make docopen      - Open doxygen docs"
	@echo "  make docclean     - Remove doxygen docs"
