# TSO-2526 Deduplication File System

A high-performance, FUSE-based file system that implements inline, block-level data deduplication using SHA-256 hashing. It chunks data into fixed blocks, stores unique data in container files, and utilizes reference counting with garbage collection to highly optimize physical storage. Includes an eBPF tool for kernel-level I/O observability.

[![C](https://img.shields.io/badge/C-11%2B-blue.svg)](https://en.wikipedia.org/wiki/C_(programming_language))
[![FUSE](https://img.shields.io/badge/FUSE-3-orange.svg)]()
[![Observability](https://img.shields.io/badge/Kernel-eBPF-purple.svg)]()

## Implemented features

- **Block-level inline deduplication** with fixed 4KB chunks and SHA-256 hashing.
- **Reference counting & Garbage Collection**: Automatically reclaims physical container space when files are deleted or modified.
- **Persistent storage**: Serializes metadata (GLib Hash Tables) to disk, surviving system reboots.
- **Virtual Statistics File**: Real-time readouts of Logical Size, Physical Size, and Dedup Ratio via `/stats.txt`.
- **eBPF Kernel Observability**: Independent validation tool (`phys_io_counter`) that hooks into `block_rq_issue` to prove physical disk write suppression.

## Installation and setup

### Prerequisites

You need the FUSE 3 development headers, GLib 2.0 (for advanced data structures), and OpenSSL (for cryptography).
```bash
sudo apt-get update
sudo apt-get install libfuse3-dev libglib2.0-dev libssl-dev
```

### Build the project

Compile the file system and the eBPF monitoring tool using the provided Makefile:

```bash
make clean && make all
```

## Run the system

### 1. Create directories
You need a directory for the persistent physical storage and a directory to serve as the FUSE mount point.
```bash
# Create the physical storage directory
mkdir -p ~/TSO_DEDUP_DATA

# Create the FUSE mount point
sudo mkdir -p /mnt/dedup
```

### 2. Start the File System
Start the FUSE daemon. The `-f` flag keeps it in the foreground for debugging, and `-o allow_other` allows non-root users to interact with it.
```bash
sudo ./dedup_fs /mnt/dedup_fs --root_dir ~/TSO_DEDUP_DATA -f -o allow_other
```

### 3. Start the eBPF Observability Tool
To monitor actual physical writes to the disk and prove the system's efficiency, open a **new terminal** and run:
```bash
sudo ./phys_io_counter
```
*Press `Ctrl+C` to safely stop the eBPF monitor when you are done.*

### 4. Verify Deduplication
Inside the mounted directory, you can instantly check the performance and space savings by reading the dynamically generated stats file:
```bash
cat /mnt/dedup/stats.txt
```

### 5. Unmount
When you are finished testing, safely unmount the file system to trigger the persistent metadata save:
```bash
sudo fusermount3 -u /mnt/dedup
```

## Authors

<table>
<tr>
<td align="center">
<a href="https://github.com/Barrocas21">
<img src="https://github.com/Barrocas21.png" width="100px;" alt="Barrocas21"/><br />
<sub><b>Barrocas21</b></sub>
</a><br/>
</td>
<td align="center">
<a href="https://github.com/DelgadoDevT">
<img src="https://github.com/DelgadoDevT.png" width="100px;" alt="DelgadoDevT"/><br />
<sub><b>DelgadoDevT</b></sub>
</a><br/>
</td>
<td align="center">
<a href="https://github.com/migukau">
<img src="https://github.com/migukau.png" width="100px;" alt="migukau"/><br />
<sub><b>migukau</b></sub>
</a><br/>
</td>
</tr>
</table>