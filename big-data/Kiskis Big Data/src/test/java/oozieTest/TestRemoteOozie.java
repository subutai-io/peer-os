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
package oozieTest;

import ExternalResources.OozieExternalResource;
import hadoop.HdfsAdmin;
import org.apache.oozie.client.OozieClient;
import org.apache.oozie.client.OozieClientException;
import org.apache.oozie.client.WorkflowJob;
import org.junit.ClassRule;
import org.junit.Test;

/**
* ...
*
* @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
* @version $Rev$
*/
public class TestRemoteOozie {
    @ClassRule
    public static OozieExternalResource oozieExternalResource = new OozieExternalResource("localhost");

    @Test
    public void testOozie() throws OozieClientException {

        //Place examples and share directories(comes with the Oozie distribution) to HDFS if does not exist
        HdfsAdmin.copyDirectoryToHDFS("/home/emin/workspace/TestLib/examples/oozie/examples/", "/user/emin/examples/");
        HdfsAdmin.copyDirectoryToHDFS("/home/emin/workspace/TestLib/examples/oozie/share/", "/user/emin/share/");

        // Set Oozie path for the workflow job
        oozieExternalResource.getConf().setProperty(OozieClient.APP_PATH, "hdfs://localhost:8020/user/emin/examples/apps/map-reduce");

        // setting workflow parameters
        oozieExternalResource.getConf().setProperty("nameNode","hdfs://localhost:8020");
        oozieExternalResource.getConf().setProperty("jobTracker", "localhost:9000");
        //
//        conf.setProperty("inputDir", "/user/emin/Desktop/hadoop_play");
        oozieExternalResource.getConf().setProperty("outputDir", "map-reduce-remote-java");
        oozieExternalResource.getConf().setProperty("examplesRoot","examples");
        oozieExternalResource.getConf().setProperty("queueName", "default");

//        submit and start the workflow job
        String jobId = oozieExternalResource.getWc().run(oozieExternalResource.getConf());
        System.out.println("Workflow job submitted");

        // wait until the workflow job finishes printing the status every 10 seconds
        while (oozieExternalResource.getWc().getJobInfo(jobId).getStatus() == WorkflowJob.Status.RUNNING) {
            System.out.println("Workflow job running ...");
            try {
                Thread.sleep(10 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

        // print the final status o the workflow job
        System.out.println("Workflow job completed ...");
        System.out.println(oozieExternalResource.getWc().getJobInfo(jobId));
    }
}
