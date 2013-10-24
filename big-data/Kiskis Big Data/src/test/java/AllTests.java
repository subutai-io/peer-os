/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
import hadoopTest.RunHadoopandWait;
import hadoopTest.TestHadoopExternalResource;
import hiveTest.TestHiveExternalResource;
import junit.framework.JUnit4TestAdapter;
import luceneTest.TestLuceneExternalResource;
import mahoutTest.TestMahoutExternalResource;
import oozieTest.TestOozieExternalResource;
import oozieTest.TestRemoteOozie;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import pigTest.TestPigExternalResource;
import solrTest.TestRemoteSolr;
import solrTest.TestSolrExternalResource;
import stormTest.TestRemoteStorm;
import stormTest.TestStormExternalResource;

// This section declares all of the test classes in your program.
@RunWith(Suite.class)
@Suite.SuiteClasses({
        // Add test classes here seperated with a comma!
        TestHiveExternalResource.class,
//        RunHadoopandWait.class,
//        TestHadoopExternalResource.class,
//        TestMahoutExternalResource.class,
//        TestPigExternalResource.class,
//        TestRemoteOozie.class,
//        TestOozieExternalResource.class,
//        TestLuceneExternalResource.class,
//        TestRemoteSolr.class,
//        TestSolrExternalResource.class,
//        TestStormExternalResource.class,
//        TestRemoteStorm.class,


})
/**
 * ...
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$
 */
public class AllTests {
    // This can be empty if you are using an IDE that includes support for JUnit
    // such as Eclipse. However, if you are using Java on the command line or
    // with a simpler IDE like JGrasp or jCreator, the following main() and
    // suite()
    // might be helpful.

    // Execution begins at main(). In this test class, we will execute
    // a text test runner that will tell you if any of your tests fail.
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    // The suite() method is helpful when using JUnit 3 Test Runners or Ant.
    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(AllTests.class);
    }

}