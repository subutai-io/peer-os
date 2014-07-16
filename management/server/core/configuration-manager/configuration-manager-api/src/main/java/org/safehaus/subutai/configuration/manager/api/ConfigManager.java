/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.configuration.manager.api;


import org.safehaus.subutai.shared.protocol.Agent;


/**
 *
 */
public interface ConfigManager {

    public void injectConfiguration(Agent agent, Config config);

    public Config getConfiguration(Agent agent, String configPathFilename, ConfigTypeEnum configTypeEnum);

}
