#!/bin/bash

[ -d lib ]             || svn co https://github.com/kilim/kilim/branches/asm5-jdk8 lib
[ -a lib/kilim.jar ]   || (cd lib; ant clean weave jar)

mkdir -p target
$JAVA_HOME/bin/javac -cp lib/\* -d target src/KilimHello.java 
java -cp target:lib/\* kilim.tools.Weaver -d target target
java -cp target:lib/\* tools.KilimHello




