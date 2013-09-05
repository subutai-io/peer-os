package org.safehaus.hadoop.hdfs.test;

import java.io.IOException;
import java.io.StringWriter;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hello world!
 *
 */
public class App 
{
		
    public static void main( String[] args ) throws IOException
    {
    	Logger logger = LoggerFactory.getLogger(App.class);
    	
        System.out.println( "Hello World!" );
        
    	Configuration myConf = new Configuration();
    	
    	myConf.addResource(new Path("/etc/hadoop/core-site.xml"));
    	myConf.addResource(new Path("/etc/hadoop/hdfs-site.xml"));
    	myConf.addResource(new Path("/etc/hadoop/mapred-site.xml"));
    	 
    	StringWriter writer = new StringWriter();
    	
    	Configuration.dumpConfiguration(myConf, writer);
    	
    	logger.debug("Hadoop Configuration: {}", writer);

    	FileSystem fileSystem = FileSystem.get(myConf);

    	Path homeDir = fileSystem.getHomeDirectory();
    	
    	System.out.println("Home Directory: " + homeDir);
    	
    	if (!fileSystem.exists(homeDir))
    	{	
    		logger.warn("Home directory {} does not exists.", homeDir);
    	}
    }
}
