#!/bin/bash

# run a java based server
#   first arg: classpath, which gets a "/*" appended
#   everything else: passed to java
# defaults -Xmx1G
# output is sent to $output, defaults to /dev/null
# wrap the server in ulimit -Hn
# if killed, cleans up (kills) the subprocesses
# requires sudo - will exit if a password is required and it's in the background



if [ $# -eq 0 ]; then
    echo 'run the command "java -cp $1/\* ${@:2}" using sudo/su to set max ulimit'
    echo 'std out/err are redirected to $output (/dev/null if not provided)'
elif [ $# -lt 2 ]; then
   echo "at least 2 arguments required, exiting"
   exit
fi


export cp="$1/\*"
shift 1
export args="$@"
export user=$(whoami)





: ${output:="/dev/null"}

cmd="$JAVA_HOME/bin/java -cp $cp -Xmx1G $args > $output 2>&1"


sudo -n true 2> /dev/null
req=$?









msg="appear to be running in the background and need sudo password ... cowardly exiting without running commands"

if [ $req -ne 0 ]; then
    trap "echo '$msg'; exit 1" SIGTTIN
    read -t 0.1 dummyValue
    trap SIGTTIN

    # check if terminal is connected to stdin/out
    if [[ -t 0 && -t 1 || -p /dev/stdin && -p /dev/stdout ]]; then
	sudo true || exit 1
    else
	echo "sudo password required and terminal not connected to stdin/out ... cowardly exiting without running commands"
	exit 1
    fi
fi

lim=102400

sudo bash -c "
  ulimit -Hn $lim;
  su $user -mc 'ulimit -Sn \$(ulimit -Hn); exec $cmd'
" &

pid1=$!


trap "
echo 'script killed: cleaning up subprocesses'
pid2=\$(ps --ppid $pid1 -o pid=)
pid3=\$(ps --ppid \$pid2 -o pid=)
kill \$pid3
trap - SIGINT SIGTERM EXIT
exit 0
" SIGINT SIGTERM EXIT

wait $pid1

trap - SIGINT SIGTERM EXIT









