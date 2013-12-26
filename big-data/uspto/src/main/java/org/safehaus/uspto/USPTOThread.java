package org.safehaus.uspto;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.safehaus.cassandra.CassandraClient;
import org.safehaus.mongodb.MongoDBClient;
import org.safehaus.uspto.dtd.ApplicationReference;
import org.safehaus.uspto.dtd.PublicationReference;
import org.safehaus.uspto.dtd.UsPatentGrant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.mongodb.BasicDBObject;

public class USPTOThread implements Runnable {

	ConcurrentLinkedQueue<File> patentFilesQueue;
	CassandraClient cassandraClient;
	MongoDBClient mongoDbClient;
	Logger logger = LoggerFactory.getLogger(this.getClass());
	public final String xmlDelimeter;
	DocumentBuilderFactory dbFactory;
	DocumentBuilder dBuilder;
	USPTOEntityResolver entityResolver;
	USPTOV44Parser usptov40Writer;

	public USPTOThread(ConcurrentLinkedQueue<File> patentFilesQueue, CassandraClient cassandraClient, MongoDBClient mongoDbClient, final String xmlDelimeter)
	{
		this.patentFilesQueue = patentFilesQueue;
		this.cassandraClient = cassandraClient;
		this.mongoDbClient = mongoDbClient;
		this.xmlDelimeter = xmlDelimeter;
		dbFactory = DocumentBuilderFactory.newInstance();
		entityResolver = new USPTOEntityResolver();
		dbFactory.setExpandEntityReferences(false);
		dbFactory.setValidating(false);
		try {
			dBuilder = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		dBuilder.setEntityResolver(entityResolver);
		usptov40Writer = new USPTOV44Parser();
	}

	public void run() {
		File file = null;
		while ((file = patentFilesQueue.poll()) != null)
		{
			if (file.isFile()) {
				if (file.getName().endsWith("zip")
						|| file.getName().endsWith("ZIP")) {
					handleZipFile(file);
				}
				else if (file.getName().endsWith("xml")
						|| file.getName().endsWith("XML")) {
					handleXMLFile(file);
				}

			} else if (file.isDirectory()) {
				logger.error("Error");
			}
		}
	}
	
	public void handleXMLString(String xmlString)
	{
		Document doc = null;
		try {
			DocumentBuilder myBuilder = dbFactory.newDocumentBuilder();
			myBuilder.setEntityResolver(entityResolver);
			doc = myBuilder.parse(new InputSource(new StringReader(xmlString)));
			//cassandraClient.insertXML("Deneme1", xmlString);
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();}
		 catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
//		System.out.println(doc.getDoctype());
		UsPatentGrant usPatentGrant = usptov40Writer.parseDocument(doc);
		if (usPatentGrant != null)
		{
			// Cassandra cql does not allow single quote in queries.
			String xmlStringEscaped = xmlString.replaceAll("'", "''");
			String jsonString = usPatentGrant.toJSon().toString();
			String jsonStringEscaped = jsonString.replaceAll("'", "''");
			PublicationReference publicationReference = usPatentGrant.getUsBibliographicDataGrant().getPublicationReference();
			ApplicationReference applicationReference = usPatentGrant.getUsBibliographicDataGrant().getApplicationReference();
			cassandraClient.insertXML(publicationReference.getDocumentId().documentNumber,
					publicationReference.getDocumentId().date,
					applicationReference.getDocumentId().documentNumber,
					applicationReference.getDocumentId().date,
					xmlStringEscaped, jsonStringEscaped);
			mongoDbClient.insert(new BasicDBObject(publicationReference.getDocumentId().documentNumber,usPatentGrant.toBasicDBObject()));
		}
	}
	 
	public int handleMultipleXMLDocuments(InputStream fileStream)
	{
		int xmlCount = 0;
		entityResolver.resetCurrentVersion();
		Scanner scanner = new Scanner(fileStream);
		scanner.useDelimiter("\\<\\?xml");
		while (scanner.hasNext())
		{
			String xmlString = "<?xml"+scanner.next();
			//String nextXmlString = "<?xml " + xmlString.replaceFirst("\\?>", " standalone=\"yes\" \\?>");
			//String finalString = nextXmlString.replaceFirst("<!DOCTYPE[^>]*>", "");
			//System.out.println("scanner output: " + finalString);			
			handleXMLString(xmlString);
			xmlCount++;
		}
		scanner.close();
		return xmlCount;
	}

	public void handleXMLFile(File file) {
		logger.info("Processing File: {}", file.getName());
		long startTime = System.currentTimeMillis();
		InputStream fileStream = null;
		
		try {
			fileStream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		int xmlCount = handleMultipleXMLDocuments(fileStream);
		long endTime = System.currentTimeMillis();
		logger.info("File: {} processed in {} seconds, contained: {} xml documents",file.getName(),(endTime-startTime)/1000,xmlCount);
	}

	public void handleZipFile(File file) {
		logger.info("Processing File: {}", file.getName());
		long startTime = System.currentTimeMillis();

		try {
			ZipFile zipFile = new ZipFile(file);
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			int xmlCount = 0;
			while (entries.hasMoreElements()) {
				
				ZipEntry entry = entries.nextElement();

				if (entry.isDirectory()) {
					logger.warn("Can't process directory inside zip files. ignoring directory: {}",
							entry.getName());
				}
				else {
					//logger.info("Processing File: {}", entry.getName());
					InputStream fileStream = zipFile.getInputStream(entry);
					if (entry.getName().endsWith("xml")
								|| entry.getName().endsWith("XML")) {
							xmlCount += handleMultipleXMLDocuments(fileStream);
					}

					
				}

			}
			zipFile.close();
			long endTime = System.currentTimeMillis();
			logger.info("File: {} processed in {} seconds, contained: {} xml documents",file.getName(),(endTime-startTime)/1000,xmlCount);
		} catch (ZipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
