package org.safehaus.uspto;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.safehaus.cassandra.CassandraClient;
import org.safehaus.mongodb.MongoDBClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Uspto {

	ArrayList<File> patentFiles = new ArrayList<File>();
	Logger logger = LoggerFactory.getLogger(this.getClass());
	public final String xmlDelimeter = "\\<\\?xml";
	CassandraClient cassandraClient = null;
	String cassandraHost = null;
	MongoDBClient mongoDbClient = null;
	String mongoDbHost = null;
	String mongoDbDatabase = null;
	DocumentBuilderFactory dbFactory;
	DocumentBuilder dBuilder;
	UsptoEntityResolver entityResolver;
	UsptoDomParser usptov40Writer;

	public Uspto()
	{
		dbFactory = DocumentBuilderFactory.newInstance();
		entityResolver = new UsptoEntityResolver();
		dbFactory.setExpandEntityReferences(false);
		dbFactory.setValidating(false);
		try {
			dBuilder = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		dBuilder.setEntityResolver(entityResolver);
		usptov40Writer = new UsptoDomParser();
		
		InputStream inputStream = this.getClass().getResourceAsStream("/uspto.properties");
    	if (inputStream != null)
    	{
    		Properties properties = new Properties();
    		try {
				properties.load(inputStream);
	    		cassandraHost = properties.getProperty("cassandra.host");
	    		if (cassandraHost != null)
	    		{
	    			cassandraClient = new CassandraClient();
	    		}
	    		mongoDbHost = properties.getProperty("mongodb.host");
	    		mongoDbDatabase = properties.getProperty("mongodb.database");
	    		if (mongoDbClient != null && mongoDbDatabase != null)
	    		{
	    			mongoDbClient = new MongoDBClient();
	    		}
			} catch (IOException e) {
				logger.error("Can't load properties file", e);
				e.printStackTrace();
			}
    	}
	}

	public void convertPatents() {

		long startTime = System.currentTimeMillis();

		System.setProperty("entityExpansionLimit","200000");
		
//		addFiles("/home/selcuk/share/uspto");
//		addFiles("/home/selcuk/tmp");
//		addFiles("/home/selcuk/workspace/USPTO/src/main/resources/samples");
		addFiles("/data/trunk/uspto/products/full_text_grants/2013");
//		addFiles("/data/trunk/uspto/products/full_text_grants/2012");
//		addFiles("/data/trunk/uspto/products/full_text_grants/2011");
//		addFiles("/data/trunk/uspto/products/full_text_grants/2010");
//		addFiles("/data/trunk/uspto/products/full_text_grants/2009");
//		addFiles("/data/trunk/uspto/products/full_text_grants/2008");
//		addFiles("/data/trunk/uspto/products/full_text_grants/2007");
//		addFiles("/data/trunk/uspto/products/full_text_grants/2006");
//		addFiles("/data/trunk/uspto/products/full_text_grants/2005");
///		addFiles("/data/trunk/uspto/products/full_text_grants/2004");
		
		if (cassandraClient != null)
		{
			cassandraClient.connect(cassandraHost);
	//		cassandraClient.dropSchema("uspto");
	//		cassandraClient.createSchema();
		}

		if (mongoDbClient != null)
		{
			try {
				mongoDbClient.connect(mongoDbHost);
			} catch (UnknownHostException e1) {
				logger.error("Can't connect to mongodb on host {}", mongoDbHost, e1);
				return;
			}
//			mongoDbClient.dropDB(mongoDbDatabase);
			mongoDbClient.connectDB(mongoDbDatabase);
		}
				
		ConcurrentLinkedQueue<File> patentFilesQueue = new ConcurrentLinkedQueue<File>();
		patentFilesQueue.addAll(patentFiles);
		
		int maxThreadCount = 4;
		
		Thread[] threads = new Thread[maxThreadCount];
		for (int i=0; i<maxThreadCount; i++)
		{
			UsptoThread usptoThread = new UsptoThread(patentFilesQueue, cassandraClient, mongoDbClient, xmlDelimeter);
			Thread thread = new Thread(usptoThread);
			threads[i] = thread;
			thread.start();
		}
		
		for (int i=0; i<maxThreadCount; i++)
		{
			try {
				threads[i].join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (cassandraClient != null)
		{
			cassandraClient.close();
		}
		if (mongoDbClient != null)
		{
			mongoDbClient.close();
		}
		
		long endTime = System.currentTimeMillis();
		logger.info("Main program finished in {} seconds",(endTime-startTime)/1000);

	}


	public void addFiles(String rootDirectoryName) {

		File rootDirectory = new File(rootDirectoryName);
		if (rootDirectory.isFile()) {
			patentFiles.add(rootDirectory);
			return;
		}
		
		if (!rootDirectory.isDirectory())
		{
			return;
		}

		Queue<File> directoriesQueue = new LinkedList<File>();

		directoriesQueue.add(rootDirectory);

		while (!directoriesQueue.isEmpty()) {

			File currentDirectory = directoriesQueue.remove();

			File[] filesList = currentDirectory.listFiles();

			for (File file : filesList) {
				if (file.isFile()) {
					patentFiles.add(file);
				} else if (file.isDirectory()) {
					directoriesQueue.add(file);
				}
			}
		}
	}

}
