
Global configuration properties

Standard configuration:

src/main/resources
	cassandra.yaml
	hazelcast.xml
	log4j.properties

Test configuration:

src/test/resources
	cassandra.yaml
	hazelcast.xml
	log4j.properties

Important configuration options in custom.properties:

cassandra.use_remote
cassandra.local.url
cassandra.remote.url

Make sure you're only running with cassandra.use_remote=false when testing and
that you're running with cassandra.use_remote=true when running in production.

You can override the properties by having a file named
custom.properties in the classpath. This will get loaded after the
custom.properties file is loaded and will override any values with the same
names. This makes it easier to keep confidential credentials out of source
code version control.

The custom properties file location can be overriden at build-time by passing
the file location as a property parameter to maven.  For example:

mvn clean install -Dcustom-spring-properties=classpath:/custom.properties

