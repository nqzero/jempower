# jempower
java web server hello world examples with async, based loosely on the techempower plaintext benchmarks

## build and run the servers

to build and start all the servers, run `./build.sh` and then kill it to stop the servers
* downloads dependencies
* builds
* uses sudo to increase ulimit
* runs the server as the user

this is equivalent to:
```
for ii in kilim comsat jetty spark utow; do
  (cd $ii; mvn clean package dependency:copy-dependencies -DoutputDirectory=target)
done

QUASAR="-javaagent:$PWD/comsat/target/quasar-core-0.7.2-jdk8.jar"
PATH=$PWD/tools:$PATH


judo.sh  jetty/target          JettyTechem  &    # 9090
judo.sh  jetty/target          JettyAsync   &    # 9091
judo.sh  jetty/target          JettyAsync2  &    # 9092
judo.sh  kilim/target          KilimHello  1&    # 9093
judo.sh   utow/target          UtowTechem   &    # 9095
judo.sh comsat/target  $QUASAR ComsatJetty 1&    # 9096
judo.sh   utow/target          UtowAsync    &    # 9097
judo.sh   utow/target          UtowAsync2   &    # 9098
judo.sh  spark/target          SparkHello   &    # 9099

# kill all the servers
jkill.sh
```
ctrl-c will also kill a server if it's in the foreground


## testing with ApacheBench
chose a port from the list above, eg 9091 is JettyAsync

```
ulim.sh ab -r -k -c 20000 -n 1000000 localhost:9091/hello
```

apache bench is able to reach a concurrency level of 20000 which is sufficient to get a decent view of the servers
(at least on low end hardware).
some of the synchronous servers are able to saturate ab (use a 2nd instance)

multiple runs are required for each server to allow the JIT to warm up



## build/run each module individually
there are modules for several different servers.
each module has it's own pom.xml and all the examples are in the default package for easy running.
eg, 
```
cd jetty
mvn clean package dependency:copy-dependencies -DoutputDirectory=target
java -cp "target/*" JettyAsync
```

##### run comsat
comsat requires a javaagent to modify the bytecode
`java -cp "target/*" -javaagent:target/quasar-core-0.7.2-jdk8.jar ComsatJetty`


## "ulimit -n" tools
the tools directory is useful for starting, stopping and testing the servers at high concurrency.
by default, ubuntu makes setting "ulimit -n" hard.
add it to your path, then:
```
judo.sh   jetty/target          JettyAsync    &
ulim.sh ab -r -k -c 4000 -n 1000000 localhost:9091/hello
jkill.sh
```

command | description
-------|-------
`judo.sh` | ulimit -n wrapper for java $1/* $@
`jkill.sh` | kill -TERM all the servers started with `judo.sh`
`ulim.sh` | ulimit -n wrapper for arbitrary commands (need to escape globs)

NB: `jkill.sh` will kill the process group associated with 'judo.sh'.
i'm not aware of any other user of this name, but it's possible. buyer beware

to see the actual ulimit that these scripts are able to achieve, run `ulim.sh ulimit -Sn`

##### other stuff
there are challenges of running high concurrency on linux, including file descriptors and ports.
in the tools directory there are some shell scripts that simplify bypassing ulimits, eg judo.sh above

here are some examples that might be useful
```
sysctl net.ipv4.ip_local_port_range ="1024 65535"
```
and more help here: http://gwan.com/en_apachebench_httperf.html



## the servers

for undertow and jetty, there are 2 flavors of async
* UtowAsync and JettyAsync use a single timer to periodically sweep thru the requests and complete them
* the Async2 variants use multiple timers and a queue
* values were somewhat tuned on an i3-2105
* performance was somewhat better for the Async2 variants

kilim and quasar both accept a delay argument (msec)
* a short delay (1-10 msec) is equivalent to the other Async servers
* they both use fibers to provide async handlers
* kilim also provides async I/O (servlet 3.1 provides a mechanism but it's a pain to work with)
* none of the other servers use async I/O

other servers:
* UtowBayou (port 9094, synchronous)
from https://github.com/zhong-j-yu/latency-diff/blob/master/src/undertow/HelloWorld.java


