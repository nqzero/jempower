#! /bin/bash

root=$(dirname $_)
base=$(cd $root; pwd)


if [ -n "$MAVEN_HOME" ]; then
    echo "adding MAVEN_HOME to path: $MAVEN_HOME/bin"
    PATH="$MAVEN_HOME/bin:$PATH"
fi


tech="kilim comsat jetty spark utow"
for ii in $tech; do
    (
	cd $ii;
	mvn -q clean package dependency:copy-dependencies -DoutputDirectory=target
    )
    ret=$?
    if [ $ret -ne 0 ]; then
	echo "build failed for package: $ii"
	exit 1
    fi
done


PATH=$base/tools:$PATH
QUASAR="-javaagent:$(ls $base/comsat/target/quasar-core-*.jar)"


# make sure sudo works
sudo true || exit



# start all the servers
cd $root

judo.sh  jetty/target          JettyTechem  &    # 9090
judo.sh  jetty/target          JettyAsync   &    # 9091
judo.sh  jetty/target          JettyAsync2  &    # 9092
judo.sh  kilim/target          KilimHello  1&    # 9093
judo.sh   utow/target          UtowTechem   &    # 9095
judo.sh comsat/target  $QUASAR ComsatJetty 1&    # 9096
judo.sh   utow/target          UtowAsync    &    # 9097
judo.sh   utow/target          UtowAsync2   &    # 9098
judo.sh  spark/target          SparkHello   &    # 9099


for ii in {1..9}; do
    wait -n
    echo "server shutdown unexpectedly ..."
done
