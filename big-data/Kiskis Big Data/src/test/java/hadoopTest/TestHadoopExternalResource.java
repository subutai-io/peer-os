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
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;
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
    String mapredJarPath = "/home/emin/workspace/TestLib/target/TestLib-1.0-SNAPSHOT.jar";

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

//    private static void removeDirectoryFromHDFS(FileSystem fs, String path)
//    {
//        try {
//            if(fs.exists(new Path(path)))
//            {
//                System.out.println(path + " exists and it is being deleted for the new execution!");
//                fs.delete(new Path(path), true);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        }
//    }
//    private static void copyDirectoryToHDFS(FileSystem fs, String inputPathLocal, String inputPathHDFS)
//    {
//        try {
//            if(!fs.exists(new Path(inputPathHDFS)))
//            {
//                System.out.println("Input directory does not exist on HDFS, copying from local filesystem to HDFS");
//                fs.copyFromLocalFile(false,true, new Path(inputPathLocal),new Path(inputPathHDFS));
//            }
//            else
//            {
//                System.out.println("Input directory already exists on HDFS!");
//            }
//        } catch (IOException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        }
//    }
}
