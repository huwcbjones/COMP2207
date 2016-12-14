#!/bin/bash
./StartRMI.sh
java -cp "Notification.jar" -XX:+UseG1GC -XX:MinHeapFreeRatio=40 -XX:MaxHeapFreeRatio=50 -Djava.rmi.server.useCodebaseOnly=false -Djava.rmi.server.codebase="https://www.huwcbjones.co.uk/Notification.jar" SourceProxy $*