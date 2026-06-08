#include "vmlinux.h"
#include <bpf/bpf_helpers.h>
#include <bpf/bpf_tracing.h>
#include <bpf/bpf_core_read.h>
#include "systracer.h"
#include "hash.h"

#define MYOP_OPEN 56
#define MYOP_READ 63
#define MYOP_WRITE 64

// Define a ring buffer map to send data to user space
struct {
    __uint(type, BPF_MAP_TYPE_RINGBUF);
    __uint(max_entries, 8 * 1024 * 1024);
} events SEC(".maps");

// Define a map to count lost events due to ring buffer full
struct {
    __uint(type, BPF_MAP_TYPE_ARRAY);
    __type(key, u32);
    __type(value, u64);
    __uint(max_entries, 1);
} lost_events SEC(".maps");

// Define a map to store the pids to be filtered (i.e., traced)
struct {
    __uint(type, BPF_MAP_TYPE_HASH);
    __uint(max_entries, 100);
    __type(key, u32);
    __type(value, u32);
} my_config SEC(".maps");

// Define a structure for block request submission event
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
    int8_t rwbs[10]; // Read / Write / Flush / Discard + flags (Sync, Meta, etc.)
    int8_t comm[16];
    int8_t cmd[4];
};

/* Function to check if pid is on my_config map or not
 * Receives a pid (u32 pid) as argument
 * Returns 1 (to discard) or 0 (to collect)
 */
int to_discard (u32 pid) { // 1 - discard, 0 - collect
    u32 *pid_to_filter = bpf_map_lookup_elem(&my_config, &pid);
    if(pid_to_filter) return 0;
    return 1;
}

/* Function to get struct file* from a file descriptor
 * Receives a file descriptor (int fd) as argument
 * Returns the corresponding struct file* or NULL
 */
struct file* get_file_from_fd(int fd) {
    struct task_struct *ts = (struct task_struct*)bpf_get_current_task();
    struct file **fd_array = BPF_CORE_READ(ts, files, fdt, fd);
    struct file *file;
    bpf_core_read(&file, sizeof(file), &fd_array[fd]);
    return file;
}

/* Function to check if the file mode is regular file or not
 * Receives a umode_t mode as argument
 * Returns 1 (not regular file) or 0 (regular file)
 */
int check_file_mode(umode_t mode) {
    if ((mode & S_IFMT) != S_IFREG) return 1;
    return 0;
}

/* Function to check if the file mode is regular file or not
 * Receives a struct file* as argument
 * Returns 1 (not regular file) or 0 (regular file)
 */
int check_file_mode_from_file(struct file *file) {
    struct dentry *d = BPF_CORE_READ(file, f_path.dentry);
    struct inode *inode = BPF_CORE_READ(d, d_inode);
    umode_t mode = BPF_CORE_READ(inode, i_mode);
    return check_file_mode(mode);
}

/* Function to check if the file mode is regular file or not
 * Receives a struct path* as argument
 * Returns 1 (not regular file) or 0 (regular file)
 */
int check_file_mode_from_path(const struct path *path) {
    struct dentry *d = BPF_CORE_READ(path, dentry);
    struct inode *inode = BPF_CORE_READ(d, d_inode);
    umode_t mode = BPF_CORE_READ(inode, i_mode);
    return check_file_mode(mode);
}

/* Function to check if the file mode is regular file or not
 * Receives a struct inode* as argument
 * Returns 1 (not regular file) or 0 (regular file)
 */
int check_file_mode_from_inode(struct inode *inode) {
    umode_t mode = BPF_CORE_READ(inode, i_mode);
    return check_file_mode(mode);
}

/* Function to create and submit an event to user space for read/write operations
 * Receives the syscall id (u64 id), the type of event (int type), the filename (const unsigned char *filename), the size of the operation (size_t count) and the hash of the buffer (u32 hash) as arguments
 * Returns 0 on success or 1 on failure
 */
