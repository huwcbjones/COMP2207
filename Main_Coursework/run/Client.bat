@echo off
java -cp "Notification.jar" -XX:+UseG1GC -XX:MinHeapFreeRatio=40 -XX:MaxHeapFreeRatio=50 Client %*