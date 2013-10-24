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

import ExternalResources.HadoopExternalResource;
import ExternalResources.OozieExternalResource;
import ExternalResources.OrderExternalResources;
import ExternalResources.PigExternalResource;
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
public class TestOozieExternalResource {
    //static HadoopExternalResource hadoopExternalResource = new HadoopExternalResource();
    static PigExternalResource pigExternalResource = new PigExternalResource();
    static OozieExternalResource oozieExternalResource = new OozieExternalResource("/home/emin/test_documents/conf/oozie-conf","/home/emin/test_documents/oozie-data");
//    static OozieExternalResource oozieExternalResource = new OozieExternalResource("localhost");


    @ClassRule
    public static OrderExternalResources resources = new OrderExternalResources(pigExternalResource,oozieExternalResource);

    @Test
    public void sendOozieJob()
    {
        // Configure Oozie Server
        //Place examples and share directories(comes with the Oozie distribution) to HDFS if does not exist
        HdfsAdmin.copyDirectoryToHDFS("/home/emin/test_documents/examples/oozie/examples/", "/user/emin/examples/");
        HdfsAdmin.copyDirectoryToHDFS("/home/emin/test_documents/examples/oozie/share/", "/user/emin/share/");

        // Set Oozie path for the workflow job
        oozieExternalResource.getConf().setProperty(OozieClient.APP_PATH, "hdfs://localhost:8020/user/emin/examples/apps/map-reduce");

        // setting workflow parameters
        oozieExternalResource.getConf().setProperty("nameNode","hdfs://localhost:8020");
        oozieExternalResource.getConf().setProperty("jobTracker", "localhost:9000");
        //
//        conf.setProperty("inputDir", "/user/emin/Desktop/hadoop_play");
        oozieExternalResource.getConf().setProperty("outputDir", "map-reduce-local-java");
        oozieExternalResource.getConf().setProperty("examplesRoot","examples");
        oozieExternalResource.getConf().setProperty("queueName", "default");

        // submit and start the workflow job
        String jobId = null;
        try {
            jobId = oozieExternalResource.getWc().run(oozieExternalResource.getConf());
        } catch (OozieClientException e) {
            e.printStackTrace();
        }
        System.out.println("Workflow job submitted");

        // wait until the workflow job finishes printing the status every 10 seconds
        try {
            while (oozieExternalResource.getWc().getJobInfo(jobId).getStatus() == WorkflowJob.Status.RUNNING) {
                System.out.println("Workflow job running ...");
                Thread.sleep(10 * 1000);
            }
        } catch (OozieClientException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // print the final status o the workflow job
        System.out.println("Workflow job completed ...");
        try {
            System.out.println(oozieExternalResource.getWc().getJobInfo(jobId));
        } catch (OozieClientException e) {
            e.printStackTrace();
        }
        try {
            OozieExternalResource.printWorkflowInfo(oozieExternalResource.getWc().getJobInfo(jobId));
        } catch (OozieClientException e) {
            e.printStackTrace();
        }
//        HadoopExternalResource.waitForAllTime();
    }

}
