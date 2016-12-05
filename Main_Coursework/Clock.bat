@echo off
pushd %cd%
CD /D "E:\Documents\University\COMP2207\Main_Coursework\out\Production\Main_Coursework"
(CALL E:\Documents\University\COMP2207\Main_Coursework\StartRMI.bat)
popd %cd%
CD /D %cd%
java -cp "out\production\Main_Coursework" ^
	-Djava.rmi.server.codebase=file:/E:/Documents/University/COMP2207/Main_Coursework/out/production/Main_Coursework/ ^
	-Djava.security.policy=server.policy ^
	Clock %*