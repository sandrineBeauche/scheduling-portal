#!/bin/sh

CLASSPATH=.
workingDir=`dirname $0`
. $workingDir/env.sh log4j-client

$JAVACMD org.ow2.proactive.scheduler.util.adminconsole.AdminController $@


echo