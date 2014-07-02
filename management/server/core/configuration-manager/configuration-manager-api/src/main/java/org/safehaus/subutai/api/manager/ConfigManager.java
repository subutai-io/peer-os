/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.api.manager;


import java.util.List;

import org.safehaus.subutai.shared.protocol.Agent;


/**
 *
 */
public interface ConfigManager {

    public void injectRegularConfiguration(Object conf, String path, Agent agent);
}
