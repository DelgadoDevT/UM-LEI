#!/bin/bash

echo "PID = $$"

echo "Press any key to continue^^."
read -n 1 -s key

fio --rw=write --bs=4k --size=1g --ioengine=sync --name=seqwr-bs4k-s1g --direct=1
