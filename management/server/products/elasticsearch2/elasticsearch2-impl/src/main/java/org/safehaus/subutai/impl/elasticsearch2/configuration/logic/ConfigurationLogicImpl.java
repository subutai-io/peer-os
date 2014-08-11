package org.safehaus.subutai.impl.elasticsearch2.configuration.logic;


import org.safehaus.subutai.api.elasticsearch2.ConfigurationLogic;
import org.safehaus.subutai.configuration.manager.api.ConfigManager;
import org.safehaus.subutai.configuration.manager.api.ConfigTypeEnum;

import com.google.gson.JsonObject;


public class ConfigurationLogicImpl implements ConfigurationLogic {

    private ConfigManager configManager;


    public void setConfigManager( final ConfigManager configManager ) {
        this.configManager = configManager;
    }


    public ConfigManager getConfigManager() {
        return configManager;
    }


    public void doSomeTask() {
        configManager.injectConfiguration( null, null, null, null );
    }


    public void dst2() {

        JsonObject o = configManager.getConfiguration( null, "/conf/cassandra.yaml", ConfigTypeEnum.YAML );
    }
}
