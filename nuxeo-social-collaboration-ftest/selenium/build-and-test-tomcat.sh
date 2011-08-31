#!/bin/bash -x

HERE=$(cd $(dirname $0); pwd -P)

# Retrieve Nuxeo Distribution and selenium-server.jar
(cd .. && mvn clean dependency:copy -P$MAVEN_PROFILES) || exit 1

# Start Tomcat
cd ../target
unzip nuxeo-social-collaboration-distribution-*.zip || exit 1
mv nuxeo-social-collaboration-*-tomcat tomcat || exit 1
chmod +x tomcat/bin/nuxeoctl || exit 1

tomcat/bin/nuxeoctl start || exit 1

# Run selenium tests
cd $HERE
./run.sh
ret1=$?

# Stop Tomcat
(cd ../target && tomcat/bin/nuxeoctl stop) || exit 1

# Exit if some tests failed
[ $ret1 -eq 0 ] || exit 9
