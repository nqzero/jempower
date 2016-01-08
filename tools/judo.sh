#!/bin/bash

export cp=$1
shift 1
export cmd="$@"
export user=$(whoami)
myjudo=$(which __judo_helper.sh)

sudo -E bash -c "ulimit -n 102400; su $user -mc '$myjudo'"
