#include "vmlinux.h"
#include <bpf/bpf_helpers.h>
#include <bpf/bpf_tracing.h>
#include <bpf/bpf_core_read.h>
#include "systracer.h"


struct {
    __uint(type, BPF_MAP_TYPE_RINGBUF);
    __uint(max_entries, 8 * 1024 * 1024);
} events SEC(".maps");

struct
{
    __uint(type, BPF_MAP_TYPE_ARRAY);
    __type(key, u32);
    __type(value, u64);
    __uint(max_entries, 1);
} lost_events SEC(".maps");

struct 
{
    __uint(type, BPF_MAP_TYPE_HASH);
    __type(key, u32);
    __type(value, u8);
    __uint(max_entries, 1024);
} pid_map SEC(".maps");

struct block_rq_submission {
    uint16_t common_type;
    uint8_t common_flags;
    uint8_t common_preempt_count;
    int32_t common_pid;
    uint32_t dev;
    uint64_t sector;
    uint32_t nr_sectors;
    uint32_t nr_bytes;
    uint16_t ioprio;
    int8_t rwbs[10];
    int8_t comm[16];
    int8_t cmd[4];
};

struct sched_process_fork_ctx {
    u16 common_type;
    u8 common_flags;
    u8 common_preempt_count;
    s32 common_pid;
    char parent_comm[16];
    pid_t parent_pid;
    char child_comm[16];
    pid_t child_pid;
};

int create_and_submit_event(u32 pid, u64 id, int layer) {
    struct data_t *event = bpf_ringbuf_reserve(&events, sizeof(struct data_t), 0);
    if (event == NULL) {
        u32 key=0;
        u64 *lost_counter = bpf_map_lookup_elem(&lost_events, &key);
        if(lost_counter){
            __sync_fetch_and_add(lost_counter, 1);
            return 0;
        }
        return 1;       
    }

    event->pid = pid;
    event->syscall_id = id;
    event->layer = layer;
    bpf_get_current_comm(&event->command, sizeof(event->command));

    bpf_ringbuf_submit(event, 0);

    return 0;
}

int to_discard(u32 pid) {
    u8 *exists = bpf_map_lookup_elem(&pid_map, &pid);
    return (exists) ? 0 : 1;
}

SEC("tp/syscalls/sys_enter_openat")
int BPF_PROG(sys_enter_openat, struct pt_regs *regs, long syscall_id, int dfd, const char *filename) {
    u32 pid = bpf_get_current_pid_tgid() >> 32;
    if (to_discard(pid)) return 0;
    return create_and_submit_event(pid, (u64)syscall_id, 0);
}

SEC("tp/syscalls/sys_enter_close")
int BPF_PROG(sys_enter_close, struct pt_regs *regs, long syscall_id, int dfd, const char *filename) {
    u32 pid = bpf_get_current_pid_tgid() >> 32;
    if (to_discard(pid)) return 0;
    return create_and_submit_event(pid, (u64)syscall_id, 0);
}

SEC("tp/syscalls/sys_enter_write")
int BPF_PROG(sys_enter_write, struct pt_regs *regs, long syscall_id, int dfd, const char *filename) {
    u32 pid = bpf_get_current_pid_tgid() >> 32;
    if (to_discard(pid)) return 0;
    return create_and_submit_event(pid, (u64)syscall_id, 0);
}

SEC("tp/syscalls/sys_enter_read")
int BPF_PROG(sys_enter_read, struct pt_regs *regs, long syscall_id, int dfd, const char *filename) {
    u32 pid = bpf_get_current_pid_tgid() >> 32;
    if (to_discard(pid)) return 0;
    return create_and_submit_event(pid, (u64)syscall_id, 0);
}

SEC("kprobe/vfs_read")
int BPF_KPROBE(vfs_enter_read, struct file *file, char *buf, size_t count, loff_t *pos) {
    u32 pid = bpf_get_current_pid_tgid() >> 32;
    if (to_discard(pid)) return 0;
    return create_and_submit_event(pid, MYOP_READ, 1); 
}

SEC("kprobe/vfs_write")
int BPF_KPROBE(vfs_enter_write, struct file *file, const char *buf, size_t count, loff_t *pos) {
    u32 pid = bpf_get_current_pid_tgid() >> 32;
    if (to_discard(pid)) return 0;
    return create_and_submit_event(pid, MYOP_WRITE, 1);
}

SEC("kprobe/ext4_file_read_iter") 
int BPF_KPROBE(ext4_file_read_iter, struct kiocb *iocb, struct iov_iter *to) {
    u32 pid = bpf_get_current_pid_tgid() >> 32;
    if (to_discard(pid)) return 0;
    return create_and_submit_event(pid, MYOP_FILE_READ, 2);
}

SEC("kprobe/ext4_file_write_iter") 
int BPF_KPROBE(ext4_file_write_iter, struct kiocb *iocb, struct iov_iter *from) {
    u32 pid = bpf_get_current_pid_tgid() >> 32;
    if (to_discard(pid)) return 0;
    return create_and_submit_event(pid, MYOP_FILE_WRITE, 2);
}


SEC("tp/block/block_rq_issue")
int trace_block_rq_issue(struct block_rq_submission *ctx) {
    u32 pid = bpf_get_current_pid_tgid() >> 32;
    if (to_discard(pid)) return 0;

    if (ctx->rwbs[0] == 'R') 
        return create_and_submit_event(pid, MYOP_BLOCK_READ, 3);
    else if (ctx->rwbs[0] == 'W') 
        return create_and_submit_event(pid, MYOP_BLOCK_WRITE, 3);
    
    return 0;
};

SEC("tp/sched/sched_process_fork")
int trace_sched_process_fork(struct sched_process_fork_ctx *ctx) {
    u32 parent_pid = ctx->parent_pid;
    u32 child_pid = ctx->child_pid;

    if (!to_discard(parent_pid)) {
        u8 val = 1;
        bpf_map_update_elem(&pid_map, &child_pid, &val, BPF_ANY);
    }
    
    return 0;
}

char LICENSE[] SEC("license") = "Dual BSD/GPL";
