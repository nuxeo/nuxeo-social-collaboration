#!/bin/sh -x
HERE=$(cd $(dirname $0); pwd -P)

# Load conf file if any
if [ -r $HERE/run.conf ]; then
    . $HERE/run.conf
fi

# Default values
HIDE_FF=${HIDE_FF:-}
SUITES=${SUITES:-"suite"}
URL=${URL:-http://localhost:8080/nuxeo/}

# Build command line
CMD="java -jar selenium-server.jar -port 14440 -timeout 7200 \
      -htmlSuite "*chrome" $URL "
if [ ! -z $HIDE_FF ]; then
    # we assume that there is a long running xvfb-run process running and
    # listening on display :1
    export DISPLAY=${DISPLAY:-":1"}
#    CMD="xvfb-run -a $CMD"
fi
CMD_END="-firefoxProfileTemplate ffprofile -userExtensions user-extensions.js"

check_ports_and_kill_ghost_process() {
    hostname=${1:-0.0.0.0}
    port=${2:-14440}
    RUNNING_PID=`lsof -n -i TCP@$hostname:$port | grep '(LISTEN)' | awk '{print $2}'`
    if [ ! -z $RUNNING_PID ]; then
        echo [WARN] A process is already using port $port: $RUNNING_PID
        echo [WARN] Storing jstack in $PWD/$RUNNING_PID.jstack then killing process
        [ -e $JAVA_HOME/bin/jstack ] && $JAVA_HOME/bin/jstack $RUNNING_PID >$PWD/$RUNNING_PID.jstack
        kill $RUNNING_PID || kill -9 $RUNNING_PID
        sleep 5
    fi
}

# Try to kill ghosts processses
check_ports_and_kill_ghost_process

# Clean old results
rm -f $HERE/result-*.html

cd $HERE
# Update path in user-extensions.js
sed "s,\(storedVars\['testfolderpath'\]\ \=\).*$,\1\ \"$HERE\";,g" < user-extensions.js.sample > user-extensions.js

# Update url in profile
sed "s,\(capability.principal.codebase.p0.id...\).*$,\1\"$URL\");,g" < ffprofile/prefs.js.sample > ffprofile/prefs.js

exit_code=0
# Launch suites
for suite in $SUITES; do
    echo "### [INFO] Running test suite $suite ..."
    $CMD "$PWD/tests/$suite.html" "$PWD/result-$suite.html" $CMD_END
    if [ $? != 0 ]; then
        echo "### [ERROR] $suite TEST FAILURE"
        exit_code=9
    else
        echo "### [INFO] $suite TEST SUCCESSFUL"
    fi
    # pause to prevent "Xvfb failed to start"
    sleep 5
done

if [ $exit_code != 0 ]; then
    echo "### [ERROR] TESTS FAILURE"
else
    echo "### [INFO] TESTS SUCCESSFUL"
fi

exit $exit_code
