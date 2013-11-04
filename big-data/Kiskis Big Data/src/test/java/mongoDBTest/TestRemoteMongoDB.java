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
package mongoDBTest;

import com.mongodb.*;
import org.junit.Test;

import java.net.UnknownHostException;
import java.util.Set;

/**
 * ...
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$
 */
public class TestRemoteMongoDB {

    @Test
    public void testMongoDB() throws UnknownHostException {
        // connect to the local database server
        MongoClient mongoClient = new MongoClient( "localhost" , 27017 );

        // get handle to "test"
        DB db = mongoClient.getDB("test");

        // Authenticate - optional
        // boolean auth = db.authenticate("foo", "bar");

        // get a list of the collections in this database and print them out
        Set<String> collectionNames = db.getCollectionNames();
        for (String s : collectionNames) {
            System.out.println("Collection name: " + s);
        }
        for (String s : mongoClient.getDatabaseNames()) {
            System.out.println("Database Name: " + s);
        }
        DBCollection coll = db.getCollection("testCollection2");
//        BasicDBObject doc = new BasicDBObject("name", "MongoDB").
//                append("type", "database").
//                append("count", 1).
//                append("info", new BasicDBObject("x", 204).append("y", 103));
//
//        coll.insert(doc);
        DBObject myDoc = coll.findOne();
        System.out.println(myDoc);

        DBCursor cursor = coll.find();
        try {
            while(cursor.hasNext()) {
                System.out.println(cursor.next());
            }
        } finally {
            cursor.close();
        }


    }
}
