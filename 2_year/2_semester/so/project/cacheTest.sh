#!/bin/bash

if [ -z "$1" ]; then
    echo "Error "
    echo "Use: $0 <repetition number>"
    exit 1
fi
output_file="cacheTests.txt"
> "$output_file"
repetition=$1
first_run=true

./bin/dserver dataset/ 1 &

sleep 1

for ((i = 0 ; i < 30 ; i++)); do
    ./addGdatasetMetadata.sh dataset/Gcatalog.tsv 
done

./bin/dclient -f

for i in $(seq 1000 5000 51000); do
    echo "=== Executing server with cache size -> $i ==="

    total_time=0

    ./bin/dserver dataset/ $i &
    SERVER_PID=$!
    sleep 2

    total_time=0

    for j in $(seq 1 $repetition); do
        random_number=$(( RANDOM % 50000 + 1 ))

        result=$( { time ./bin/dclient -l $random_number "romeo"; } 2>&1 )
        
        exec_time=$(echo "$result" | grep real | sed 's/real\t//')
        
        if [[ $exec_time =~ ([0-9]+)m([0-9]+\.[0-9]+)s ]]; then
            minutes=${BASH_REMATCH[1]}
            seconds=${BASH_REMATCH[2]}
            
        total_seconds=$(echo "scale=6; $minutes * 60 + $seconds" | bc)
        
        total_time=$(echo "scale=6; $total_time + $total_seconds" | bc)
            
        else
            echo "Error extracting time"
        fi
    done



    average=$(echo "scale=6; $total_time / $repetition" | bc)
    echo "" >> "$output_file"
    echo "=== Average time after $repetition executions: $average seconds ===" >> "$output_file"

    ./bin/dclient -f
    wait $SERVER_PID 2>/dev/null

    echo "=== Iteration $i finished ===" >> "$output_file"
    echo "" >> "$output_file"
done