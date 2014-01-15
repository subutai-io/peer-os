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
package hiveTest;

import ExternalResources.HadoopExternalResource;
import ExternalResources.HiveExternalResource;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * ...
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$
 */
public class TestRemoteHive {
    static String hiveHost = "172.16.9.5";

    public static HadoopExternalResource hadoopResource = new HadoopExternalResource(hiveHost,hiveHost,"emin");
    public static HiveExternalResource  hiveResource = new HiveExternalResource(hiveHost);

    @ClassRule
    public static TestRule chain= RuleChain.outerRule(hadoopResource).around(hiveResource);

    @Test
    public void testRemoteHiveThriftServer()
    {
        Statement stmt = null;
        try {
            stmt = hiveResource.getCon().createStatement();
            ResultSet res;
//            stmt.executeQuery("CREATE TABLE " + "denemeRemote" + " (key INT, value STRING)");
//            show tables
            String sql = "show tables";
            System.out.println("Running: " + sql);
            res = stmt.executeQuery(sql);
            while (res.next()) {
                System.out.println(res.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }
}
