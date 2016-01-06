#!/bin/bash

cp=$1
shift 1
cmd="$@"
user=$(whoami)


sudo bash -c "ulimit -n 102400; su $user -mc '$JAVA_HOME/bin/java -cp $cp/\* -Xmx1G $cmd > /dev/null 2>&1'"
