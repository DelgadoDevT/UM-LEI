#include <signal.h>
#include <stdio.h>
#include <unistd.h>
#include <bpf/libbpf.h>
// #include <errno.h>
#include "systracer.h"
#include "systracer.skel.h"

#define POLL_TIMEOUT_MS 100
#define MAX_SYSCALLS 512

static const char *syscall_table[MAX_SYSCALLS] = {
#include "syscalls_table.h"
};

const char *resolve_syscall(int nr)
{
	switch(nr) {
        case 1001: return "vfs_read";
        case 1002: return "vfs_write";
        case 1003: return "vfs_open";
        case 1004: return "ext4_read";
        case 1005: return "ext4_write";
		case 1006: return "block_read";
		case 1007: return "block_write";
    }

    if (nr >= 0 && nr < MAX_SYSCALLS && syscall_table[nr])
        return syscall_table[nr];
    return "unknown";
}

static volatile bool exiting = false;

static void sig_handler(int sig){
	exiting = true;
}

int event_handler(void *ctx, void *data, size_t data_sz)
{
	struct data_t *m = data;

	printf("%-6d %-16s %-6ld %-16s\n", m->pid, m->command, m->syscall_id, resolve_syscall(m->syscall_id));

	return 0;
}


int main(int argc, char *argv[])
{
    struct systracer_bpf *skel;
    int err;
	struct ring_buffer *events = NULL;

	if (argc < 2) {
		printf("Usage: %s <target_pid>\n", argv[0]);
		return 1;
	}

	__u32 target_pid = atoi(argv[1]);

	/* Cleaner handling of Ctrl-C */
	signal(SIGINT, sig_handler);

	/* Load & verify BPF programs */
	skel = systracer_bpf__open_and_load();
	if (!skel) {
		printf("Failed to open BPF object\n");
		goto cleanup;
	}

	__u8 exists = 1;
	err = bpf_map__update_elem(skel->maps.pid_map, &target_pid, sizeof(target_pid), &exists, sizeof(exists), BPF_ANY);
	if (err) {
		fprintf(stderr, "Failed to update target PID map: %d\n", err);
		systracer_bpf__destroy(skel);
        goto cleanup;
	}

	/* Attach hooks */
	err = systracer_bpf__attach(skel);
	if (err) {
		fprintf(stderr, "Failed to attach BPF skeleton: %d\n", err);
		systracer_bpf__destroy(skel);
        goto cleanup;
	}

	int key = 0;
	unsigned long long lost_counter = 0;
	bpf_map__update_elem(skel->maps.lost_events, &key, sizeof(key), &lost_counter, sizeof(lost_counter), BPF_ANY);
	/* Set up ring buffer polling */
	events = ring_buffer__new(bpf_map__fd(skel->maps.events), event_handler, NULL, NULL);
	if (!events) {
		err = -1;
		printf("Failed to create events ring buffer\n");
		goto cleanup;
	}


	/* Process events */
	printf("%-6s %-16s %-6s %-16s\n", "pid", "command", "id", "syscall");
	while (!exiting) {
		err = ring_buffer__poll(events, POLL_TIMEOUT_MS);
    }

	printf("Lost %lld events\n", lost_counter);

	cleanup:
	systracer_bpf__destroy(skel);

	return err < 0 ? -err : 0;
}