#!/bin/bash
# run the commands wrapped in a ulimit

cmd="$@"

user=$(whoami)

sudo bash -c "ulimit -n 102400; su $user -mc '$cmd'"
