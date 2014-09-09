/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.environment.api;


import java.util.List;

import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.protocol.EnvironmentBuildTask;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentBuildException;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentDestroyException;
import org.safehaus.subutai.core.environment.api.helper.Environment;


/**
 *
 */
public interface EnvironmentManager {

    /**
     * Builds the environment with a given blueprint descriptor.
     */
    public boolean buildEnvironment( EnvironmentBuildTask environmentBuildTask );

//    public boolean buildEnvironment( EnvironmentBlueprint blueprint );

    public Environment buildEnvironmentAndReturn( EnvironmentBuildTask environmentBuildTask )
            throws EnvironmentBuildException;


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
    public boolean destroyEnvironment( String environmentName ) throws EnvironmentDestroyException;

    /**
     * Saves blueprint test into database
     */
    public boolean saveBlueprint( String bluepringStr );

    public List<EnvironmentBuildTask> getBlueprints();

    boolean deleteBlueprint( String name );

    public String parseBlueprint( EnvironmentBlueprint blueprint );
}
