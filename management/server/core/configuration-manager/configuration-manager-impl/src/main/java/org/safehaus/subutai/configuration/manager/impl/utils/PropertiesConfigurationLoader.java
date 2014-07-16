package org.safehaus.subutai.configuration.manager.impl.utils;


import java.io.File;
import java.io.InputStream;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;


/**
 * Created by bahadyr on 7/16/14.
 */
public class PropertiesConfigurationLoader implements ConfigurationLoader {

    String filename = "/home/bahadyr/Desktop/products/hadoop-1.2.1/conf/log4j.properties";


    public Object loadConfig( InputStream in ) {
        try {
            File configFile = new File( filename );
            PropertiesConfiguration configuration = new PropertiesConfiguration( configFile );
            return configuration;
//            Object o = configuration.getProperty( "log4j.appender.DRFA.layout" );
        }
        catch ( ConfigurationException e ) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public Object getConfiguration() {
        return null;
    }
}
