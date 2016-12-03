@echo off
pushd %cd%
CD /D "C:\Program Files\Java\jdk1.8.0_101\bin"
start /B rmiregistry.exe
popd %cd%
CD /D %cd%