#!/bin/bash
# run the commands wrapped in a ulimit

cmd="$@"

user=$(whoami)

lim=102400
sudo bash -c "ulimit -Hn $lim; su $user -mc 'ulimit -Sn \$(ulimit -Hn); $cmd'"
