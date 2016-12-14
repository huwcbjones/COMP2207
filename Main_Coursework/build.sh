#!/bin/bash
if [ -d "out" ]; then
	rm -rf out/*;
else
	mkdir out;
fi;

javac -d "out" -cp "src" -Xlint:unchecked src/*.java
jar cvf out/Notification.jar -c  src/META-INF/MANIFEST.MF -C out .
mv out/Notification.jar run/Notification.jar