=== Hardcoded Fixes Needed ===

Done

=== Release Preparation Problem ===

We also need to fix the final karaf-maven-plugin problem. Run the following
to see the release preparation fail in the last stage when the features file
is being generated.

```
mvn -DsubutaiRelease release:prepare
```