int create_and_submit_rw_event(u64 id, int type, const unsigned char *filename, size_t count, u32 hash) {

    // reserve space in ring buffer
    struct data_t *data = bpf_ringbuf_reserve(&events, sizeof(struct data_t), 0);
    if(!data){
        // if failed to reserve space in ring buffer, increase the lost events counter in the lost_events map and return 0 to ignore this event. This way we can keep track of how many events are lost due to ring buffer full.
        u32 key=0;
        u64 *lost_counter = bpf_map_lookup_elem(&lost_events, &key);
        if(lost_counter){
            __sync_fetch_and_add(lost_counter, 1);
            return 0;
        }

        return 1;
    }

    // fill data structure
    data->pid = bpf_get_current_pid_tgid() >> 32;
    data->syscall_id = id;
    data->type = type;
    data->size = count;
    data->hash = hash;
    bpf_get_current_comm(&data->command, sizeof(data->command));
    bpf_probe_read(data->filename, sizeof(data->filename), filename);

    // send data to user space
    bpf_ringbuf_submit(data, 0);

    return 0;
}

/* Function to create and submit an event to user space for other operations
 * Receives the syscall id (u64 id), the type of event (int type), and the filename (const unsigned char *filename) as arguments
 * Returns 0 on success or 1 on failure
 */
int create_and_submit_other_event(u64 id, int type, const unsigned char *filename) {

    // reserve space in ring buffer
    struct data_t *data = bpf_ringbuf_reserve(&events, sizeof(struct data_t), 0);
    if(!data){
        // if failed to reserve space in ring buffer, increase the lost events counter in the lost_events map and return 0 to ignore this event. This way we can keep track of how many events are lost due to ring buffer full.
        u32 key=0;
        u64 *lost_counter = bpf_map_lookup_elem(&lost_events, &key);
        if(lost_counter){
            __sync_fetch_and_add(lost_counter, 1);
            return 0;
        }

        return 1;
    }

    // fill data structure
    data->pid = bpf_get_current_pid_tgid() >> 32;
    data->syscall_id = id;
    data->type = type;
    data->size = 0;
    data->hash = 0;
    bpf_get_current_comm(&data->command, sizeof(data->command));
    bpf_probe_read(data->filename, sizeof(data->filename), filename);

    // send data to user space
    bpf_ringbuf_submit(data, 0);

    return 0;
}

/* ----SYSCALL-----  */
/* Probes at the system call layer for open, read, pread, write, pwrite and close operations */

SEC("tp/syscalls/sys_enter_openat")
int BPF_PROG(sys_enter_openat, struct pt_regs *regs, long syscall_id, int dfd, const char * filename, int flags, umode_t mode)
{
    // check if the pid is in the filter list or not. If not, return 0 to ignore this event.
    u32 pid = bpf_get_current_pid_tgid() >> 32;
    if (to_discard(pid)) return 0;

    // NOTE: At the entry point of the openat syscall, we can only get the filename from the syscall's arguments.
    // The file structure is not yet created, so it can only be accessed at the syscall's exit.
    return create_and_submit_other_event(syscall_id, L_SYSCALL, (const unsigned char *)filename);
}

SEC("tp/syscalls/sys_enter_write")
int BPF_PROG(sys_enter_write, struct pt_regs *regs, long syscall_id, u32 fd, char *buf, size_t count)
{
    // check if the pid is in the filter list or not. If not, return 0 to ignore this event.
    u32 pid = bpf_get_current_pid_tgid() >> 32;
    if (to_discard(pid)) return 0;

    // get file path from fd and check if it is a regular file. If not, return 0 to ignore this event.
    struct file *file = get_file_from_fd(fd);
    if (check_file_mode_from_file(file)) return 0;

    // get filename from struct file
    const unsigned char * filename = BPF_CORE_READ(file, f_path.dentry, d_name.name);

    // calculate hash of the buffer content
    uint32_t hash = xxhash32(buf, count, 12345);

    return create_and_submit_rw_event(syscall_id, L_SYSCALL, filename, count, hash);
}

