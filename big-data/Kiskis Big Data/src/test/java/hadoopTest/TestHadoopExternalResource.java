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
package hadoopTest;

import ExternalResources.HadoopExternalResource;
import hadoop.HdfsAdmin;
import hadoop.WordCountExample.WordMapper;
import hadoop.WordCountExample.WordReducer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.apache.hadoop.hdfs.protocol.ClientProtocol;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Scanner;


/**
 * ...
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$
 */
public class TestHadoopExternalResource {

    @ClassRule
    public static HadoopExternalResource resource = new HadoopExternalResource();

    String inputPathHDFS="/home/emin/dft-input/dft";
    String inputPathLocal = "/home/emin/Desktop/hadoop_play/dft";
    String outputPath="/home/emin/dft-output";
    String mapredJarPath = "/home/emin/main/Kiskis Big Data/target/KiskisBigData-1.0-SNAPSHOT.jar";

    @Test
    public void testHadoopCluster() throws IOException, ClassNotFoundException, InterruptedException {
        Configuration conf = new Configuration();
        conf.set("mapred.jar", mapredJarPath);

        //If the input file does not exist on the hdfs, then copy it from local file system to HDFS
        FileSystem fs = FileSystem.get(conf);
        HdfsAdmin.copyDirectoryToHDFS(inputPathLocal, inputPathHDFS);
        System.out.println("Testing the hadoop cluster with a MapReduce JOB!");
        Job job = new Job(conf, "wordcount");
//        job.setJarByClass(WordCount.class);
        job.setMapperClass(WordMapper.class);
        job.setReducerClass(WordReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        job.setInputFormatClass(KeyValueTextInputFormat.class);
        FileInputFormat.addInputPath(job, new Path(inputPathHDFS));

        HdfsAdmin.removeDirectoryFromHDFS(outputPath);

        FileOutputFormat.setOutputPath(job, new Path(outputPath));
        System.out.println("JOB JAR: " + job.getJar());
        job.waitForCompletion(true);
//        HadoopExternalResource.waitForAllTime();
    }
    @Test
    public void printHdfsStatistics() throws IOException {
        Configuration conf = resource.getConf();
        FileSystem fs = FileSystem.get(conf);
        DistributedFileSystem dfs = (DistributedFileSystem) fs;
        System.out.println("Content Summary: "+dfs.getClient().namenode.getContentSummary("/"));
        ClientProtocol clientProtocol = dfs.getClient().namenode;
        try {
            System.out.println("Printing Distributed File System Home Directory:");
            FileSystem fileSystem = DistributedFileSystem.get(conf);
            System.out.println(fileSystem.getHomeDirectory());
            System.out.println("Statistics:");
            for (FileSystem.Statistics statistics : DistributedFileSystem.getAllStatistics()) {
                System.out.println(statistics.toString());
            }

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }
    @Test
    public void checkJobTrackerStatus() throws IOException {
        JobClient jobClient = new JobClient(new InetSocketAddress("localhost", 9000), new Configuration());
        System.out.println("JobTracker State: " + jobClient.getClusterStatus().getJobTrackerState().toString());
        Assert.assertTrue(jobClient.getClusterStatus().getJobTrackerState().toString().equalsIgnoreCase("RUNNING"));
    }

}
