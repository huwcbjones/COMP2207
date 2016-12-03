@echo off
(CALL StartRMI.bat)
java -cp "out\production\Main_Coursework" ^
	-Djava.rmi.server.codebase=file:/C:/Users/Huw/Documents/University/COMP2207/Main_Coursework/out/Production/Main_Coursework/ ^
	-Djava.rmi.server.hostname=localhost ^
	-Djava.security.policy=server.policy ^
	Clock %*