#!/bin/bash

USE_SYNC=${1:-"nosync"}
ITERATIONS=${2:-1}
MOUNT_POINT="/mnt/dedup_fs"
DEDUP_DATA_DIR="/home/tso/TSO_DEDUP_DATA/w2_dedup"
LOG_DIR="scripts/test_logs"
RESULTS_CSV="scripts/results_w2.csv"
PASSTHROUGH_EXEC="scripts/passthrough/passthrough"

mkdir -p "$LOG_DIR" "$DEDUP_DATA_DIR"
[ ! -f "$RESULTS_CSV" ] && echo "Iteration,Filesystem,Scenario,DedupRatio,WriteSpeed_MiBs,ReadSpeed_MiBs" > "$RESULTS_CSV"

make || exit 1; make -C scripts/passthrough || exit 1

sudo fusermount3 -uz "$MOUNT_POINT" 2>/dev/null
sudo pkill -9 "dedup_fs" 2>/dev/null
sudo pkill -9 "passthrough" 2>/dev/null

FIO_CMD_W2_CREATE="fio --name=create-source --ioengine=libaio --rw=write --bs=4k --size=128M --filename=$MOUNT_POINT/source_file.dat --refill_buffers"
FIO_CMD_W2_READ_ALL="fio --name=read-all --ioengine=libaio --rw=read --bs=4k --size=128M --filename=$MOUNT_POINT/source_file.dat:$MOUNT_POINT/copy1.dat:$MOUNT_POINT/copy2.dat:$MOUNT_POINT/copy3.dat"

run_workload2() {
    local fs_type=$1; local fs_exec=$2; local root_dir=$3; local iter=$4
    echo " -> Running $fs_type..."
    sudo mkdir -p "$MOUNT_POINT"; mkdir -p "$root_dir"

    if [ "$fs_type" == "dedup_fs" ]; then
        ( sudo "$fs_exec" "$MOUNT_POINT" --root_dir "$root_dir" -f -o allow_other > "$LOG_DIR/${fs_type}_W2_iter${iter}_fs.log" 2>&1 & )
    else
        ( sudo "$fs_exec" "$MOUNT_POINT" -f -o allow_other > "$LOG_DIR/${fs_type}_W2_iter${iter}_fs.log" 2>&1 & )
    fi

    local retry=0
    while ! mountpoint -q "$MOUNT_POINT" || [ ! -w "$MOUNT_POINT" ]; do
        sleep 1
        retry=$((retry + 1))
        [ $retry -gt 10 ] && { echo "    [!] Error: Mount failed"; return 1; }
    done
    sleep 1

    if [ "$fs_type" == "dedup_fs" ]; then
        ( sudo ./phys_io_counter > "$LOG_DIR/${fs_type}_W2_iter${iter}_ebpf.log" 2>&1 & )
    fi

    eval "$FIO_CMD_W2_CREATE --output=$LOG_DIR/${fs_type}_W2_iter${iter}_create.log"
    cp "$MOUNT_POINT/source_file.dat" "$MOUNT_POINT/copy1.dat"
    cp "$MOUNT_POINT/source_file.dat" "$MOUNT_POINT/copy2.dat"
    cp "$MOUNT_POINT/source_file.dat" "$MOUNT_POINT/copy3.dat"
    eval "$FIO_CMD_W2_READ_ALL --output=$LOG_DIR/${fs_type}_W2_iter${iter}_read.log"

    if [ "$USE_SYNC" == "sync" ]; then sync; sleep 2; fi
    
    if [ "$fs_type" == "dedup_fs" ]; then
        sudo pkill -SIGINT "phys_io_counter" 2>/dev/null
        sleep 1
    fi

    DEDUP_RATIO=""
    if [ "$fs_type" == "dedup_fs" ] && [ -f "$MOUNT_POINT/stats.txt" ]; then
        DEDUP_RATIO=$(cat "$MOUNT_POINT/stats.txt" | grep "Dedup Ratio:" | awk '{print $3}' | sed 's/x//')
    fi
    
    WRITE_VAL=$(grep -i "write:" "$LOG_DIR/${fs_type}_W2_iter${iter}_create.log" | grep -ioP "(?<=bw=)[0-9.]+" | head -1)
    READ_VAL=$(grep -i "read:" "$LOG_DIR/${fs_type}_W2_iter${iter}_read.log" | grep -ioP "(?<=bw=)[0-9.]+" | head -1)
    echo "$iter,$fs_type,W2,$DEDUP_RATIO,$WRITE_VAL,$READ_VAL" >> "$RESULTS_CSV"

    sudo fusermount3 -uz "$MOUNT_POINT" 2>/dev/null
    sleep 1
    
    sudo pkill -9 "dedup_fs" 2>/dev/null
    sudo pkill -9 "passthrough" 2>/dev/null
    sudo pkill -9 "phys_io_counter" 2>/dev/null
    sudo pkill -9 "fio" 2>/dev/null
    
    sudo bash -c 'echo 3 > /proc/sys/vm/drop_caches'
    sleep 1
    sudo rm -rf "$root_dir"
}

for ((i=1; i<=ITERATIONS; i++)); do
    echo "=== Workload 2 | Iter $i/$ITERATIONS ==="
    run_workload2 "dedup_fs" "./dedup_fs" "$DEDUP_DATA_DIR" "$i"
    run_workload2 "passthrough_fs" "$PASSTHROUGH_EXEC" "/home/tso/TSO_DEDUP_DATA/pt_w2" "$i"
done
