/**
 * @file phys_io_counter.c
 * @brief User-space controller for the physical I/O counter eBPF tool.
 * 
 * Loads the eBPF program into the kernel, polls the shared map for statistics,
 * and outputs the metrics to standard output.
 */

#include <stdio.h>
#include <unistd.h>
#include <signal.h>
#include <bpf/libbpf.h>
#include <bpf/bpf.h>
#include "phys_io_counter.h"
#include "phys_io_counter.skel.h"

// Variável global para controlar o ciclo de vida do programa
volatile sig_atomic_t keep_running = 1;

// Ctrl+C
void sigint_handler(int signo) {
    keep_running = 0;
}

/**
 * @brief Main entry point for the user-space controller.
 * 
 * @param argc Argument count.
 * @param argv Argument vector.
 * @return 0 on success, non-zero on error.
 */
int main(int argc, char **argv)
{
    struct phys_io_counter_bpf *skel;

    signal(SIGINT, sigint_handler);

    skel = phys_io_counter_bpf__open_and_load();
    if (!skel) return 1;

    if (phys_io_counter_bpf__attach(skel)) {
        phys_io_counter_bpf__destroy(skel);
        return 1;
    }

    printf("%-10s | %-15s %-15s %-15s | %-15s %-15s %-15s\n", 
           "TIME(s)", "RD_MB", "RD_MB/s", "RD_OPS/s", "WR_MB", "WR_MB/s", "WR_OPS/s");

    int map_fd = bpf_map__fd(skel->maps.io_stats_map);
    int seconds = 0;
    __u32 key = 0;
    io_stats_t prev = {0}, cur;

    while (keep_running) {
        sleep(1);
        seconds++;

        if (bpf_map_lookup_elem(map_fd, &key, &cur) != 0) continue;

        double rd_mb = (double)cur.read_bytes / 1048576.0;
        double rd_mb_s = (double)(cur.read_bytes - prev.read_bytes) / 1048576.0;
        double rd_ops_s = (double)(cur.read_ops - prev.read_ops);

        double wr_mb = (double)cur.write_bytes / 1048576.0;
        double wr_mb_s = (double)(cur.write_bytes - prev.write_bytes) / 1048576.0;
        double wr_ops_s = (double)(cur.write_ops - prev.write_ops);

        printf("%-10d | %-15.2f %-15.2f %-15.2f | %-15.2f %-15.2f %-15.2f\n",
               seconds, rd_mb, rd_mb_s, rd_ops_s, wr_mb, wr_mb_s, wr_ops_s);

        prev = cur;
    }

    phys_io_counter_bpf__destroy(skel);
    return 0;
}
