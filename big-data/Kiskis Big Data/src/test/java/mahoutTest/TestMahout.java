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
package mahoutTest;

import ExternalResources.HadoopExternalResource;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.mahout.clustering.syntheticcontrol.kmeans.Job;
import org.junit.ClassRule;
import org.junit.Test;

/**
 * ...
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$
 */
public class TestMahout {

//    @ClassRule
//    public static HadoopExternalResource hadoopExternalResource = new HadoopExternalResource();

    @Test
    public void testMahoutResource() throws Exception {

        Configuration conf = new Configuration();

        // The input file has to be under the testdata directory on the HDFS
        String inputPathHDFS="testdata";
        // If the input file does not exist on HDFS, then you will need this variable to copy the input data from local file system to HDFS where it stands for the path of the input file
        // To download the file, you may refer to this guide: https://confluence.safehaus.org/display/KSKSDATA/Mahout+Installation+on+Hadoop
        String inputPathLocal = "/home/emin/test_documents/synthetic_control.data";

        //If the input file does not exist on the hdfs, then copy it from local file system to HDFS
        FileSystem fs = FileSystem.get(conf);
        if(!fs.exists(new Path(inputPathHDFS)))
        {
            System.out.println("Input directory does not exist on HDFS, copying from local filesystem to HDFS");
            fs.copyFromLocalFile(false,true, new Path(inputPathLocal),new Path(inputPathHDFS));
        }
        else
        {
            System.out.println("Input directory already exists on HDFS!");
        }
        // Run mahout job
        Job.main(new String[0]);
    }
}
