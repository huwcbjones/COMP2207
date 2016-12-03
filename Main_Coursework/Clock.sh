#!/bin/bash
./StartRMI.sh
java -cp "out/production/Main_Coursework" \
	-Djava.security.manager \
	-Djava.security.policy=server.policy \
	-Djava.rmi.codebase=file:/Users/huw/Documents/University/COMP2207/Main_Coursework/out/production/Main_Coursework/ \
	-Djava.rmi.server.codebase=file:/Users/huw/Documents/University/COMP2207/Main_Coursework/out/production/Main_Coursework/ \
	Clock $*
