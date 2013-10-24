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

/**
 * ...
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$
 */

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.pig.ExecType;
import org.apache.pig.PigServer;
import org.apache.pig.backend.executionengine.ExecException;

import java.util.Properties;

public class PigExternalResource extends MyExternalResource {
    Properties props = new Properties();
    private PigServer pigServer;
    Configuration conf;
    FileSystem fs;

    /**
     * before method
     * @throws Throwable
     */
    @Override
    public void before(){
        System.out.println("Before method of " + this.getClass().toString());
        props.setProperty("fs.default.name", "hdfs://localhost:8020");
        props.setProperty("mapred.job.tracker", "localhost:9000");
        try {
            setPigServer(new PigServer(ExecType.MAPREDUCE, props));
        } catch (ExecException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Override
    public void after(){
        System.out.println("After method of " + this.getClass().toString());

    }



    public PigServer getPigServer() {
        return pigServer;
    }

    public void setPigServer(PigServer pigServer) {
        this.pigServer = pigServer;
    }
}

