echo "Test ---> how number of processes changes the time to search for identifiers in documents"

if [ -z "$1" ]; then
    echo "Error "
    echo "Use: $0 <repetition number> <processes_number>"
    exit 1
fi

if [ -z "$2" ]; then
    echo "Error "
    echo "Use: $0 <repetition number> <processes_number>"
    exit 1
fi

repetition=$1
processes=$2

for i in $(seq 1 "$processes"); do
    echo "=== Executing query with $i processes ==="

    total_time=0

    for j in $(seq 1 $repetition); do

        result=$( { time ./bin/dclient -s "abcdefghijklmnopqrstuvwxyz" $i; } 2>&1 )
        
        exec_time=$(echo "$result" | grep real | sed 's/real\t//')
        
        if [[ $exec_time =~ ([0-9]+)m([0-9]+\.[0-9]+)s ]]; then
            minutes=${BASH_REMATCH[1]}
            seconds=${BASH_REMATCH[2]}
            
        total_seconds=$(echo "scale=6; $minutes * 60 + $seconds" | bc)
        
        total_time=$(echo "scale=6; $total_time + $total_seconds" | bc)
            
        else
            echo "Error extracting time."
        fi
    done

    average=$(echo "scale=6; $total_time / $repetition" | bc)
    echo ""
    echo "=== Average time after $repetition executions: $average seconds ==="


    echo "=== Iteration $i finished ==="
    echo ""
done


