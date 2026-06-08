#include <signal.h>
#include <stdio.h>
#include <unistd.h>
#include <bpf/libbpf.h>
#include "syscounter.h"
#include "syscounter.skel.h"
#include <sys/stat.h>

#define POLL_TIMEOUT_MS 100

#define MAX_SYSCALLS 512

static volatile bool exiting = false;
static void sig_handler(int sig){
	exiting = true;
}

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

/* Function to get all values from the counter map
 * Receives a pointer to the BPF skeleton (struct syscounter_bpf *skel), an array of counter_key structs to store the keys (struct counter_key keys[MAX_PIDS * MAX_OPS]) and an array of long long to store the values (long long values[MAX_PIDS * MAX_OPS]) as arguments
 * Returns the number of entries retrieved from the map
 */
int get_values_from_counter_map (struct syscounter_bpf *skel, struct counter_key keys[MAX_PIDS * MAX_OPS], long long values[MAX_PIDS * MAX_OPS]) {

	int num_entries = 0;

	struct counter_key cur_key, next_key;

	// Start with NULL to get first key
	if (bpf_map__get_next_key(skel->maps.counter, NULL, &next_key, sizeof(next_key)) != 0) {
		printf("Map is empty\n");
		return num_entries;
	}

	// Iterate through the map using get_next_key until there are no more keys
	cur_key = next_key;
	while (1) {
		long long counter_value;
		if (bpf_map__lookup_elem(skel->maps.counter, &cur_key, sizeof(cur_key),
								&counter_value, sizeof(counter_value), BPF_ANY) == 0)
		{
			// Store the key and value in the provided arrays
			keys[num_entries] = cur_key;
			values[num_entries] = counter_value;
			num_entries++;
		}

		// Get next key
		if (bpf_map__get_next_key(skel->maps.counter, &cur_key, &next_key, sizeof(next_key)) != 0)
			break;

		cur_key = next_key;
	}

	return num_entries;
}

/* Function to get unique pid values from the counter keys
 * Receives an array of counter_key structs (struct counter_key keys[MAX_PIDS * MAX_OPS]), the number of entries in the array (int num_entries) and an array of strings to store the unique pid values (char pid_list[MAX_PIDS][30]) as arguments
 * Returns the number of unique pid values found
 */
int get_unique_pid_values(struct counter_key keys[MAX_PIDS * MAX_OPS], int num_entries, char pid_list[MAX_PIDS][30]) {
	int pid_count = 0;
	int found;

	for (int i = 0; i < num_entries; i++) {
		char val[30];
		snprintf(val, sizeof(val), "%s-%d", keys[i].command, keys[i].pid);

		found = 0;
		for (int j = 0; j < pid_count; j++)
			if (strcmp(pid_list[j], val)==0) found = 1;
		if (!found) {
			strcpy(pid_list[pid_count++], val);
		}
	}

	return pid_count;
}

/* Function to get unique op values from the counter keys
 * Receives an array of counter_key structs (struct counter_key keys[MAX_PIDS * MAX_OPS]), the number of entries in the array (int num_entries) and an array of integers to store the unique op values (int op_list[MAX_OPS]) as arguments
 * Returns the number of unique op values found
*/
int get_unique_op_values(struct counter_key keys[MAX_PIDS * MAX_OPS], int num_entries, int op_list[MAX_OPS]) {
	int op_count = 0;
	int found;

	for (int i = 0; i < num_entries; i++) {
		found = 0;
		for (int j = 0; j < op_count; j++)
			if (op_list[j] == keys[i].op) found = 1;
		if (!found) op_list[op_count++] = keys[i].op;
	}

	return op_count;
}

/* Function to print the results in a table format
 * Receives an array of counter_key structs (struct counter_key keys[MAX_PIDS * MAX_OPS]), an array of long long values (long long values[MAX_PIDS * MAX_OPS]), the number of entries in the arrays (int num_entries), an array of strings with the unique pid values (char pid_list[MAX_PIDS][30]), the number of unique pid values (int pid_count), an array of integers with the unique op values (int op_list[MAX_OPS]) and the number of unique op values (int op_count) as arguments
 * Prints the results in a table format with the pids as rows and the ops as columns, and the total for each op at the end
 */
void print_results(struct counter_key keys[MAX_PIDS * MAX_OPS], long long values[MAX_PIDS * MAX_OPS], int num_entries, char pid_list[MAX_PIDS][30], int pid_count, int op_list[MAX_OPS], int op_count) {
	long long total_op_list[MAX_OPS] = {0};

	printf("%-30s", "PID\\OP");
	for (int i = 0; i < op_count; i++)
		printf("%-16s", resolve_syscall(op_list[i]));
	printf("\n");

	int total_width = 30 + op_count * 16;
	for (int i = 0; i < total_width; i++)
		putchar('-');
	printf("\n");

	char comm[30];
	for (int j = 0; j < pid_count; j++) {
		printf("%-30s", pid_list[j]);
		for (int i = 0; i < op_count; i++) {
			long long val = 0;
			for (int k = 0; k < num_entries; k++) {
				sprintf(comm, "%s-%d", keys[k].command, keys[k].pid);
				if ((strcmp(comm,pid_list[j])==0) && keys[k].op == op_list[i]) {
					val = values[k];
					total_op_list[i] += values[k];
				}
			}
			printf("%-16lld", val);
		}
		printf("\n");
	}

	for (int i = 0; i < total_width; i++)
		putchar('-');
	printf("\n");

	printf("%-30s", "Total");
	for (int i = 0; i < op_count; i++) {
		printf("%-16lld", total_op_list[i]);
	}
	printf("\n");
}


int main(int argc, char *argv[])
{
    struct syscounter_bpf *skel;
    int err;


	/* Cleaner handling of Ctrl-C */
	signal(SIGINT, sig_handler);

	/* Load & verify BPF programs */
	skel = syscounter_bpf__open_and_load();
	if (!skel) {
		printf("Failed to open BPF object\n");
		goto cleanup;
	}

	/* Configure a map with the pid to filter */
	if (argc < 2) {
		printf("usage: ./syscounter <pid>");
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
	err = syscounter_bpf__attach(skel);
	if (err) {
		fprintf(stderr, "Failed to attach BPF skeleton: %d\n", err);
		syscounter_bpf__destroy(skel);
        goto cleanup;
	}

	/* Process events until interrupted */
	printf("Successfully started! Press Ctrl-C to stop and print results.\n");
	// Use a very large sleep interval and rely on the signal handler to set exiting to true when Ctrl-C is pressed.
	while (!exiting) {
        sleep(99999999);
	}
	printf("\n");

	/* Print values stored in counter map */
	struct counter_key keys[MAX_PIDS * MAX_OPS];
	long long values[MAX_PIDS * MAX_OPS];
	char pid_list[MAX_PIDS][30];
	int op_list[MAX_OPS];

	int num_entries = get_values_from_counter_map (skel, keys, values);
	if (num_entries) {
		int pid_count = get_unique_pid_values(keys, num_entries, pid_list);
		int op_count = get_unique_op_values(keys, num_entries, op_list);
		print_results(keys, values, num_entries, pid_list, pid_count, op_list, op_count);
	}

	cleanup:
	syscounter_bpf__destroy(skel);

	return err < 0 ? -err : 0;
}