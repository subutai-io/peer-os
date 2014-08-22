package org.safehaus.subutai.configuration.manager.impl.loaders;


import org.safehaus.subutai.configuration.manager.api.TextInjector;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;

import com.google.gson.JsonObject;


/**
 * Created by bahadyr on 7/16/14.
 */
public class XMLConfigurationLoader implements ConfigurationLoader {

    //            String xmlConfig = "/home/bahadyr/Desktop/products/hadoop-1.2.1/conf/core-site.xml";

    private TextInjector textInjector;


    public XMLConfigurationLoader( final TextInjector textInjector ) {
        this.textInjector = textInjector;
    }


    @Override
    public JsonObject getConfiguration( String hostname, String configPathFilename ) {
        try {
            //TODO cat file from given agent, convert to required format, detect types and form a Config
            //TODO convert text to XML object
            XMLConfiguration xmlConfiguration = new XMLConfiguration( configPathFilename );
            //TODO XML to Config converter
            JsonObject jsonObject = new JsonObject();
        }
        catch ( ConfigurationException e ) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public boolean setConfiguration( String hostname, String configFilePath,  String config ) {
        //TODO Read config from instance, set values from Config, inject Config
        return true;
    }


}
