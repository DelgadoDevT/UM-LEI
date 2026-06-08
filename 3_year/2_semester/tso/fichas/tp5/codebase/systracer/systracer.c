#include <signal.h>
#include <stdio.h>
#include <unistd.h>
#include <bpf/libbpf.h>
#include "systracer.h"
#include "systracer.skel.h"
#include <sys/stat.h>

#define POLL_TIMEOUT_MS 100

#define MAX_SYSCALLS 512

static volatile bool exiting = false;
static void sig_handler(int sig){
	exiting = true;
}

/*
  To generate syscalls_table.h run:

  ausyscall --dump | awk '$1 ~ /^[0-9]+$/ { printf("[%d] = \"%s\",\n", $1, $2) }' > syscalls_table.h
*/

static const char *syscall_table[MAX_SYSCALLS] = {
#include "syscalls_table.h"
};

/* Function to resolve syscall number to name
 * Receives a syscall number (int nr) as argument
 * Returns the corresponding syscall name or "unknown" if not found
 */
const char *resolve_syscall(int nr)
{
    if (nr >= 0 && nr < MAX_SYSCALLS && syscall_table[nr])
        return syscall_table[nr];
    return "unknown";
}

/* Function to handle events received from the BPF program
 * Receives a pointer to the context (void *ctx), a pointer to the data (void *data) and the size of the data (size_t data_sz) as arguments
 * Returns 0 on success
 */
int event_handler(void *ctx, void *data, size_t data_sz)
{
	struct data_t *m = data;

	// check layer type and translate it to string
	char type[20];
	if (m->type == L_SYSCALL) strcpy(type, "syscall");
	else if (m->type == L_VFS) strcpy(type, "VFS");
	else if (m->type == L_EXT4) strcpy(type, "EXT4");
	else if (m->type == L_FUSE) strcpy(type, "FUSE");
	else if (m->type == L_BIO) strcpy(type, "BIO");

	// resolve syscall name from syscall id
	const char *syscall_name = resolve_syscall(m->syscall_id);

	// if the syscall is read or write, print the hash value and size
	if ((strcmp(syscall_name, "write") == 0 || \
		strcmp(syscall_name, "pwrite") == 0 || \
		strcmp(syscall_name, "read") == 0 || \
		strcmp(syscall_name, "pread") == 0))
	{
		if (m->hash) // print hash value only if it is not zero
			printf("%-6d %-16s %-16s %-16s %-64s %-10ld %-10u\n", m->pid, m->command, syscall_name, type, m->filename, m->size, m->hash);
		else printf("%-6d %-16s %-16s %-16s %-64s %-10ld\n", m->pid, m->command, syscall_name, type, m->filename, m->size);
	} else {// for other syscalls, we don't have hash and size information, so we print only the basic info
		printf("%-6d %-16s %-16s %-16s %-64s\n", m->pid, m->command, syscall_name, type, m->filename);
	}

	return 0;
}


int main(int argc, char *argv[])
{
    struct systracer_bpf *skel;
    int err;
	struct ring_buffer *events = NULL;


	/* Cleaner handling of Ctrl-C */
	signal(SIGINT, sig_handler);

	/* Load & verify BPF programs */
	skel = systracer_bpf__open_and_load();
	if (!skel) {
		printf("Failed to open BPF object\n");
		goto cleanup;
	}

	/* Configure a map with the pid to filter */
	if (argc < 2) {
		printf("usage: ./systracer <pid>");
	}

	// add the pids to filter to the my_config map. The key is the pid and the value is 1 (could be a char instead).
	uint32_t key;
	for (int i = 1; i < argc; i++) {
		key = atoi(argv[i]);
		uint32_t value = 1;
		bpf_map__update_elem(skel->maps.my_config, &key, sizeof(key), &value, sizeof(value), 0);
		printf("Tracing Pid %d\n", key);
	}


	/* Attach hooks */
	err = systracer_bpf__attach(skel);
	if (err) {
		fprintf(stderr, "Failed to attach BPF skeleton: %d\n", err);
		systracer_bpf__destroy(skel);
        goto cleanup;
	}

	// initialize lost events counter in the lost_events map to 0
	key = 0;
	unsigned long long lost_counter = 0;
	bpf_map__update_elem(skel->maps.lost_events, &key, sizeof(key), &lost_counter, sizeof(lost_counter), BPF_ANY);

	/* Set up ring buffer polling */
	events = ring_buffer__new(bpf_map__fd(skel->maps.events), event_handler, NULL, NULL);
	if (!events) {
		err = -1;
		printf("Failed to create events ring buffer\n");
		goto cleanup;
	}

	/* Process events until interrupted */
	printf("%-6s %-16s %-16s %-16s %-64s %-10s %-10s\n", "pid", "command", "syscall", "type", "filename", "size", "hash");
	printf("----------------------------------------------------------------------------------------------------------------------------------------------------\n");
	while (!exiting) {
		err = ring_buffer__poll(events, POLL_TIMEOUT_MS);
    }

	printf("--------------------------------------------------------------------------------------------------------------------------------------------------\n");

	// check if there were lost events by looking up the lost_events map with key = 0.
	err = bpf_map__lookup_elem(skel->maps.lost_events, &key, sizeof(key), &lost_counter, sizeof(lost_counter), BPF_ANY);
	if (err < 0)
	{
		goto cleanup;
	}
	printf("Lost %lld events\n", lost_counter);

	// ---
	cleanup:
	systracer_bpf__destroy(skel);

	return err < 0 ? -err : 0;
}