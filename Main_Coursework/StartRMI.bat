@echo off
(tasklist /nh /fi "imagename eq rmiregistry.exe" | find /i "rmiregistry.exe" > nul || (
	pushd %cd%
	CD /D "C:\Program Files\Java\jdk1.8.0_101\bin"
	start /B rmiregistry.exe
	popd %cd%
	CD /D %cd%
))