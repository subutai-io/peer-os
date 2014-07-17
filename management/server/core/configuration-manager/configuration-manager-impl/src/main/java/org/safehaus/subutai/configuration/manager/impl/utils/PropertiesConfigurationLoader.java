package org.safehaus.subutai.configuration.manager.impl.utils;


import java.io.File;

import org.safehaus.subutai.shared.protocol.Agent;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import com.google.gson.JsonObject;


/**
 * Created by bahadyr on 7/16/14.
 */
public class PropertiesConfigurationLoader implements ConfigurationLoader {

    //    String filename = "/home/bahadyr/Desktop/products/hadoop-1.2.1/conf/log4j.properties";


    // Maps configuration into Config object
    @Override
    public JsonObject getConfiguration( Agent agent, String configPathFilename ) {

        try {
            //TODO cat file from given agent, convert to required format, detect types and form a Config
            //TODO convert text to Properties object
            File configFile = new File( configPathFilename );
            PropertiesConfiguration configuration = new PropertiesConfiguration( configFile );
            //TODO properties to Config converter code
            //            return configuration;
            //            Object o = configuration.getProperty( "log4j.appender.DRFA.layout" );
            JsonObject jsonObject = new JsonObject();
        }
        catch ( ConfigurationException e ) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public void setConfiguration( final Agent agent, JsonObject config ) {
        //TODO Read config from instance, set values from Config, inject Config
    }
}
