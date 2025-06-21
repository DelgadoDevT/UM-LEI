
# Document Indexing System

## Overview

This project, developed for the Operating Systems course at University of Minho, implements a **Document Indexing System** using a **client-server model** built in C. It allows users to index, search, and manage metadata for text documents stored locally. The system features persistent storage, a custom caching mechanism, and support for concurrent operations via child processes.

Communication between the `dclient` (client) and `dserver` (server) is handled via **named pipes (FIFOs)** to ensure asynchronous, isolated, and scalable interactions.

## Contributors

- [DelgadoDevT](https://github.com/DelgadoDevT)
- [PaoComPlanta](https://github.com/PaoComPlanta)
- [TiagoBrito5](https://github.com/TiagoBrito5)

## Features

### Indexing (`-a`)
Adds a document's metadata (title, author, year, path) to the system. Metadata is appended or inserted in the first available free space.

```bash
./bin/dclient -a "title" "authors" "year" "path"
```

### Metadata Query (`-c`)
Retrieves metadata for a specific document using its key. Optimized via a caching system.

```bash
./bin/dclient -c "key"
```

### Remove Entry (`-d`)
Marks metadata as invalid and adds its position to the free queue for future reuse.

```bash
./bin/dclient -d "key"
```

### Line Count by Keyword (`-l`)
Counts the number of lines containing a specific keyword in a document.

```bash
./bin/dclient -l "key" "keyword"
```

### Global Search (`-s`)
Performs a parallel search across all documents using multiple processes (controlled by user).

```bash
./bin/dclient -s "keyword" "nr_processes"
```

### Shutdown Server (`-f`)
Safely shuts down the server, persisting current state and freeing resources.

```bash
./bin/dclient -f
```

## Architecture

- **Client-Server Communication**: via named pipes (`/tmp/serverChannel` and `/tmp/clientChannel<PID>`).
- **Metadata Storage**: binary files (`metadata.bin`, `freeQueue.bin`, `identifier.bin`) ensure data persistence.
- **Concurrency**: the server handles parallel keyword searches using `fork()` and limits active processes based on user input.
- **Cache**: in-memory cache with a fixed size (defined on server start) improves read performance, using the **Clock Algorithm** for replacement.
- **Free Queue**: stores reusable positions in metadata file to reduce fragmentation.
- **Garbage Collector**: compacts metadata storage periodically or when limits are reached.

## How to Run

1. **Compile the project**:
   ```bash
   make
   ```

2. **Generate documentation with Doxygen** (optional):
   ```bash
   make doc
   ```

3. **Start the server** (optionally pass cache size):
   ```bash
   ./bin/dserver dataset/ 36000
   ```
   
4. **Execute client commands** using the syntax shown in the Features section.

5. **Shut down the server** using:
   ```bash
   ./bin/dclient -f
   ```

## Persistence Files

- `metadata.bin`: stores all document metadata  
- `freeQueue.bin`: tracks reusable positions in metadata file  
- `identifier.bin`: tracks the next unique ID to assign  

These files are loaded at server start and saved at shutdown.

## Notes

- The system is designed for **UNIX-based environments** (e.g., Linux).
- All binaries are located in the `bin/` folder.
- Named pipes are created under `/tmp/`.
