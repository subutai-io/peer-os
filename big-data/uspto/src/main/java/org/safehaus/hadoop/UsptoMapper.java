package org.safehaus.hadoop;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Properties;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.safehaus.cassandra.CassandraClient;
import org.safehaus.mongodb.MongoDBClient;
import org.safehaus.uspto.UsptoDomParser;
import org.safehaus.uspto.UsptoEntityResolver;
import org.safehaus.uspto.UsptoJDomParser;
import org.safehaus.uspto.dtd.ApplicationReference;
import org.safehaus.uspto.dtd.PublicationReference;
import org.safehaus.uspto.dtd.UsPatentGrant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;

public class UsptoMapper extends Mapper<String, BytesWritable, String, Text> {

	CassandraClient cassandraClient = null;
	String cassandraHost = null;
	MongoDBClient mongoDbClient = null;
	String mongoDbHost = null;
	String mongoDbDatabase = null;
	Logger logger = LoggerFactory.getLogger(this.getClass());
	UsptoDomParser usptoDomParser;
	UsptoJDomParser usptoJDomParser;
	DocumentBuilderFactory dbFactory;
	UsptoEntityResolver entityResolver;

	@Override
	protected void setup(Context context) throws IOException, InterruptedException {
		usptoDomParser = new UsptoDomParser();
		usptoJDomParser = new UsptoJDomParser();
		dbFactory = DocumentBuilderFactory.newInstance();
		entityResolver = new UsptoEntityResolver();
		dbFactory.setExpandEntityReferences(false);
		dbFactory.setValidating(false);
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

		super.setup(context);
	}

	@Override
	public void map(String key, BytesWritable value, Context contex) throws IOException, InterruptedException {

		if (cassandraClient != null)
		{
			cassandraClient.connect(cassandraHost);
		}
		if (mongoDbClient != null)
		{
			try {
				mongoDbClient.connect(mongoDbHost);
				mongoDbClient.connectDB(mongoDbDatabase);
			} catch (UnknownHostException e1) {
				logger.error("Can't connect to mongodb on host {}", mongoDbHost, e1);
				return;
			}
		}

		if (key.endsWith("zip")
				|| key.endsWith("ZIP")) {
			handleZipValue(key, value);
		}
		else if (key.endsWith("xml")
				|| key.endsWith("XML")) {
			handleXMLValue(key, value);
		}
		contex.write(key, new Text("Processed"));
		if (cassandraClient != null)
		{
			cassandraClient.close();
		}
		
		if (mongoDbClient != null)
		{
			mongoDbClient.close();
		}
		
	}
	
	@Override
	protected void cleanup(Context context) throws IOException, InterruptedException {
		// TODO Auto-generated method stub
		super.cleanup(context);
	}
	
	public void handleXMLString(String xmlString)
	{
//		UsPatentGrant usPatentGrant = usptoDomParser.parseXMLString(xmlString, dbFactory, entityResolver);
		UsPatentGrant usPatentGrant = usptoJDomParser.parseXMLString(xmlString, dbFactory, entityResolver);
		if (usPatentGrant != null)
		{
			// Cassandra cql does not allow single quote in queries.
			String xmlStringEscaped = xmlString.replaceAll("'", "''");
			String jsonString = usPatentGrant.toJSon().toString();
			String jsonStringEscaped = jsonString.replaceAll("'", "''");
			PublicationReference publicationReference = usPatentGrant.getUsBibliographicDataGrant().getPublicationReference();
			ApplicationReference applicationReference = usPatentGrant.getUsBibliographicDataGrant().getApplicationReference();
			if (cassandraClient != null)
			{
				cassandraClient.insertXML(publicationReference.getDocumentId().documentNumber,
					publicationReference.getDocumentId().date,
					applicationReference.getDocumentId().documentNumber,
					applicationReference.getDocumentId().date,
					xmlStringEscaped, jsonStringEscaped);
			}
			if (mongoDbClient != null)
			{
				mongoDbClient.insert(new BasicDBObject(publicationReference.getDocumentId().documentNumber,usPatentGrant.toBasicDBObject()));
			}
		}
	}
	
	public void handleXMLValue(String key, BytesWritable value) {
		logger.info("Processing File: {}", key);
		long startTime = System.currentTimeMillis();
		ByteArrayInputStream byteInputStream = new ByteArrayInputStream(value.getBytes());
		int xmlCount = handleMultipleXMLDocuments(byteInputStream);
		long endTime = System.currentTimeMillis();
		logger.info("File: {} processed in {} seconds, contained: {} xml documents",key,(endTime-startTime)/1000,xmlCount);
	}
	
	public void handleZipValue(String key, BytesWritable value) {
		logger.info("Processing File: {}, Bytes: {}", key, value.getLength());
		long startTime = System.currentTimeMillis();

		ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(value.getBytes()));

		int xmlCount = 0;
		ZipEntry entry = null;

		try {
			while ((entry = zipInputStream.getNextEntry()) != null)
			{
					if (entry.isDirectory()) {
						logger.warn("Can't process directory inside zip files. ignoring directory: {}",
								entry.getName());
					}
					if (entry.getName().endsWith("xml")
							|| entry.getName().endsWith("XML")) {
						ByteBuffer byteBuffer = ByteBuffer.allocate((int)entry.getSize());
						byte[] bytes = new byte[2048];
						long remainingBytes = entry.getSize();
						int readBytes = 0;
						do {
							readBytes = zipInputStream.read(bytes);
							byteBuffer.put(bytes, 0, readBytes);
							remainingBytes -= readBytes;
						} while (remainingBytes > 0 && readBytes > 0);
						if (remainingBytes > 0)
						{
							logger.warn("Can't read {} remaining bytes from zip file, ignoring file {}.", remainingBytes, key);
							zipInputStream.close();
							return;
						}
						else
						{
							ByteArrayInputStream byteInputStream = new ByteArrayInputStream(byteBuffer.array());
							xmlCount += handleMultipleXMLDocuments(byteInputStream);
						}
					}
			}
			zipInputStream.close();
		} catch (IOException e) {
			logger.error("Zip error {}", e);
			e.printStackTrace();
		}		
		
		long endTime = System.currentTimeMillis();
		logger.info("File: {} processed in {} seconds, contained: {} xml documents",key,(endTime-startTime)/1000,xmlCount);
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
	
}
