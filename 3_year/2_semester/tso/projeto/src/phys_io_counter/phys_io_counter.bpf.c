/**
 * @file phys_io_counter.bpf.c
 * @brief eBPF kernel-space program for intercepting and counting block I/O requests.
 */

#include "vmlinux.h"
#include <bpf/bpf_helpers.h>
#include "phys_io_counter.h"

/**
 * @brief eBPF map to store the global I/O statistics.
 * 
 * A single-entry array map containing the io_stats_t structure.
 */
struct {
    __uint(type, BPF_MAP_TYPE_ARRAY);
    __uint(max_entries, 1);
    __type(key, u32);
    __type(value, io_stats_t);
} io_stats_map SEC(".maps");

/**
 * @brief Context structure provided by the tracepoint tp/block/block_rq_issue.
 */
struct block_rq_submission {
    u16 common_type;
    u8 common_flags;
    u8 common_preempt_count;
    s32 common_pid;
    u32 dev;
    u64 sector;
    u32 nr_sectors;
    u32 nr_bytes;
    u16 ioprio;
    s8 rwbs[10];
    s8 comm[16];
    s8 cmd[4];
};

/**
 * @brief Tracepoint handler for block I/O issue events.
 * 
 * Inspects the I/O request type (read or write) and increments the corresponding
 * counters in the io_stats_map.
 * 
 * @param ctx The tracepoint context containing request details.
 * @return 0 on success.
 */
SEC("tp/block/block_rq_issue")
int handle_block_rq_issue(struct block_rq_submission *ctx)
{
    u64 bytes = (u64)ctx->nr_bytes;
    u32 key = 0;
    io_stats_t *stats = bpf_map_lookup_elem(&io_stats_map, &key);
    
    if (stats) {
        if (ctx->rwbs[0] == 'W' || ctx->rwbs[1] == 'W') {
            __sync_fetch_and_add(&stats->write_bytes, bytes);
            __sync_fetch_and_add(&stats->write_ops, 1);
        } else if (ctx->rwbs[0] == 'R' || ctx->rwbs[1] == 'R') {
            __sync_fetch_and_add(&stats->read_bytes, bytes);
            __sync_fetch_and_add(&stats->read_ops, 1);
        }
    }
    
    return 0;
}

char LICENSE[] SEC("license") = "GPL";
