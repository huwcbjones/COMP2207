#!/bin/bash
pushd .
cd "out/production/Main_Coursework"
../../../StartRMI.sh
popd
java -cp "out/production/Main_Coursework" \
	-Djava.rmi.server.codebase=file:/Users/huw/Documents/University/COMP2207/Main_Coursework/out/production/Main_Coursework/ \
	GifSource $*
