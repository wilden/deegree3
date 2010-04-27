#!/bin/sh

DIR=`dirname $0`

#export LD_LIBRARY_PATH=/home/schneider/workspace/joglbeef/lib/linux-amd64
#export JAVA_OPTS="-Xmx1000M -XX:MaxDirectMemorySize=500M"

java $JAVA_OPTS -cp $DIR/deegree-tools-3.0-pre4.jar:. org.deegree.tools.ToolBox "$@"
