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
package ExternalResources.Threads;

import org.apache.hadoop.mapred.JobTracker;

/**
 * Created with IntelliJ IDEA.
 * User: frkn
 * Date: 10/1/13
 * Time: 10:23 AM
 * To change this template use File | Settings | File Templates.
 */
public class JobTrackerThread extends Thread implements Runnable {
    public void run()
    {
        String [] temp = new String[0];
        try {
            System.out.println("Starting JobTracker!");
            System.setProperty("hadoop.log.dir", "/home/emin/Desktop/hadoop_play/my_hdfs/logs");
            System.setProperty("hadoop.job.history.location","/home/emin/Desktop/hadoop_play/my_hdfs/logs/history");
            JobTracker.main(temp);
        } catch (Exception e) {
            System.out.println("Could not start JobTracker!");
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
