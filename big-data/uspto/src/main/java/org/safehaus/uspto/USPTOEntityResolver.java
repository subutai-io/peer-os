package org.safehaus.uspto;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;

import org.safehaus.cassandra.CachedInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.EntityResolver2;

public class UsptoEntityResolver implements EntityResolver2 {
	

	public enum Version {
	    US_PATENT_GRANT_V24 (0,"/dtd/us-patent-grant-v24/dtds/"),
	    US_PATENT_GRANT_V25 (1,"/dtd/us-patent-grant-v25/dtds/"),
	    US_PATENT_GRANT_V30 (2,"/dtd/us-patent-grant-v30/dtds/"),
	    US_PATENT_GRANT_V40 (3,"/dtd/us-patent-grant-v40/dtds/"),
	    US_PATENT_GRANT_V41 (4,"/dtd/us-patent-grant-v41/dtds/"),
	    US_PATENT_GRANT_V42 (5,"/dtd/us-patent-grant-v42/dtds/"),
	    US_PATENT_GRANT_V43 (6,"/dtd/us-patent-grant-v43/dtds/"),
	    US_PATENT_GRANT_V44 (7,"/dtd/us-patent-grant-v44/dtds/");

	    private final int index;
	    private final String path;
	    
	    Version(int index, String path) {
	    	this.index = index;
	        this.path = path;
	    }
	    
	    public String path() { return path; }
	    
	    public int index() { return index; }
	    
	}
	
	Logger logger = LoggerFactory.getLogger(this.getClass());
	private Version currentVersion = null;
	private String lastFileName;
	
	private ArrayList<Hashtable<String, CachedInputStream>> streamCache;
	
	public UsptoEntityResolver()
	{
		streamCache = new ArrayList<Hashtable<String,CachedInputStream>>(8);
		for (int i=0; i<8; i++)
		{
			streamCache.add(i, new Hashtable<String, CachedInputStream>());
		}
	}
	
	public void resetCurrentVersion()
	{
		currentVersion = null;
		lastFileName = null;
	}
	
	public InputSource resolveEntity(String publicId, String systemId)
	        throws SAXException, IOException {
			System.out.println("publicId: "+publicId+" systemId: "+systemId);
			File ff = new File(systemId);
			System.out.println("base: "+ff.getName());
	    return null;
	}

	public InputSource getExternalSubset(String name, String baseURI)
	        throws SAXException, IOException {
	    System.out.print("getExternalSubset:");
	    System.out.print(" - name = "+name);
	    System.out.println(" - baseURI = "+baseURI);
	    return null;
	}

	public InputSource resolveEntity(String name, String publicId,
	        String baseURI, String systemId) throws SAXException, IOException {
		
//	    System.out.print("resolveEntity:");
//	    System.out.print(" - name = "+name);
//	    System.out.print(" - publicId = "+publicId);
//	    System.out.print(" - baseURI = "+baseURI);
//	    System.out.println(" - systemId = "+systemId);
	    
	    //correct directory separators for current system
	    String fileName = systemId.replaceAll("\\\\", File.separator);
	    
	    //If we receive an entity name for top level dtd, set currentVersion to the new version.
	    if (fileName.contains("ST32-US-Grant-024.dtd"))
	    {
	    	if (currentVersion == null || currentVersion != Version.US_PATENT_GRANT_V24)
	    	{
	    		logger.info("Found {}", fileName);
	    	}
	    	currentVersion = Version.US_PATENT_GRANT_V24;
	    }
	    else if (fileName.contains("ST32-US-Grant-025xml.dtd"))
	    {
	    	if (currentVersion == null || currentVersion != Version.US_PATENT_GRANT_V25)
	    	{
	    		logger.info("Found {}", fileName);
	    	}
	    	currentVersion = Version.US_PATENT_GRANT_V25;
	    }
	    else if (fileName.contains("us-patent-grant-v30-2004-03-04.dtd"))
	    {
	    	if (currentVersion == null || currentVersion != Version.US_PATENT_GRANT_V24)
	    	{
	    		logger.info("Found {}", fileName);
	    	}
	    	currentVersion = Version.US_PATENT_GRANT_V30;
	    }
	    else if (fileName.matches("us-patent-grant-v40.*\\.dtd"))
	    {
	    	if (currentVersion == null || currentVersion != Version.US_PATENT_GRANT_V40
	    			|| !lastFileName.equals(fileName))
	    	{
//	    		logger.info("Found {}", fileName);
	    		lastFileName = fileName;
	    	}
	    	currentVersion = Version.US_PATENT_GRANT_V40;
	    }
	    else if (fileName.contains("us-patent-grant-v41-2005-08-25.dtd"))
	    {
	    	if (currentVersion == null || currentVersion != Version.US_PATENT_GRANT_V41)
	    	{
//	    		logger.info("Found {}", fileName);
	    	}
	    	currentVersion = Version.US_PATENT_GRANT_V41;
	    }
	    else if (fileName.contains("us-patent-grant-v42-2006-08-23.dtd"))
	    {
	    	if (currentVersion == null || currentVersion != Version.US_PATENT_GRANT_V42)
	    	{
	    		//logger.info("Found {}", fileName);
	    	}
	    	currentVersion = Version.US_PATENT_GRANT_V42;
	    }
	    else if (fileName.contains("us-patent-grant-v43-2012-12-04.dtd"))
	    {
	    	if (currentVersion == null || currentVersion != Version.US_PATENT_GRANT_V43)
	    	{
	    		//logger.info("Found {}", fileName);
	    	}
	    	currentVersion = Version.US_PATENT_GRANT_V43;
	    }
	    else if (fileName.contains("us-patent-grant-v44-2013-05-16.dtd"))
	    {
	    	if (currentVersion == null || currentVersion != Version.US_PATENT_GRANT_V44)
	    	{
	    		//logger.info("Found {}", fileName);
	    	}
	    	currentVersion = Version.US_PATENT_GRANT_V44;
	    }
	    
	    Hashtable<String, CachedInputStream> streamTable = streamCache.get(currentVersion.index());
	    
	    CachedInputStream cachedStream = null;
	    
	    if (streamTable.containsKey(fileName))
	    {
	    	cachedStream = streamTable.get(fileName);
	    }
	    else
	    {
		    //First time this entity is referenced, read file and insert to hashtable.
	    	InputStream inputStream = this.getClass().getResourceAsStream(currentVersion.path()+fileName);
	    	if (inputStream == null)
	    	{
	    		//Could not find the file.
	    		return null; 
	    	}
	    	cachedStream = new CachedInputStream(inputStream);
	    	streamTable.put(fileName, cachedStream);
	    }
	    
//	    System.out.println(" - returnPath = "+fileName);
	    cachedStream.reset();
	    return new InputSource(cachedStream);
	}
}