SEC("tp/syscalls/sys_enter_pwrite64")
int BPF_PROG(sys_enter_pwrite64, struct pt_regs *regs, long syscall_id, u32 fd, char *buf, size_t count)
{
    // check if the pid is in the filter list or not. If not, return 0 to ignore this event.
    u32 pid = bpf_get_current_pid_tgid() >> 32;
    if (to_discard(pid)) return 0;

    // get file path from fd and check if it is a regular file. If not, return 0 to ignore this event.
    struct file *file = get_file_from_fd(fd);
    if (check_file_mode_from_file(file)) return 0;

    // get filename from struct file
    const unsigned char * filename = BPF_CORE_READ(file, f_path.dentry, d_name.name);

    // calculate hash of the buffer content
    uint32_t hash = xxhash32(buf, count, 12345);

    return create_and_submit_rw_event(syscall_id, L_SYSCALL, filename, count, hash);
}

SEC("tp/syscalls/sys_enter_read")
int BPF_PROG(sys_enter_read, struct pt_regs *regs, long syscall_id, u32 fd, char *buf, size_t count)
{
    // check if the pid is in the filter list or not. If not, return 0 to ignore this event.
    u32 pid = bpf_get_current_pid_tgid() >> 32;
    if (to_discard(pid)) return 0;

    // get file path from fd and check if it is a regular file. If not, return 0 to ignore this event.
    struct file *file = get_file_from_fd(fd);
    if (check_file_mode_from_file(file)) return 0;

    // get filename from struct file
    const unsigned char * filename = BPF_CORE_READ(file, f_path.dentry, d_name.name);

    // NOTE 1: At the entry point of the pread64 syscall, the buffer content is not yet available.
    // We can only access it at the exit, so we submit the event with hash = 0 here.
    // NOTE 2: The count argument represents the requested read size, not the actual read size, which can only be accessed at the exit of the read syscall.
    return create_and_submit_rw_event(syscall_id, L_SYSCALL, filename, count, 0);
}

SEC("tp/syscalls/sys_enter_pread64")
int BPF_PROG(sys_enter_pread64, struct pt_regs *regs, long syscall_id, u32 fd, char *buf, size_t count)
{
    // check if the pid is in the filter list or not. If not, return 0 to ignore this event.
    u32 pid = bpf_get_current_pid_tgid() >> 32;
    if (to_discard(pid)) return 0;

    // get file path from fd and check if it is a regular file. If not, return 0 to ignore this event.
    struct file *file = get_file_from_fd(fd);
    if (check_file_mode_from_file(file)) return 0;

    // get filename from struct file
    const unsigned char * filename = BPF_CORE_READ(file, f_path.dentry, d_name.name);

    // NOTE 1: At the entry point of the pread64 syscall, the buffer content is not yet available.
    // We can only access it at the exit, so we submit the event with hash = 0 here.
    // NOTE 2: The count argument represents the requested read size, not the actual read size, which can only be accessed at the exit of the pread64 syscall.
    return create_and_submit_rw_event(syscall_id, L_SYSCALL, filename, count, 0);
}

SEC("tp/syscalls/sys_enter_close")
int BPF_PROG(sys_enter_close, struct pt_regs *regs, long syscall_id, u32 fd)
{
    // check if the pid is in the filter list or not. If not, return 0 to ignore this event.
    u32 pid = bpf_get_current_pid_tgid() >> 32;
    if (to_discard(pid)) return 0;

    // get file path from fd and check if it is a regular file. If not, return 0 to ignore this event.
    struct file *file = get_file_from_fd(fd);
    if (check_file_mode_from_file(file)) return 0;

    // get filename from struct file
    const unsigned char * filename = BPF_CORE_READ(file, f_path.dentry, d_name.name);

    return create_and_submit_other_event(syscall_id, L_SYSCALL, filename);
}

/* ----VFS-----  */
/* Probes at the VFS layer for open, read and write operations. */

