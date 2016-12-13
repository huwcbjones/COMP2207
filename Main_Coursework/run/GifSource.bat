@echo off
(CALL StartRMI.bat)
java -cp "Notification.jar" -Djava.rmi.server.useCodebaseOnly=false -Djava.rmi.server.codebase="https://www.huwcbjones.co.uk/Notification.jar" GifSource %*