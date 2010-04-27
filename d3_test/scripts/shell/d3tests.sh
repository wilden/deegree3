#!/bin/sh

OLDIR=`pwd`
cd `dirname $0`

java -cp deegree-test-3.0-pre4.jar:. org.deegree.test.TestingToolBox "$@"
cd $OLDIR
