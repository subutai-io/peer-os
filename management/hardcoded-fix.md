=== Hardcoded Fixes Needed ===

./server/server-karaf/src/main/assembly/bin/start:       java -jar /opt/subutai-mng/keys/peer-key-generator-1.0-SNAPSHOT.jar $folder subutai`date +%s`@subutai.io


=== Release Preparation Problem ===

We also need to fix the final karaf-maven-plugin problem. Run the following
to see the release preparation fail in the last stage when the features file
is being generated.

```
mvn -DsubutaiRelease release:prepare
```

