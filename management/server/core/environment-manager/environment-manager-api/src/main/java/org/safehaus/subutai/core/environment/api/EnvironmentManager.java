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
import org.safehaus.subutai.core.environment.api.helper.EnvironmentBuildProcess;


/**
 *
 */
public interface EnvironmentManager
{

    /**
     * Builds the environment with a given blueprint descriptor.
     */
    boolean buildEnvironment( EnvironmentBuildTask environmentBuildTask );

    //    public boolean buildEnvironment( EnvironmentBlueprint blueprint );

    Environment buildEnvironmentAndReturn( EnvironmentBuildTask environmentBuildTask ) throws EnvironmentBuildException;


    /**
     * Returns the set of existing environments.
     */
    List<Environment> getEnvironments();

    /**
     * Gets the environment by given environment name.
     */
    Environment getEnvironmentInfo( String environmentName );

    /**
     * Destroys environment by a given environmentt name.
     */
    boolean destroyEnvironment( String environmentName ) throws EnvironmentDestroyException;

    /**
     * Saves blueprint test into database
     */
    boolean saveBlueprint( String bluepringStr );

    List<EnvironmentBuildTask> getBlueprints();

    boolean deleteBlueprint( String name );

    String parseBlueprint( EnvironmentBlueprint blueprint );

    boolean saveBuildProcess( EnvironmentBuildProcess buildProgress );

    List<EnvironmentBuildProcess> getBuildProcesses();

    void saveEnvironment( Environment environment );

    void buildEnvironment( EnvironmentBuildProcess environmentBuildProcess );

    void deleteBuildProcess( EnvironmentBuildProcess environmentBuildProcess );
}
