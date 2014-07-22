/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.configuration.manager.impl;


import org.safehaus.subutai.configuration.manager.api.ConfigManager;
import org.safehaus.subutai.configuration.manager.api.ConfigTypeEnum;
import org.safehaus.subutai.configuration.manager.impl.loaders.ConfigurationLoader;
import org.safehaus.subutai.configuration.manager.impl.loaders.PropertiesConfigurationLoader;
import org.safehaus.subutai.configuration.manager.impl.loaders.XMLConfigurationLoader;
import org.safehaus.subutai.configuration.manager.impl.loaders.YamConfigurationlLoader;
import org.safehaus.subutai.configuration.manager.impl.utils.ConfigParser;
import org.safehaus.subutai.configuration.manager.impl.utils.IniParser;
import org.safehaus.subutai.configuration.manager.impl.utils.XmlParser;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.FileUtil;

import org.apache.commons.configuration.ConfigurationException;

import com.google.gson.JsonObject;


/**
 * This is an implementation of LxcManager
 */
public class ConfigManagerImpl implements ConfigManager {

    @Override
    public void injectConfiguration( Agent agent, JsonObject config ) {

        //TODO echo to given agent
        ConfigurationLoader configurationLoader = null;

        String type = "";
        switch ( type ) {
            case "YAML": {
                configurationLoader = new YamConfigurationlLoader();
                break;
            }
            case "PROPERTIES": {
                configurationLoader = new PropertiesConfigurationLoader();
                break;
            }
            case "XML": {
                configurationLoader = new XMLConfigurationLoader();
                break;
            }
        }
        configurationLoader.setConfiguration( agent, config );
    }


    @Override
    public String getProperty( final JsonObject config, final String path ) {
        return null;
    }


    @Override
    public void setProperty( final JsonObject config, final String path, final String value ) {

    }


    @Override
    public JsonObject getConfiguration( Agent agent, String configPathFilename, ConfigTypeEnum configTypeEnum ) {

        ConfigurationLoader configurationLoader = null;
        switch ( configTypeEnum ) {
            case YAML: {
                configurationLoader = new YamConfigurationlLoader();
                break;
            }
            case PROPERTIES: {
                configurationLoader = new PropertiesConfigurationLoader();
                break;
            }
            case XML: {
                configurationLoader = new XMLConfigurationLoader();
                break;
            }
        }

        return configurationLoader.getConfiguration( agent, configPathFilename );
    }


    @Override
    public JsonObject getConfigurationJson( final String configPathFilename, final ConfigTypeEnum configTypeEnum ) {
        ConfigParser configParser = null;
        String content = FileUtil.getContent( configPathFilename, this );
        try {
            switch ( configTypeEnum ) {
                case YAML: {
                    configParser = new XmlParser( content );
                    break;
                }
                case PROPERTIES: {
                    configParser = new IniParser( content );
                    break;
                }
                case XML: {
                    configParser = new XmlParser( content );
                    break;
                }
            }
            return configParser.parserConfig( configPathFilename, configTypeEnum );
        }
        catch ( ConfigurationException e ) {
            e.printStackTrace();
        }
        return null;
    }
}
