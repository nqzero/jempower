#!/bin/bash

: ${output:="/dev/null"}

$JAVA_HOME/bin/java -cp "$cp" -Xmx1G $cmd > "$output" 2>&1



