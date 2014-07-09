/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.configuration.manager.impl;


import org.safehaus.subutai.configuration.manager.api.ConfigManager;
import org.safehaus.subutai.configuration.manager.impl.utils.CCLoader;
import org.safehaus.subutai.shared.protocol.Agent;

import org.apache.cassandra.config.Config;
import org.apache.cassandra.exceptions.ConfigurationException;


/**
 * This is an implementation of LxcManager
 */
public class ConfigManagerImpl implements ConfigManager {


    @Override
    public void injectConfiguration( final Object conf, final String path, final Agent agent ) {


    }


    @Override
    public Config getCassandraConfig() {
        CCLoader c = new CCLoader();
        Config o = null;
        try {
            o = c.loadConfig();
        }
        catch ( ConfigurationException e ) {
            e.printStackTrace();
        }
        return o;
    }
}
