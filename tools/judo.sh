#!/bin/bash

# run a java based server
#   first arg: classpath, which gets a "/*" appended
#   everything else: passed to java
# defaults -Xmx1G
# output is sent to $output, defaults to /dev/null
# wrap the server in ulimit -n

export cp="$1/*"
shift 1
export cmd="$@"
export user=$(whoami)

# use a helper script to make it easier to find/kill running processes
myjudo=$(which __judo_helper.sh)

sudo -E bash -c "ulimit -n 102400; su $user -mc '$myjudo'"
