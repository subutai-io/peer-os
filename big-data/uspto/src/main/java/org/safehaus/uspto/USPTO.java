package org.safehaus.uspto;

import java.io.File;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
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
	public final String cassandraHost = "172.16.9.13";
	public final String mongoDbHost = "172.16.9.13";
	DocumentBuilderFactory dbFactory;
	DocumentBuilder dBuilder;
	UsptoEntityResolver entityResolver;
	CassandraClient cassandraClient;
	MongoDBClient mongoDbClient;
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
		cassandraClient = new CassandraClient();
		mongoDbClient = new MongoDBClient();

	}

	public void convertPatents() {

		long startTime = System.currentTimeMillis();

		System.setProperty("entityExpansionLimit","200000");
		
//		addFiles("/home/selcuk/share/uspto");
		addFiles("/home/selcuk/tmp");
//		addFiles("/home/selcuk/workspace/USPTO/src/main/resources/samples");
//		addFiles("/data/trunk/uspto/products/full_text_grants/2013");
//		addFiles("/data/trunk/uspto/products/full_text_grants/2012");
//		addFiles("/data/trunk/uspto/products/full_text_grants/2011");
//		addFiles("/data/trunk/uspto/products/full_text_grants/2010");
//		addFiles("/data/trunk/uspto/products/full_text_grants/2009");
//		addFiles("/data/trunk/uspto/products/full_text_grants/2008");
//		addFiles("/data/trunk/uspto/products/full_text_grants/2007");
//		addFiles("/data/trunk/uspto/products/full_text_grants/2006");
//		addFiles("/data/trunk/uspto/products/full_text_grants/2005");
///		addFiles("/data/trunk/uspto/products/full_text_grants/2004");
		
		cassandraClient.connect(cassandraHost);
//		cassandraClient.dropSchema("uspto");
//		cassandraClient.createSchema();

		try {
			mongoDbClient.connect(mongoDbHost);
		} catch (UnknownHostException e1) {
			logger.error("Can't connect to mongodb on host {}", mongoDbHost, e1);
			return;
		}
//		mongoDbClient.dropDB("uspto");
		mongoDbClient.connectDB("uspto");
		
//		cassandraClient.loadData();
//		cassandraClient.queryData();
		
		ConcurrentLinkedQueue<File> patentFilesQueue = new ConcurrentLinkedQueue<File>();
		patentFilesQueue.addAll(patentFiles);
		
		int maxThreadCount = 3;
		
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

		cassandraClient.close();
		mongoDbClient.close();
		
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
