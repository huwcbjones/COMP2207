#!/bin/bash
rmi=$(pgrep rmiregistry | wc -l)
if [ $rmi -ne 1 ]; then
	rmiregistry &
	echo "Started RMI Registry..."
fi
