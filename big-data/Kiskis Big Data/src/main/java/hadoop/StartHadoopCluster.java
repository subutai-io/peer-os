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
package hadoop;

import ExternalResources.Threads.*;
import hadoop.WordCountExample.WordMapper;
import hadoop.WordCountExample.WordReducer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import ubuntu.JavaCheck;

import java.io.IOException;
import java.util.Scanner;

/**
 * ...
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$
 */
public class StartHadoopCluster {

    String inputPathHDFS="/home/emin/dft-input/dft";
    String inputPathLocal = "/home/emin/Desktop/hadoop_play/dft";
    String outputPath="/home/emin/dft-output";
    String mapredJarPath = "/home/emin/workspace/TestLib/target/TestLib-1.0-SNAPSHOT.jar";

    boolean isLocal = true;
    private Configuration conf;

    public static void main(String [] args) throws InterruptedException, IOException, ClassNotFoundException {
        StartHadoopCluster a = new StartHadoopCluster();
        a.startLocalCluster();
        while(true)
            Thread.sleep(2147483647);
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
//            threads[0].join();
        } catch (Exception e) {
            System.out.println("Could not start NameNode!");
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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
//                    Turn SafeMode off if turned on
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


    public Configuration getConf() {
        return conf;
    }

    public void setConf(Configuration conf) {
        this.conf = conf;
    }
}
