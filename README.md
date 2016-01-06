# jempower
java web server hello world examples with async, based loosely on the techempower plaintext benchmarks

##### build/run each module
there are modules for several different servers.
each module has it's own pom.xml and all the examples are in the default package for easy running.
eg, 
```
cd utow
mvn clean package dependency:copy-dependencies -DoutputDirectory=target
java -cp "target/*" UtowAsync
```

##### run comsat
comsat requires a javaagent to modify the bytecode
`java -cp "target/*" -javaagent:target/quasar-core-0.7.2-jdk8.jar ComsatJetty`


##### everything
````
for ii in comsat jetty spark utow; do
  (cd $ii; mvn clean package dependency:copy-dependencies -DoutputDirectory=target)
done

QUASAR="-javaagent:comsat/target/quasar-core-0.7.2-jdk8.jar"
PATH=$PWD/tools:$PATH


judo.sh  spark/target          SparkHello   &    # 4567
judo.sh  jetty/target          JettyTechem  &    # 9090
judo.sh  jetty/target          JettyAsync   &    # 9091
judo.sh   utow/target          UtowTechem   &    # 9095
judo.sh comsat/target  $QUASAR ComsatJetty  &    # 9096
judo.sh   utow/target          UtowAsync    &    # 9097
judo.sh   utow/target          UtowAsync2   &    # 9098
```


##### other stuff
there are challenges of running high concurrency on linux, including file descriptors and ports.
in the tools directory there are some shell scripts that simplify bypassing ulimits, eg judo.sh above

here are some examples that might help
```
sysctl net.ipv4.ip_local_port_range ="1024 65535"
# to run manually with ulimit
sudo bash -c "ulimit -n 102400; su $USER -mc '$JAVA_HOME/bin/java -cp utow/target/\* -Xmx1G UtowAsync2'"
sudo bash -c "ulimit -n 102400; su $USER -mc 'ab -r -k -c 4000 -n 500000 localhost:9098/hello'"
```
and more help here: http://gwan.com/en_apachebench_httperf.html

jempower.nb is a netbeans project just to simplify editing the files.
prolly shouldn't have been added, but at this point, easier to just leave it be


##### todo
kilim is another fiber library which includes a web server - need to add a script for building KilimHello


