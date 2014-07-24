/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.api.manager;


import java.util.List;

import org.safehaus.subutai.api.manager.exception.EnvironmentBuildException;
import org.safehaus.subutai.api.manager.helper.Environment;
import org.safehaus.subutai.api.manager.helper.EnvironmentBlueprint;


/**
 *
 */
public interface EnvironmentManager {

    /**
     * Builds the environment with a given blueprint descriptor.
     */
    public boolean buildEnvironment( String blueprintStr );

    public boolean buildEnvironment( EnvironmentBlueprint blueprint );

    public Environment buildEnvironmentAndReturn( EnvironmentBlueprint blueprint ) throws EnvironmentBuildException;


    /**
     * Returns the set of existing environments.
     */
    public List<Environment> getEnvironments();

    /**
     * Gets the environment by given environment name.
     */
    public Environment getEnvironmentInfo( String environmentName );

    /**
     * Destroys environment by a given environmentt name.
     */
    public boolean destroyEnvironment( String environmentName );

    /**
     * Saves blueprint test into database
     */
    public boolean saveBlueprint( String bluepringStr );

    public List<EnvironmentBlueprint> getBlueprints();

    boolean deleteBlueprint( String name );
}
