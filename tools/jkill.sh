#!/bin/bash

# used to use signal -2 (ie SIGINT) to simulate ctrl-c from the terminal
# however, on one machine, this triggered a logout so switched to SIGTERM
# i3-2105, ubuntu 16.04, lightdm/Xorg/upstart/unity, bash shell
# googled, but didn't find anything approaching an explanation

# only uses first 15 characters ...
pids=$(pgrep -x judo.sh)

for ii in $pids; do kill -TERM $ii; done


