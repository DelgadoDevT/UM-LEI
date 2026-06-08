#!/bin/bash

# Configuration
MOUNT_POINT="/mnt/dedup_fs"
DEDUP_DATA_DIR="/home/tso/TSO_DEDUP_DATA/prof"
SCRIPT_DIR="scripts"

# Ensure script directory exists
mkdir -p "$SCRIPT_DIR"

echo "=== 1. Cleaning up previous tests ==="
sudo fusermount3 -uz "$MOUNT_POINT" 2>/dev/null
sudo pkill -9 "dedup_fs" 2>/dev/null
sudo pkill -9 "fio" 2>/dev/null
sudo rm -rf "$DEDUP_DATA_DIR"
sudo mkdir -p "$MOUNT_POINT"
mkdir -p "$DEDUP_DATA_DIR"

echo "=== 2. Starting dedup_fs ==="
sudo ./dedup_fs "$MOUNT_POINT" --root_dir "$DEDUP_DATA_DIR" -f -o allow_other &
disown
sleep 2

# Get the exact PID of the dedup_fs executable (ignoring sudo)
FS_PID=$(pgrep -x dedup_fs)
if [ -z "$FS_PID" ]; then
    echo "[!] Error: dedup_fs is not running."
    exit 1
fi
echo " -> Found dedup_fs PID: $FS_PID"

echo "=== 3. Starting FIO (CPU Workload) ==="
sudo fio --name=profiling --ioengine=sync --rw=write --bs=4k --size=1G --filename="$MOUNT_POINT/prof_file.dat" --runtime=45 --time_based > /dev/null 2>&1 &
disown
sleep 2

echo "=== 4. Recording Flamegraph (Please wait 30 seconds...) ==="
# perf record generates 'perf.data' in the current directory by default
sudo perf record -F 99 -p $FS_PID -g -- sleep 30

echo "=== 5. Generating exportable file in scripts/ directory ==="
# We pipe the script output directly to the scripts folder
sudo perf script > "$SCRIPT_DIR/flamegraph_final.perf"
echo " -> File '$SCRIPT_DIR/flamegraph_final.perf' created successfully!"

echo "=== 6. Final cleanup ==="
sudo pkill -9 "fio" 2>/dev/null
sudo fusermount3 -uz "$MOUNT_POINT" 2>/dev/null
sudo pkill -9 "dedup_fs" 2>/dev/null
sudo rm -rf "$DEDUP_DATA_DIR"
sudo rm -f perf.data perf.data.old

echo "Done! You can now find the report in $SCRIPT_DIR/flamegraph_final.perf"