// NOTE: vfs_open is called only for files already created.
// int vfs_open(const struct path *path, struct file *file)
SEC("kprobe/vfs_open")
int BPF_KPROBE(vfs_enter_open, const struct path *path, struct file *file)
{
    // check if the pid is in the filter list or not. If not, return 0 to ignore this event.
    u32 pid = bpf_get_current_pid_tgid() >> 32;
    if (to_discard(pid)) return 0;

    // check if the file mode is regular file or not. If not, return 0 to ignore this event.
    if (check_file_mode_from_path(path)) return 0;

    // get filename from struct path
    const unsigned char * filename = BPF_CORE_READ(path , dentry, d_name.name);

    return create_and_submit_other_event(MYOP_OPEN, L_VFS, filename);
}

// ssize_t vfs_write(struct file *, const char __user *, size_t, loff_t *)
SEC("kprobe/vfs_write")
int BPF_KPROBE(vfs_enter_write, struct file *file, const char *buf, size_t count, loff_t *pos)
{
    // check if the pid is in the filter list or not. If not, return 0 to ignore this event.
    u32 pid = bpf_get_current_pid_tgid() >> 32;
    if (to_discard(pid)) return 0;

    // check if the file mode is regular file or not. If not, return 0 to ignore this event.
    if (check_file_mode_from_file(file)) return 0;

    // get filename from struct file
    const unsigned char * filename = BPF_CORE_READ(file, f_path.dentry, d_name.name);

    // calculate hash of the buffer content
    uint32_t hash = xxhash32(buf, count, 12345);

    return create_and_submit_rw_event(MYOP_WRITE, L_VFS, filename, count, hash);
}

// ssize_t vfs_read(struct file *, char __user *, size_t, loff_t *);
SEC("kprobe/vfs_read")
int BPF_KPROBE(vfs_read_entry, struct file *file, char *buf, size_t count, loff_t *pos)
{
    // check if the pid is in the filter list or not. If not, return 0 to ignore this event.
    u32 pid = bpf_get_current_pid_tgid() >> 32;
    if (to_discard(pid)) return 0;

    // check if the file mode is regular file or not. If not, return 0 to ignore this event.
    if (check_file_mode_from_file(file)) return 0;

    // get filename from struct file
    const unsigned char * filename = BPF_CORE_READ(file, f_path.dentry, d_name.name);

    // NOTE 1: At the entry point of the vfs_read, the buffer content is not yet available.
    // We can only access it at the exit, so we submit the event with hash = 0 here.
    // NOTE 2: The count argument represents the requested read size, not the actual read size, which can only be accessed at the exit of the vfs_read operation.
    return create_and_submit_rw_event(MYOP_READ, L_VFS, filename, count, 0);
}

/* ----EXT4-----  */
/* Probes at the EXT4 layer for open, read and write operations. */

// int ext4_file_open(struct inode *inode, struct file *filp)
SEC("kprobe/ext4_file_open")
int BPF_KPROBE(ext4_enter_open, struct inode *inode, struct file *filp)
{
    // check if the pid is in the filter list or not. If not, return 0 to ignore this event.
    u32 pid = bpf_get_current_pid_tgid() >> 32;
    if (to_discard(pid)) return 0;

    // check if the file mode is regular file or not. If not, return 0 to ignore this event.
    if (check_file_mode_from_file(filp)) return 0;

    // get filename from struct file
    const unsigned char * filename = BPF_CORE_READ(filp, f_path.dentry, d_name.name);

    return create_and_submit_other_event(MYOP_OPEN, L_EXT4, filename);
}

