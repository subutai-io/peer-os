package org.safehaus.subutai.impl.cassandra.configuration.logic;


import org.safehaus.subutai.api.cassandra.ConfigurationLogic;
import org.safehaus.subutai.core.configuration.api.ConfigManager;
import org.safehaus.subutai.core.configuration.api.ConfigTypeEnum;

import com.google.gson.JsonObject;


/**
 * Created by bahadyr on 7/17/14.
 */
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
