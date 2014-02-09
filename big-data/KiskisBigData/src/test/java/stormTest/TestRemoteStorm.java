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
package stormTest;

import backtype.storm.Config;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.testing.TestWordSpout;
import backtype.storm.topology.TopologyBuilder;
import org.junit.Test;
import examples.storm.ExclamationTopology;

/**
 * ...
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$
 */
public class TestRemoteStorm {

    @Test
    public void testRemoteStorm()
    {
//        System.setProperty("storm.jar", "/home/emin/workspace/TestLib/externalJars/jdo2-api-2.3-ec.jar");
//        System.setProperty("storm.jar", "/home/emin/workspace/TestLib/storm-0.8.2.jar");
//        System.setProperty("storm.jar", "/var/lib/lxc/template-cont/rootfs/home/ubuntu/storm-starter/target/storm-starter-0.0.1-SNAPSHOT-standalone.jar");
        System.setProperty("storm.jar", "/home/emin/workspace/TestLib/target/TestLib-1.0-SNAPSHOT.jar");
        TopologyBuilder builder = new TopologyBuilder();


        builder.setSpout("word", new TestWordSpout());
        builder.setBolt("exclaim1", new ExclamationTopology.ExclamationBolt()).shuffleGrouping("word");
        builder.setBolt("exclaim2", new ExclamationTopology.ExclamationBolt()).shuffleGrouping("exclaim1");

        Config conf = new Config();
//        conf.setDebug(true);
        conf.put(Config.NIMBUS_HOST, "172.16.9.7");
        conf.put(Config.NIMBUS_THRIFT_PORT,6627);
        conf.put(Config.STORM_ZOOKEEPER_PORT,2181);
        conf.put(Config.STORM_ZOOKEEPER_SERVERS,"172.16.9.7");
        conf.setNumWorkers(1);
        conf.setMaxSpoutPending(5000);
        StormSubmitter submitter = new StormSubmitter();
        try {
            submitter.submitTopology("TestLib-jar", conf, builder.createTopology());
//            submitter.submitTopology("test-normal-jar", conf, builder.createTopology());
//            submitter.submitTopology("redundant-jar", conf, builder.createTopology());
        } catch (AlreadyAliveException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InvalidTopologyException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