// ssize_t ext4_file_write_iter(struct kiocb *iocb, struct iov_iter *from)
SEC("kprobe/ext4_file_write_iter")
int BPF_KPROBE(ext4_enter_write, struct kiocb *iocb, struct iov_iter *from)
{
    // check if the pid is in the filter list or not. If not, return 0 to ignore this event.
    u32 pid = bpf_get_current_pid_tgid() >> 32;
    if (to_discard(pid)) return 0;

    // get file struct from iocb and check if it is a regular file. If not, return 0 to ignore this event.
    struct file* file;
    bpf_core_read(&file, sizeof(void *), &iocb->ki_filp);
    if (check_file_mode_from_file(file)) return 0;

    // get filename from struct file
    const unsigned char * filename = BPF_CORE_READ(file, f_path.dentry, d_name.name);

    // get data from struct iov_iter
    struct iovec v;
    const struct iovec *iovp;
    size_t off, size;

    // NOTE: For simplicity, we only handle the case of a single contiguous user-space buffer (i.e., ITER_UBUF) in this example, which is common with write/read operations. Other cases (e.g., ITER_IOVEC, ITER_KVEC, etc.) can be handled similarly by iterating over the iovec array and calculating the hash for each buffer.
    u8 type = BPF_CORE_READ(from, iter_type);
    if (type != ITER_UBUF) return 0;
    iovp = &from->__ubuf_iovec;
    bpf_core_read(&v, sizeof(v), iovp);
    off = BPF_CORE_READ(from, iov_offset);
    size = v.iov_len - off;

    // calculate hash of the buffer content
    uint32_t hash = xxhash32(v.iov_base + off, size, 12345);

    return create_and_submit_rw_event(MYOP_WRITE, L_EXT4, filename, size, hash);
}

// ssize_t ext4_file_read_iter(struct kiocb *iocb, struct iov_iter *to)
SEC("kprobe/ext4_file_read_iter")
int BPF_KPROBE(ext4_enter_read, struct kiocb *iocb, struct iov_iter *to)
{
    // check if the pid is in the filter list or not. If not, return 0 to ignore this event.
    u32 pid = bpf_get_current_pid_tgid() >> 32;
    if (to_discard(pid)) return 0;

    // get file struct from iocb and check if it is a regular file. If not, return 0 to ignore this event.
    struct file* file;
    bpf_core_read(&file, sizeof(void *), &iocb->ki_filp);
    if (check_file_mode_from_file(file)) return 0;

    // get filename from struct file
    const unsigned char * filename = BPF_CORE_READ(file, f_path.dentry, d_name.name);

    // get data size from struct iov_iter
    // NOTE: this is the requested read size, not the actual read size, which can only be accessed at the exit of the ext4_file_read_iter.
    size_t size = BPF_CORE_READ(to, count);

    // NOTE: At the entry point of the ext4_file_read_iter, the buffer content is not yet available.
    // We can only access it at the exit, so we submit the event with hash = 0 here.
    return create_and_submit_rw_event(MYOP_READ, L_EXT4, filename, size, 0);
}

/* ----FUSE-----  */
/* Probes at the FUSE file system for open, read, and write operations */

// static int fuse_open(struct inode *inode, struct file *file)
SEC("kprobe/fuse_open")
int BPF_KPROBE(fuse_enter_open, struct inode *inode, struct file *file)
{
    // check if the pid is in the filter list or not. If not, return 0 to ignore this event.
    u32 pid = bpf_get_current_pid_tgid() >> 32;
    if (to_discard(pid)) return 0;

    // check if the file mode is regular file or not. If not, return 0 to ignore this event.
    if (check_file_mode_from_inode(inode)) return 0;

    // get filename from struct file
    const unsigned char * filename = BPF_CORE_READ(file, f_path.dentry, d_name.name);

    return create_and_submit_other_event(MYOP_OPEN, L_FUSE, filename);
}

// static ssize_t fuse_file_read_iter(struct kiocb *iocb, struct iov_iter *from)
SEC("kprobe/fuse_file_read_iter")
int BPF_KPROBE(fuse_enter_file_read_iter, struct kiocb *iocb, struct iov_iter *to)
{
    // check if the pid is in the filter list or not. If not, return 0 to ignore this event.
    u32 pid = bpf_get_current_pid_tgid() >> 32;
    if (to_discard(pid)) return 0;

    // get file struct from iocb and check if it is a regular file. If not, return 0 to ignore this event.
    struct file* file;
    bpf_core_read(&file, sizeof(void *), &iocb->ki_filp);
    if (check_file_mode_from_file(file)) return 0;

    // get filename from struct file
    const unsigned char * filename = BPF_CORE_READ(file, f_path.dentry, d_name.name);

    // get data size from struct iov_iter
    // NOTE: this is the requested read size, not the actual read size, which can only be accessed at the exit of the fuse_file_read_iter.
    size_t size = BPF_CORE_READ(to, count);

    // NOTE: At the entry point of the fuse_file_read_iter, the buffer content is not yet available.
    // We can only access it at the exit, so we submit the event with hash = 0 here.
    return create_and_submit_rw_event(MYOP_READ, L_FUSE, filename, size, 0);
}

