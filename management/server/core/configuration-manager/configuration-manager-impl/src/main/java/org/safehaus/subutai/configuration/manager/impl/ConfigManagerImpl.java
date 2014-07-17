/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.configuration.manager.impl;


import org.safehaus.subutai.configuration.manager.api.ConfigManager;
import org.safehaus.subutai.configuration.manager.api.ConfigTypeEnum;
import org.safehaus.subutai.configuration.manager.impl.utils.ConfigurationLoader;
import org.safehaus.subutai.configuration.manager.impl.utils.PropertiesConfigurationLoader;
import org.safehaus.subutai.configuration.manager.impl.utils.XMLConfigurationLoader;
import org.safehaus.subutai.configuration.manager.impl.utils.YamConfigurationlLoader;
import org.safehaus.subutai.shared.protocol.Agent;

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
}
