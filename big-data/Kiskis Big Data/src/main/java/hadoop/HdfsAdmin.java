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

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.apache.hadoop.hdfs.protocol.FSConstants;

import java.io.IOException;

/**
 * ...
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$
 */
public class HdfsAdmin {
    private static Configuration conf;
    private static FileSystem fs;

    public HdfsAdmin()
    {
        try {
            conf =  new Configuration();
            fs = FileSystem.get(conf);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
    public static void removeDirectoryFromHDFS(String path)
    {
        try {
            conf =  new Configuration();
            fs = FileSystem.get(conf);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        try {
            if(fs.exists(new Path(path)))
            {
                System.out.println(path + " exists and it is being deleted for the new execution!");
                fs.delete(new Path(path), true);
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
    public static void removeDirectoryFromHDFS(Configuration configuration, String path)
    {
        try {
            conf =  configuration;
            fs = FileSystem.get(conf);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        try {
            if(fs.exists(new Path(path)))
            {
                System.out.println(path + " exists and it is being deleted for the new execution!");
                fs.delete(new Path(path), true);
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
    public static String changeOutputDirectoryIfExists(String path)
    {
        try {
            conf =  new Configuration();
            fs = FileSystem.get(conf);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        try {
            int count = 0;
            boolean exists = true;
            while(exists)
            {
                if(fs.exists(new Path(path)))
                {
                    String temp = path;
                    if(count == 1)
                        path = path + count;
                    else
                        path = path.substring(0, path.length()-1) + count;

                    System.out.println(temp + " exists and it is being changed for the new execution to " + path);
                }
                else
                    exists = false;
                count++;
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return path;

    }
    public static void copyDirectoryToHDFS(String inputPathLocal, String inputPathHDFS)
    {
        try {
            conf =  new Configuration();
            fs = FileSystem.get(conf);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        try {
            if(!fs.exists(new Path(inputPathHDFS)))
            {
                System.out.println("Input directory does not exist on HDFS, copying from local filesystem to HDFS");
                fs.copyFromLocalFile(false,true, new Path(inputPathLocal),new Path(inputPathHDFS));
            }
            else
            {
                System.out.println("Input directory " + inputPathHDFS + " already exists on HDFS!");
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
    public static void copyDirectoryToHDFS(Configuration configuration, String inputPathLocal, String inputPathHDFS)
    {
        try {
            conf =  configuration;
            fs = FileSystem.get(conf);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        try {
            if(!fs.exists(new Path(inputPathHDFS)))
            {
                System.out.println("Input directory does not exist on HDFS, copying from local filesystem to HDFS");
                fs.copyFromLocalFile(false,true, new Path(inputPathLocal),new Path(inputPathHDFS));
            }
            else
            {
                System.out.println("Input directory " + inputPathHDFS + " already exists on HDFS!");
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
    public static void leaveSafeMode()
    {
        try {
            conf =  new Configuration();
            fs = FileSystem.get(conf);
            DistributedFileSystem dfs = (DistributedFileSystem) fs;
            dfs.setSafeMode(FSConstants.SafeModeAction.SAFEMODE_LEAVE);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }
}
