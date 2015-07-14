## Performance Test Suite for JMX Performance

This is a simple Webapplication which exposes a Jolokia agent under
context root `/jolokia/` and registers an MBean for performing
performance measurements on the JMX subsystem

### Available Test

All test are realized as operations on the MBean
`jolokia:type=Performance,name=PerformanceTest"`:

* `testReadAllAttributes()` : Query all MBean and read all their
  attributes (the same way as
  [jmx_exporter](https://github.com/prometheus/jmx_exporter) does it
  currently). This will export the following metrics via these MBeans:
  
  - `metrics:name=jmx.attributes.time.mbean` Time stats for reading
    all attributes of a specific MBean
  - `metrics:name=jmx.attributes.time.query` Time stats for querying
    all MBeans
  - `metrics:name=jmx.attributes.time.read` Time stats for reading all
    attributes of all MBeans
  - `metrics:name=jmx.attributes.time.total` Total time stats

You can access these MBeans easily wither with jconsole or with an
Jolokia client like [hawtio](http://hawt.io) or
[j4psh](http://www.jmx4perl.org). 

### Testing it with Wildfly 8.2.0 Final

In order to easily run the test with a real world server, a setup with
the official Wildfly Docker image has been provided. To start up the
docker container:

    mvn -Pwildfly install docker:build docker:start -Ddocker.follow

You need Docker installed and the environment variable Docker Host
must be set. This command will start a container which exposes port
8080 on the Docker host (`localhost` or, if using boot2docker or so, the
IP of the boot2docker VM)

