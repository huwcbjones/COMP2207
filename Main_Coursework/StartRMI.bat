@echo off
(tasklist /nh /fi "imagename eq rmiregistry.exe" | find /i "rmiregistry.exe" > nul || (
	start /B rmiregistry.exe
))