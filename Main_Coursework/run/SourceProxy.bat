@echo off
(CALL StartRMI.bat)
java -cp "Notification.jar" -Djava.rmi.server.codebase="http://huwjones.me/Notification.jar" SourceProxy %*