=== Hardcoded Fixes Needed ===

./server/server-karaf/src/main/assembly/bin/start:       java -jar /opt/subutai-mng/keys/peer-key-generator-1.0-SNAPSHOT.jar $folder subutai`date +%s`@subutai.io
./server/server-karaf/src/main/assembly/etc/startup.properties:mvn\:io.subutai/subutai-appender/4.0.0-RC7-SNAPSHOT = 7
./server/server-karaf/src/main/assembly/etc/branding.properties:\u001B[1m  Subutai Management System\u001B[0m (4.0.0-RC7-SNAPSHOT)\r\n\
./server/core/identity-manager/blueprint-authz/README.md:install -s mvn:org.apache.aries.blueprint/org.apache.aries.blueprint.authz/1.0.0-SNAPSHOT
./shell-snippets/replace_subutai.sh:package="subutai-4.0.0-RC7-SNAPSHOT.tar.gz"
./shell-snippets/replace_subutai.sh:'deploy/webui-4.0.0-RC7-SNAPSHOT.war'

=== Release Preparation Problem ===

We also need to fix the final karaf-maven-plugin problem. Run the following
to see the release preparation fail in the last stage when the features file
is being generated.

```
mvn -DsubutaiRelease release:prepare
```

