@echo off
IF EXIST out (
	RD /S /Q out
	MKDIR out\
) else (
	MKDIR out\
)


:BUILD
javac -d "out" -cp "src" -Xlint:unchecked src/*.java
jar cvf out\Notification.jar -c  src\META-INF\MANIFEST.MF -C out .
MOVE out\Notification.jar run\Notification.jar