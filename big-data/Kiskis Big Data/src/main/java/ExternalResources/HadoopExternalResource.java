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
package ExternalResources;

import ExternalResources.Threads.*;
import hadoop.HdfsAdmin;
import ubuntu.JavaCheck;
import org.apache.hadoop.conf.Configuration;

/**
 * ...
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$
 */
public class HadoopExternalResource extends MyExternalResource {

    private String nameNode;
    private String jobTracker;
    private String userName;
    boolean isLocal;
    private Configuration conf;

    public HadoopExternalResource(String nameNode, String jobTracker, String userName)
    {
        this.nameNode = nameNode;
        this.jobTracker = jobTracker;
        this.userName = userName;
        isLocal = false;
    }
    public HadoopExternalResource()
    {
         isLocal = true;
    }
    @Override
    public void before(){
        if(isLocal)
            startLocalCluster();
        else
            accessRemoteCluster(nameNode, jobTracker, userName);
       
    }

    private void accessRemoteCluster(String nameNode, String jobTracker, String userName) {
        System.out.println("Accessing to the remote cluster");
        setConf(new Configuration());
        // Check if namenode and jobtracker processes are running on the computers of the given IPs
        if (JavaCheck.checkJavaProcess("NameNode", userName + "@" + nameNode) && JavaCheck.checkJavaProcess("JobTracker",userName+"@"+jobTracker))
        {
//            System.out.println("NameNode and JobTracker are running on the given machines of IPs");
            getConf().set("fs.default.name", "hdfs://" + nameNode + ":8020");
            getConf().set("mapred.job.tracker", jobTracker + ":9000");
            System.setProperty("HADOOP_USER_NAME", userName);
        }
        else
        {
            System.out.println("NameNode and JobTracker are NOT running on the given machines of IPs");
            isLocal = true;
            startLocalCluster();
        }
    }

    public void startLocalCluster()
    {
        System.out.println("Starting Local Cluster!");
        Thread [] threads = new Thread[5];
        threads[0] = new NameNodeThread();
        threads[1] = new DataNodeThread();
        threads[2] = new SecondaryNameNodeThread();
        threads[3] = new JobTrackerThread();
        threads[4] = new TaskTrackerThread();
        int startCount = 0;
        boolean isSuccesful = true;

        //Start the NameNode
        try {
            threads[0].start();
        } catch (Exception e) {
            System.out.println("Could not start NameNode!");
            e.printStackTrace();
        }
        startCount++;

        while(startCount!=threads.length)
        {
            if(threads[startCount-1].getState().toString().equalsIgnoreCase("WAITING"))
            {
                // Leave Safe Mode before JobTracker and TaskTracker starts
//                if(startCount==threads.length-2 | startCount==threads.length-1)
                if(startCount==threads.length-1)
                {
                    HdfsAdmin.leaveSafeMode();
                }
                System.out.println("State of "+threads[startCount-1].getClass().getSimpleName()+" thread is: "+threads[startCount-1].getState());
                try {
                    threads[startCount].start();
                } catch (Exception e) {
                    System.out.println("Could not start "+threads[startCount].getClass().getSimpleName()+"!");
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    isSuccesful = false;
                    break;
                }
                startCount++;
                //Wait for the last threads execution
                if(startCount==threads.length)
                    while(!threads[startCount-1].getState().toString().equalsIgnoreCase("WAITING"))
                        doNothing();
            }
        }
        if(isSuccesful)
        {
            HdfsAdmin.leaveSafeMode();
            System.out.println("Everything should have started succesfully!");
        }
        else
            System.out.println("The cluster is not started succesfully!!!");
    }


    private void doNothing() {
    }
    public static void waitForAllTime()
    {
        System.out.println("Waiting to stop...");
        while(true)
        {
        }
    }

    @Override
    public void after(){

    }

    public Configuration getConf() {
        return conf;
    }

    public void setConf(Configuration conf) {
        this.conf = conf;
    }
}
