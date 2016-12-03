@echo off
(tasklist /nh /fi "imagename eq rmiregistry.exe" | find /i "rmiregistry.exe" > nul || (CALL StartRMI.bat))
java -cp "out\production\Main_Coursework" -Djava.rmi.server.codebase=file:/E:/Documents/University/COMP2207/Main_Coursework/out/Production/Main_Coursework/ Clock %*