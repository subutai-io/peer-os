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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ...
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$
 */
public class HiveExternalResource extends MyExternalResource{

    private static String driverName = "org.apache.hadoop.hive.jdbc.HiveDriver";
    private String host;
    private Connection con;
    public HiveExternalResource(String host)
    {
        this.host = host;
    }
    public HiveExternalResource()
    {
        host = "";
    }

    @Override
    public void before() {
        Logger logger = Logger.getLogger("MyLog");
        System.out.println("In before method of " + this.getClass().getName());
        try {
            Class.forName(driverName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }
        try {
            if(host.equals(""))
                    con = DriverManager.getConnection("jdbc:hive://", "", "");
            else
                con = DriverManager.getConnection("jdbc:hive://" + host + ":10000/default", "", "");
        } catch (SQLException e) {
//            System.out.println("Given host " + host +" does not run Hive Thrift Server!");
            logger.log(Level.SEVERE,"Given host " + host +" does not run Hive Thrift Server!");
//            try {
//                con = DriverManager.getConnection("jdbc:hive://", "", "");
//            } catch (SQLException e1) {
//                e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//            }
        }
    }

    public Connection getCon() {
        return con;
    }

    public void setCon(Connection con) {
        this.con = con;
    }
}
