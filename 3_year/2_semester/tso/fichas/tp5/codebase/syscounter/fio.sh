#!/bin/bash

echo "PID = $$"

echo "Press any key to continue^^."
read -n 1 -s key

fio --name=seq_write --ioengine=psync --rw=write --bs=4k \
--size=1g --directory=/mnt/fs
