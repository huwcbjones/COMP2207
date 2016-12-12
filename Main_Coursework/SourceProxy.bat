@echo off
pushd %cd%
CD /D "out\Production\Main_Coursework"
(CALL ..\..\..\StartRMI.bat)
popd %cd%
CD /D %cd%
java -cp "out\production\Main_Coursework" ^
	-Djava.rmi.server.codebase=file:out/production/Main_Coursework/ ^
	-Djava.security.policy=server.policy ^
	SourceProxy %*