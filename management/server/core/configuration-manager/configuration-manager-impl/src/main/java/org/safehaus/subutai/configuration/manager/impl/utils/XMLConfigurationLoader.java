package org.safehaus.subutai.configuration.manager.impl.utils;


import org.safehaus.subutai.configuration.manager.api.Config;
import org.safehaus.subutai.configuration.manager.api.ConfigTypeEnum;
import org.safehaus.subutai.shared.protocol.Agent;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;


/**
 * Created by bahadyr on 7/16/14.
 */
public class XMLConfigurationLoader implements ConfigurationLoader {

    //            String xmlConfig = "/home/bahadyr/Desktop/products/hadoop-1.2.1/conf/core-site.xml";


    @Override
    public Config getConfiguration( Agent agent, String configPathFilename ) {
        try {
            //TODO cat file from given agent, convert to required format, detect types and form a Config
            //TODO convert text to XML object
            XMLConfiguration xmlConfiguration = new XMLConfiguration( configPathFilename );
            //TODO XML to Config converter
            Config config = new Config();
            config.setPath( configPathFilename );
            config.setConfigTypeEnum( ConfigTypeEnum.XML );
        }
        catch ( ConfigurationException e ) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public void setConfiguration( final Agent agent, Config config ) {
        //TODO Read config from instance, set values from Config, inject Config
    }
}
