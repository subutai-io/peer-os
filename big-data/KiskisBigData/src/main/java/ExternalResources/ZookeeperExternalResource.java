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

import org.apache.zookeeper.server.NIOServerCnxn;
import org.apache.zookeeper.server.ZooKeeperServer;
import org.junit.rules.ExternalResource;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * ...
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$
 */
public class ZookeeperExternalResource extends ExternalResource {

    @Override
    public void before()
    {
        int clientPort = 21818; // none-standard
        int numConnections = 5000;
        int tickTime = 2000;
        String dataDirectory = System.getProperty("java.io.tmpdir");

        File dir = new File(dataDirectory, "zookeeper").getAbsoluteFile();

        ZooKeeperServer server = null;
        try {
            server = new ZooKeeperServer(dir, dir, tickTime);
            NIOServerCnxn.Factory standaloneServerFactory = new NIOServerCnxn.Factory(new InetSocketAddress(clientPort), numConnections);

            standaloneServerFactory.startup(server);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }
    @Override
    public void after()
    {

    }

}
