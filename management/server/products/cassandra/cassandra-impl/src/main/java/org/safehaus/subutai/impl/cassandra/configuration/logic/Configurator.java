package org.safehaus.subutai.impl.cassandra.configuration.logic;


import org.safehaus.subutai.configuration.manager.api.ConfigManager;
import org.safehaus.subutai.configuration.manager.api.ConfigTypeEnum;


/**
 * Created by bahadyr on 7/17/14.
 */
public class Configurator {

    private ConfigManager configManager;


    public void setConfigManager( final ConfigManager configManager ) {
        this.configManager = configManager;
    }


    public void doSomeTask() {
        configManager.injectConfiguration( null,null,null );
    }

    public void dst2() {
        Object o = configManager.getConfiguration( "", ConfigTypeEnum.YAML );
    }
}
