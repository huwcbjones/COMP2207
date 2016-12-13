#!/bin/bash
rmi=$(pgrep rmiregistry | wc -l)
if [ $rmi -ne 1 ]; then
	rmiregistry -J-Djava.rmi.server.useCodebaseOnly=false &
	echo "Started RMI Registry..."
fi
