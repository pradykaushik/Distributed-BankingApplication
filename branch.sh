#!/bin/bash +vx
LIB_PATH=$"lib/commons-io-2.4.jar:lib/libthrift-0.9.2.jar:lib/slf4j-simple-1.7.12.jar:lib/slf4j-api-1.7.12.jar"
#port
java -classpath bin:$LIB_PATH BranchServer $1 $2
