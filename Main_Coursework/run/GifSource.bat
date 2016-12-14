@echo off
(CALL StartRMI.bat)
java -cp "Notification.jar" -XX:+UseG1GC -XX:MinHeapFreeRatio=30 -XX:MaxHeapFreeRatio=40 -Djava.rmi.server.useCodebaseOnly=false -Djava.rmi.server.codebase="https://www.huwcbjones.co.uk/Notification.jar" GifSource %*