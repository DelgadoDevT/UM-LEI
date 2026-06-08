#!/bin/bash

USE_SYNC=${1:-"nosync"}
ITERATIONS=${2:-1}
MOUNT_POINT="/mnt/dedup_fs"
DEDUP_DATA_DIR="/home/tso/TSO_DEDUP_DATA/w1_dedup"
LOG_DIR="scripts/test_logs"
RESULTS_CSV="scripts/results_w1.csv"
PASSTHROUGH_EXEC="scripts/passthrough/passthrough"

mkdir -p "$LOG_DIR" "$DEDUP_DATA_DIR"
[ ! -f "$RESULTS_CSV" ] && echo "Iteration,Filesystem,Scenario,DedupRatio,WriteSpeed_MiBs,ReadSpeed_MiBs" > "$RESULTS_CSV"

make || exit 1; make -C scripts/passthrough || exit 1

# Limpeza preventiva caso testes anteriores tenham falhado
sudo fusermount3 -uz "$MOUNT_POINT" 2>/dev/null
sudo pkill -9 "dedup_fs" 2>/dev/null
sudo pkill -9 "passthrough" 2>/dev/null

run_test_scenario() {
    local fs_type=$1; local fs_exec=$2; local scenario_name=$3; local fio_command=$4; local root_dir=$5; local iter=$6
    echo " -> Running $fs_type..."
    sudo mkdir -p "$MOUNT_POINT"; mkdir -p "$root_dir"

    # A sub-shell ( ... ) impede o bash de imprimir "Killed" quando matamos o processo
    if [ "$fs_type" == "dedup_fs" ]; then
        ( sudo "$fs_exec" "$MOUNT_POINT" --root_dir "$root_dir" -f -o allow_other > "$LOG_DIR/${fs_type}_${scenario_name}_iter${iter}_fs.log" 2>&1 & )
    else
        ( sudo "$fs_exec" "$MOUNT_POINT" -f -o allow_other > "$LOG_DIR/${fs_type}_${scenario_name}_iter${iter}_fs.log" 2>&1 & )
    fi

    local retry=0
    while ! mountpoint -q "$MOUNT_POINT" || [ ! -w "$MOUNT_POINT" ]; do
        sleep 1
        retry=$((retry + 1))
        [ $retry -gt 10 ] && { echo "    [!] Error: Mount failed"; return 1; }
    done
    sleep 1

    if [ "$fs_type" == "dedup_fs" ]; then
        ( sudo ./phys_io_counter > "$LOG_DIR/${fs_type}_${scenario_name}_iter${iter}_ebpf.log" 2>&1 & )
    fi

    eval "$fio_command --output=$LOG_DIR/${fs_type}_${scenario_name}_iter${iter}_fio.log"

    if [ "$USE_SYNC" == "sync" ]; then sync; sleep 2; fi
    
    if [ "$fs_type" == "dedup_fs" ]; then
        sudo pkill -SIGINT "phys_io_counter" 2>/dev/null
        sleep 1
    fi

    DEDUP_RATIO=""
    if [ "$fs_type" == "dedup_fs" ] && [ -f "$MOUNT_POINT/stats.txt" ]; then
        DEDUP_RATIO=$(cat "$MOUNT_POINT/stats.txt" | grep "Dedup Ratio:" | awk '{print $3}' | sed 's/x//')
    fi
    
    WRITE_VAL=$(grep -i "write:" "$LOG_DIR/${fs_type}_${scenario_name}_iter${iter}_fio.log" | grep -ioP "(?<=bw=)[0-9.]+" | head -1)
    echo "$iter,$fs_type,$scenario_name,$DEDUP_RATIO,$WRITE_VAL," >> "$RESULTS_CSV"

    sudo fusermount3 -uz "$MOUNT_POINT" 2>/dev/null
    sleep 1
    
    # pkill sem o '-f' afeta APENAS os binários exatos, protegendo o script de se matar a ele próprio
    sudo pkill -9 "dedup_fs" 2>/dev/null
    sudo pkill -9 "passthrough" 2>/dev/null
    sudo pkill -9 "phys_io_counter" 2>/dev/null
    sudo pkill -9 "fio" 2>/dev/null
    
    sudo bash -c 'echo 3 > /proc/sys/vm/drop_caches'
    sleep 1
    sudo rm -rf "$root_dir"
}

FIO_CMD_W1="fio --name=random-write --ioengine=libaio --rw=write --bs=4k --size=512M --filename=$MOUNT_POINT/random_file.dat --refill_buffers"

for ((i=1; i<=ITERATIONS; i++)); do
    echo "=== Workload 1 | Iter $i/$ITERATIONS ==="
    run_test_scenario "dedup_fs" "./dedup_fs" "W1" "$FIO_CMD_W1" "$DEDUP_DATA_DIR" "$i"
    run_test_scenario "passthrough_fs" "$PASSTHROUGH_EXEC" "W1" "$FIO_CMD_W1" "/home/tso/TSO_DEDUP_DATA/pt_w1" "$i"
done
