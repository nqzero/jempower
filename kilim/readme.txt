# to build and test kilim with java8

svn co https://github.com/kilim/kilim/branches/asm5-jdk8 lib

cd lib;
ant weave jar
mkdir c2
find examples bench -name "*.java" | xargs javac -cp kilim.jar:libs/\* -d ./c2
java -cp c2:kilim.jar:libs/\* kilim.tools.Weaver -d c2 c2
java -cp c2:kilim.jar:libs/\* kilim.examples.SimpleTask
java -cp c2:kilim.jar:libs/\* kilim.bench.LotsOfTasks -ntasks 300000



