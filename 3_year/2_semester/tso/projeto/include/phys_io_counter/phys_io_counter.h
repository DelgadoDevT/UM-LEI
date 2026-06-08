/**
 * @file phys_io_counter.h
 * @brief Shared data structures for the physical I/O counter eBPF tool.
 */

#ifndef PHYS_IO_COUNTER_H
#define PHYS_IO_COUNTER_H

/* In user-space, we need linux/types.h for __u64. 
   In kernel-space, vmlinux.h already provides it. */
#ifndef __VMLINUX_H__
#include <linux/types.h>
#endif

/**
 * @brief Structure to hold aggregated physical I/O statistics.
 * 
 * This structure is used as the value type in the eBPF map to share
 * metrics between kernel-space and user-space.
 */
typedef struct {
    __u64 read_bytes;  /**< Total bytes read from the device. */
    __u64 read_ops;    /**< Total read operations executed. */
    __u64 write_bytes; /**< Total bytes written to the device. */
    __u64 write_ops;   /**< Total write operations executed. */
} io_stats_t;

#endif // PHYS_IO_COUNTER_H