#!/bin/bash
./StartRMI.sh
java -cp "Notification.jar" -Djava.rmi.server.codebase=file:Notification.jar SourceProxy $*