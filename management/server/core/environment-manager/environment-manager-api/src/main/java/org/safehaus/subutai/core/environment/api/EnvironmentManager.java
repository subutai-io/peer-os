/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.environment.api;


import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.EnvironmentBuildTask;
import org.safehaus.subutai.common.protocol.PeerCommandMessage;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentBuildException;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentDestroyException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.environment.api.helper.EnvironmentBuildProcess;
import org.safehaus.subutai.core.environment.api.helper.EnvironmentContainer;


/**
 *
 */
public interface EnvironmentManager
{

    Environment buildEnvironment( EnvironmentBuildTask environmentBuildTask ) throws EnvironmentBuildException;


    /**
     * Returns the set of existing environments.
     */
    List<Environment> getEnvironments();

    /**
     * Gets the environment by given environment name.
     */
    Environment getEnvironment( String environmentName );

    /**
     * Destroys environment by a given environment name.
     */
    boolean destroyEnvironment( String environmentName ) throws EnvironmentDestroyException;

    /**
     * Saves blueprint test into database
     */
    boolean saveBlueprint( String blueprint );

    List<EnvironmentBuildTask> getBlueprints();

    boolean deleteBlueprint( String name );

    void saveEnvironment( final Environment environment );

    boolean saveBuildProcess( EnvironmentBuildProcess buildProgress );

    List<EnvironmentBuildProcess> getBuildProcesses();

    void buildEnvironment( EnvironmentBuildProcess environmentBuildProcess ) throws EnvironmentBuildException;

    void deleteBuildProcess( EnvironmentBuildProcess environmentBuildProcess );

    void invoke( PeerCommandMessage commandMessage );

    void invoke( PeerCommandMessage commandMessage, long timeout );

    Set<EnvironmentContainer> getConnectedContainers( Environment environment );

    Environment getEnvironmentByUUID( UUID environmentId );



}
