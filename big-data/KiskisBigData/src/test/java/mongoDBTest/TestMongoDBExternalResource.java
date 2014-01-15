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

import ExternalResources.MongoExternalResource;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;

/**
 * ...
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$
 */
public class TestMongoDBExternalResource {

    @ClassRule
    public static MongoExternalResource mongoExternalResource = new MongoExternalResource(12345);

    @Test
    public void testLocalMongo() throws IOException {
        MongoClient mongo = mongoExternalResource.getMongo();
        DB db = mongo.getDB("test");
        DBCollection col = db.createCollection("testCol", new BasicDBObject());
        col.save(new BasicDBObject("testDoc", new Date()));
        DBCollection col2 = db.getCollection("testCol");
        System.out.println("Column count: " + col2.getCount());
        Assert.assertEquals(1,col2.getCount());
    }
}
