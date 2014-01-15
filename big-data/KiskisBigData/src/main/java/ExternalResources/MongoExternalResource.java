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

import com.mongodb.MongoClient;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import org.junit.rules.ExternalResource;

import java.io.IOException;
import java.net.UnknownHostException;

/**
 * ...
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$
 */
public class MongoExternalResource extends ExternalResource {

    private int port;
    private MongodExecutable mongodExecutable;
    private MongoClient mongo;

    public MongoExternalResource(int port)
    {
        this.setPort(port);
    }
    @Override
    public void before()
    {
        IMongodConfig mongodConfig = null;
        try {
            mongodConfig = new MongodConfigBuilder()
                    .version(Version.Main.PRODUCTION)
                    .net(new Net(getPort(), de.flapdoodle.embed.process.runtime.Network.localhostIsIPv6()))
                    .build();
        } catch (IOException e) {
            e.printStackTrace();
        }

        MongodStarter runtime = MongodStarter.getDefaultInstance();

        mongodExecutable = null;
        mongodExecutable = runtime.prepare(mongodConfig);
        try {
            MongodProcess mongod = mongodExecutable.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            setMongo(new MongoClient("localhost", port));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void after()
    {
        if (mongodExecutable != null)
            mongodExecutable.stop();
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public MongoClient getMongo() {
        return mongo;
    }

    public void setMongo(MongoClient mongo) {
        this.mongo = mongo;
    }
}
