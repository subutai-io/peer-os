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
import backtype.storm.testing.TestWordSpout;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.utils.Utils;
import org.junit.Test;
import examples.storm.ExclamationTopology;

/**
 * ...
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$
 */
public class TestStormExternalResource {

    @Test
    public void testStorm()
    {
        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout("word", new TestWordSpout(), 10);
        builder.setBolt("exclaim1", new ExclamationTopology.ExclamationBolt(), 3).shuffleGrouping("word");
        builder.setBolt("exclaim2", new ExclamationTopology.ExclamationBolt(), 2).shuffleGrouping("exclaim1");

        Config conf = new Config();
        conf.setDebug(true);

//        if (args != null && args.length > 0) {
//            conf.setNumWorkers(3);
//
//            StormSubmitter.submitTopology(args[0], conf, builder.createTopology());
//        }
//        else {

            backtype.storm.LocalCluster cluster = new backtype.storm.LocalCluster();
            cluster.submitTopology("test", conf, builder.createTopology());
            Utils.sleep(5000);
            System.out.println("Killing the topology and shutting the cluster down.");
            cluster.killTopology("test");
            cluster.shutdown();
//        }

    }
}
