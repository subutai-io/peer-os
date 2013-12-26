package org.safehaus.mongodb;

import java.net.UnknownHostException;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class MongoDBClient {

	private MongoClient mongoClient;
	private DB usptoDB;
	private DBCollection usptoCollection;
	
	public MongoDBClient()
	{
		
	}
	
	public void connect(String node) throws UnknownHostException {
		if (mongoClient != null)
		{
			mongoClient.close();
		}
		
		mongoClient = new MongoClient(node);		
	}
	
	public void dropDB(String dbName)
	{
		mongoClient.dropDatabase(dbName);
		usptoDB = null;
	}

	public void connectDB(String dbName)
	{
		usptoDB = mongoClient.getDB(dbName);
		usptoCollection = usptoDB.getCollection("patents");
	}

	public void close(){
		if (mongoClient != null)
		{
			mongoClient.close();
			mongoClient = null;
		}
	}
	
	public void insert(DBObject patent)
	{
		usptoCollection.insert(patent);
	}
	
}
