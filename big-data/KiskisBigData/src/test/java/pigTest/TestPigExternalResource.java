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
package pigTest;

import ExternalResources.HadoopExternalResource;
import ExternalResources.PigExternalResource;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.pig.PigServer;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import java.io.IOException;
import java.util.ArrayList;

/**
 * ...
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$
 */

public class TestPigExternalResource {

    public static HadoopExternalResource hadoopResource = new HadoopExternalResource();
//    @ClassRule
    public static PigExternalResource pigResource = new PigExternalResource();

    @ClassRule
    public static TestRule chain = RuleChain.outerRule(hadoopResource).around(pigResource);

    /**
     * Run pig-latin query on hadoop
     *
     * @throws IOException
     */
    @Test
    public void testPigLatinExample() throws IOException {
        System.out.println("During Test...");
        runIdQuery(pigResource.getPigServer(), "passwd");
    }

    public static void runIdQuery(PigServer pigServer, String inputFile) throws IOException {
        FileSystem fs;
        Configuration conf = new Configuration();
        fs = FileSystem.get(conf);
        try {
            copyFromLocal("/etc/passwd", "/user/emin/");
            deleteOutputFile("/user/emin/id.out");

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        pigServer.registerQuery("A = load '" + inputFile + "' using PigStorage(':');");
        pigServer.registerQuery("B = foreach A generate $0 as id;");
        pigServer.store("B", "id.out");
    }
    /**
     * Delete output file if exists.
     *
     * @param directory
     * @throws IOException
     * @throws InterruptedException
     */
    public static void deleteOutputFile(String directory) throws IOException, InterruptedException {
        FileSystem fs;
        Configuration conf = new Configuration();
        fs = FileSystem.get(conf);
        Path path = new Path(directory) ;
        // Check if the file already exists
        if ((fs.exists(path))) {
            fs.delete(path, true); // delete file, true for recursive
            System.out.println("");
            return;
        }
    }


    /**
     * Copy local file to HDFS.
     * @param source
     * @param dest
     * @throws IOException
     */
    public static void copyFromLocal(String source, String dest) throws IOException {
        FileSystem fs;
        Configuration conf = new Configuration();
        fs = FileSystem.get(conf);
        // Get the filename out of the file path
        String filename = source.substring(source.lastIndexOf('/') + 1, source.length());

        Path srcPath = new Path(source);
        Path dstPath = new Path(dest + "/" + filename);

        // Check if the file already exists
        if ((fs.exists(dstPath))) {
            System.out.println("File \'" + filename + "\' is already in directory ");
            return;
        }

        try{
            fs.copyFromLocalFile(srcPath, dstPath);
            System.out.println("File \'" + filename + "\' copied to " + dest + ".");
        }catch(Exception e){
            System.err.println("Exception caught! :" + e);
            System.exit(1);
        }
    }



}
