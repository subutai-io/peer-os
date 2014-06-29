/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.api.manager;


import java.util.List;
import java.util.Set;

import org.safehaus.subutai.api.manager.helper.Environment;


/**
 *
 */
public interface EnvironmentManager {

    /**
     * Builds the environment with a given blueprint descriptor.
     * @param blueprintStr
     * @return
     */
    public boolean buildEnvironment(String blueprintStr);

    /**
     * Returns the set of existing environments.
     * @return
     */
    public List<Environment> getEnvironments();

    /**
     * Gets the environment by given environment name.
     * @param environmentName
     * @return
     */
    public Environment getEnvironmentInfo(String environmentName);

    /**
     * Destroys environment by a given environmentt name.
     * @param environmentName
     * @return
     */
    public boolean destroyEnvironment(String environmentName);



}
