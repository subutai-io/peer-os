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
import org.safehaus.subutai.configuration.manager.impl.utils.PlainParser;
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
    public boolean injectConfiguration( Agent agent, String configFilePath, String jsonObjectConfig,
                                        ConfigTypeEnum configTypeEnum ) {

        //TODO echo to given agent
        ConfigurationLoader configurationLoader = null;

        String type = "";
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
            case PLAIN:{
                break;
            }
        }
        boolean result = configurationLoader.setConfiguration( agent, configFilePath, jsonObjectConfig );

        return result;
    }


    @Override
    public String getProperty( final JsonObject config, final String path, ConfigTypeEnum configTypeEnum ) {
        /*ConfigParser configParser = null;
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
            configParser.getProperty( path );
        }
        catch ( ConfigurationException e ) {
            e.printStackTrace();
        }*/
        return null;
    }


    @Override
    public void setProperty( final JsonObject config, final String path, final String value,
                             ConfigTypeEnum configTypeEnum ) {
        /*ConfigParser configParser = null;
//        String content = FileUtil.getContent(configPathFilename , this ); try {
            switch ( configTypeEnum ) {
                case YAML: {
                    configParser = new XmlParser( config );
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
            configParser.setProperty( path, value );
        }
        catch ( ConfigurationException e ) {
            e.printStackTrace();
        }*/
    }


    @Override
    public JsonObject getConfiguration( Agent agent, String configPathFilename, ConfigTypeEnum configTypeEnum ) {

        ConfigurationLoader loader = null;
        switch ( configTypeEnum ) {
            case YAML: {
                loader = new YamConfigurationlLoader();
                break;
            }
            case PROPERTIES: {
                loader = new PropertiesConfigurationLoader();
                break;
            }
            case XML: {
                loader = new XMLConfigurationLoader();
                break;
            }
            case PLAIN: {
                break;
            }
        }

        return loader.getConfiguration( agent, configPathFilename );
    }


    @Override
    public JsonObject getJsonObjectFromResources( final String configPathFilename, final ConfigTypeEnum configTypeEnum ) {
        ConfigParser parser = null;
        String content = FileUtil.getContent( configPathFilename, this );
        try {
            switch ( configTypeEnum ) {
                case YAML: {
                    parser = new XmlParser( content );
                    break;
                }
                case PROPERTIES: {
                    parser = new IniParser( content );
                    break;
                }
                case XML: {
                    parser = new XmlParser( content );
                    break;
                }
                case PLAIN: {
                    parser = new PlainParser( content );
                    break;
                }
            }
            return parser.parserConfig( configPathFilename, configTypeEnum );
        }
        catch ( ConfigurationException e ) {
            e.printStackTrace();
        }
        return null;
    }
}
