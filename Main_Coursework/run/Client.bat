@echo off
java -cp "Notification.jar" -XX:+UseG1GC -XX:MinHeapFreeRatio=30 -XX:MaxHeapFreeRatio=40 Client %*