// static ssize_t fuse_file_write_iter(struct kiocb *iocb, struct iov_iter *from)
SEC("kprobe/fuse_file_write_iter")
int BPF_KPROBE(fuse_enter_file_write_iter, struct kiocb *iocb, struct iov_iter *from)
{
    // check if the pid is in the filter list or not. If not, return 0 to ignore this event.
    u32 pid = bpf_get_current_pid_tgid() >> 32;
    if (to_discard(pid)) return 0;

    // get file struct from iocb and check if it is a regular file. If not, return 0 to ignore this event.
    struct file* file;
    bpf_core_read(&file, sizeof(void *), &iocb->ki_filp);
    if (check_file_mode_from_file(file)) return 0;

    // get filename from struct file
    const unsigned char * filename = BPF_CORE_READ(file, f_path.dentry, d_name.name);

    // get data from struct iov_iter
    struct iovec v;
    const struct iovec *iovp;
    size_t off, size;

    // NOTE: For simplicity, we only handle the case of a single contiguous user-space buffer (i.e., ITER_UBUF) in this example, which is common with write/read operations. Other cases (e.g., ITER_IOVEC, ITER_KVEC, etc.) can be handled similarly by iterating over the iovec array and calculating the hash for each buffer.
    u8 type = BPF_CORE_READ(from, iter_type);
    if (type != ITER_UBUF) return 0;
    iovp = &from->__ubuf_iovec;
    bpf_core_read(&v, sizeof(v), iovp);
    off = BPF_CORE_READ(from, iov_offset);
    size = v.iov_len - off;

    // calculate hash of the buffer content
    uint32_t hash = xxhash32(v.iov_base + off, size, 12345);

    return create_and_submit_rw_event(MYOP_WRITE, L_FUSE, filename, size, hash);
}

/* ----BIO-----  */
/* Probes at the BIO layer for read and write operations. */

// sudo cat /sys/kernel/tracing/events/block/block_rq_issue/format
SEC("tp/block/block_rq_issue")
int trace_block_rq_insertstruct(struct block_rq_submission *ctx)
{
    // check if the pid is in the filter list or not. If not, return 0 to ignore this event.
    u32 pid = bpf_get_current_pid_tgid() >> 32;
    if (to_discard(pid)) return 0;

    // Check if this was a read or write based on rwbs flags
    if (ctx->rwbs[0] == 'R') {
        return create_and_submit_rw_event(MYOP_READ, L_BIO, NULL, ctx->nr_bytes, 0);
    } else if (ctx->rwbs[0] == 'W') {
        return create_and_submit_rw_event(MYOP_WRITE, L_BIO, NULL, ctx->nr_bytes, 0);
    }

    return 0;
}

/* ----PROC-----  */

// sudo cat /sys/kernel/tracing/events/sched/sched_process_fork/format
SEC("tracepoint/sched/sched_process_fork")
int trace_sched_process_fork(struct trace_event_raw_sched_process_fork *ctx)
{
    pid_t parent_pid = ctx->parent_pid;
    pid_t child_pid = ctx->child_pid;
    int value=1;

    // check if the parent pid is in the filter list or not. If not, return 0 to ignore this event.
    if (to_discard(parent_pid)) return 0;

    // Add the child pid to the filter list to trace it as well
    bpf_map_update_elem(&my_config, &child_pid, &value, 0);

    return 0;
}

char LICENSE[] SEC("license") = "Dual BSD/GPL";