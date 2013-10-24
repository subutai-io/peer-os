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
package oozie;

/**
* ...
*
* @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
* @version $Rev$
*/
public class denemeOozieCluster {

    public static void main(String [] args)
    {
        String [] argv = new String [2];
        argv[0] = "hdfs://127.0.0.1:8020/user/emin/examples/apps/map-reduce"; // WF_APP_HDFS_URI
        argv[1] = "/home/emin/workspace/TestLib/examples/oozie/examples/apps/map-reduce/workflow.xml"; // WF_PROPERTIES
        System.setProperty("oozie.home.dir", "/opt/oozie-3.3.2");
        LocalOozieExample.main(argv);
    }
}
