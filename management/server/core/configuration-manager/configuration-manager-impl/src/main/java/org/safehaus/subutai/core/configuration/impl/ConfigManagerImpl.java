/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.configuration.impl;


import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.core.configuration.api.ConfigManager;
import org.safehaus.subutai.core.configuration.api.ConfigTypeEnum;
import org.safehaus.subutai.core.configuration.api.TextInjector;
import org.safehaus.subutai.core.configuration.impl.loaders.ConfigurationLoader;
import org.safehaus.subutai.core.configuration.impl.loaders.PlainConfigurationLoader;
import org.safehaus.subutai.core.configuration.impl.loaders.PropertiesConfigurationLoader;
import org.safehaus.subutai.core.configuration.impl.loaders.ShellConfigurationLoader;
import org.safehaus.subutai.core.configuration.impl.loaders.XMLConfigurationLoader;
import org.safehaus.subutai.core.configuration.impl.loaders.YamConfigurationlLoader;
import org.safehaus.subutai.core.configuration.impl.utils.ConfigParser;
import org.safehaus.subutai.core.configuration.impl.utils.IniParser;
import org.safehaus.subutai.core.configuration.impl.utils.PlainParser;
import org.safehaus.subutai.core.configuration.impl.utils.ShellParser;
import org.safehaus.subutai.core.configuration.impl.utils.XmlParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.configuration.ConfigurationException;

import com.google.gson.JsonObject;


/**
 * This is an implementation of LxcManager
 */
public class ConfigManagerImpl implements ConfigManager
{

    private static final Logger LOG = LoggerFactory.getLogger( ConfigManagerImpl.class.getName() );
    private TextInjector textInjector;


    public TextInjector getTextInjector()
    {
        return textInjector;
    }


    public void setTextInjector( final TextInjector textInjector )
    {
        this.textInjector = textInjector;
    }


    @Override
    public boolean injectConfiguration( String hostname, String configFilePath, String jsonObjectConfig,
                                        ConfigTypeEnum configTypeEnum )
    {

        //TODO echo to given agent
        ConfigurationLoader configurationLoader = null;

        switch ( configTypeEnum )
        {
            case YAML:
            {
                configurationLoader = new YamConfigurationlLoader( textInjector );
                break;
            }
            case PROPERTIES:
            {
                configurationLoader = new PropertiesConfigurationLoader( textInjector );
                break;
            }
            case XML:
            {
                configurationLoader = new XMLConfigurationLoader( textInjector );
                break;
            }
            case PLAIN:
            {
                //TODO
                configurationLoader = new PlainConfigurationLoader( textInjector );
                break;
            }
            case SH:
            {
                //TODO
                configurationLoader = new ShellConfigurationLoader( textInjector );
                break;
            }
            default:
                break;
        }
        if ( configurationLoader == null )
        {
            return false;
        }
        return configurationLoader.setConfiguration( hostname, configFilePath, jsonObjectConfig );
    }


    @Override
    public JsonObject getConfiguration( String agentHostname, String configPathFilename, ConfigTypeEnum configTypeEnum )
    {
        ConfigurationLoader loader = null;
        switch ( configTypeEnum )
        {
            case YAML:
            {
                loader = new YamConfigurationlLoader( textInjector );
                break;
            }
            case PROPERTIES:
            {
                loader = new PropertiesConfigurationLoader( textInjector );
                break;
            }
            case XML:
            {
                loader = new XMLConfigurationLoader( textInjector );
                break;
            }
            case PLAIN:
            {
                //TODO
                loader = new PlainConfigurationLoader( textInjector );
                break;
            }
            case SH:
            {
                //TODO
                loader = new ShellConfigurationLoader( textInjector );
                break;
            }
            default:
                break;
        }

        if ( loader != null )
        {
            return loader.getConfiguration( agentHostname, configPathFilename );
        }

        return null;
    }


    @Override
    public String getProperty( JsonObject config, String path, ConfigTypeEnum configTypeEnum )
    {
        // TODO complete setProperty function
        return null;
    }


    @Override
    public void setProperty( JsonObject config, String path, String value, ConfigTypeEnum configTypeEnum )
    {
        //TODO setProperty function is empty
    }


    @Override
    public JsonObject getJsonObjectFromResources( String configPathFilename, ConfigTypeEnum configTypeEnum )
    {
        String content = FileUtil.getContent( configPathFilename, this );
        try
        {
            ConfigParser parser = null;
            switch ( configTypeEnum )
            {
                case YAML:
                {
                    parser = new XmlParser( content );
                    break;
                }
                case PROPERTIES:
                {
                    parser = new IniParser( content );
                    break;
                }
                case XML:
                {
                    parser = new XmlParser( content );
                    break;
                }
                case PLAIN:
                {
                    parser = new PlainParser( content );
                    break;
                }
                case SH:
                {
                    parser = new ShellParser( content );
                    break;
                }
                default:
                    break;
            }
            if ( parser != null )
            {
                return parser.parserConfig( configPathFilename, configTypeEnum );
            }
        }
        catch ( ConfigurationException e )
        {
            LOG.info( "ConfigManagerImpl@getJsonObjectFromResources: " + e.getMessage() );
        }
        return null;
    }
}
