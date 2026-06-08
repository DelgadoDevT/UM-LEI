TARGET = systracer
ARCH = $(shell uname -m | sed 's/x86_64/x86/' | sed 's/aarch64/arm64/')

BPF_OBJ = ${TARGET:=.bpf.o}
USER_C = ${TARGET:=.c}
USER_SKEL = ${TARGET:=.skel.h}

all: $(TARGET) $(BPF_OBJ)
.PHONY: all

$(TARGET): $(USER_C) $(USER_SKEL)
	gcc -Wall -o $(TARGET) $(USER_C) -L../libbpf/src -l:libbpf.a -lelf -lz

%.bpf.o: %.bpf.c vmlinux.h syscalls_table.h
	clang \
	    -target bpf \
        -D __TARGET_ARCH_$(ARCH) \
	    -Wall \
	    -O2 -g -o $@ -c $<
	llvm-strip -g $@

$(USER_SKEL): $(BPF_OBJ)
	bpftool gen skeleton $< > $@


syscalls_table.h:
	ausyscall --dump | \
	awk '$$1 ~ /^[0-9]+$$/ { printf("[%d] = \"%s\",\n", $$1, $$2) }' > syscalls_table.h

vmlinux.h:
	bpftool btf dump file /sys/kernel/btf/vmlinux format c > vmlinux.h

clean:
	- rm -f $(BPF_OBJ)
	- rm -f $(TARGET)
	- rm -f syscalls_table.h vmlinux.h ${TARGET}.skel.h