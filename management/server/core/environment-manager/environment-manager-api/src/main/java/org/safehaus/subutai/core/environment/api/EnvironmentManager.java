/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.environment.api;


import java.util.List;
import java.util.UUID;

import org.safehaus.subutai.common.peer.Peer;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.protocol.EnvironmentBuildTask;
import org.safehaus.subutai.common.protocol.NodeGroup;
import org.safehaus.subutai.common.protocol.PlacementStrategy;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentBuildException;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentDestroyException;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentManagerException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.environment.api.helper.EnvironmentBuildProcess;
import org.safehaus.subutai.core.environment.api.topology.TopologyData;
import org.safehaus.subutai.core.peer.api.ResourceHost;


/**
 *
 */
public interface EnvironmentManager
{

    public Environment buildEnvironment( EnvironmentBlueprint blueprint ) throws EnvironmentBuildException;

    public List<Environment> getEnvironments();

    public Environment findEnvironment( String environmentId ) throws EnvironmentManagerException;

    public void destroyEnvironment( UUID environmentId ) throws EnvironmentDestroyException;

    public UUID saveBlueprint( String blueprint ) throws EnvironmentManagerException;

    public List<EnvironmentBlueprint> getBlueprints();

    public void deleteBlueprint( UUID blueprintId ) throws EnvironmentManagerException;

    public void saveEnvironment( final Environment environment );

    public void saveBuildProcess( EnvironmentBuildProcess buildProgress ) throws EnvironmentManagerException;

    public List<EnvironmentBuildProcess> getBuildProcesses();

    public Environment buildEnvironment( final EnvironmentBuildProcess process ) throws EnvironmentBuildException;

    public void deleteBuildProcess( EnvironmentBuildProcess environmentBuildProcess )
            throws EnvironmentManagerException;

    public Environment findEnvironmentByID( UUID environmentId ) throws EnvironmentManagerException;

    public UUID saveBuildProcess( TopologyData topologyData ) throws EnvironmentManagerException;

    public EnvironmentBlueprint getEnvironmentBlueprint( UUID blueprintId ) throws EnvironmentManagerException;

    public void createAdditionalContainers( UUID id, NodeGroup nodeGroup, Peer peer ) throws EnvironmentBuildException;

    public void destroyContainer( UUID containerId ) throws EnvironmentManagerException;


    // ************** deprecated methods **************

    @Deprecated
    public void createAdditionalContainers( UUID id, String ngJson, Peer peer ) throws EnvironmentBuildException;

    @Deprecated
    Environment getEnvironmentByUUID( UUID environmentId );

    @Deprecated
    Environment getEnvironment( String environmentId );

    @Deprecated
    List<EnvironmentBuildTask> getBlueprintTasks();

    @Deprecated
    boolean deleteBlueprintTask( String name );


    @Deprecated
    public UUID addContainer( final UUID environmentId, final String template, PlacementStrategy strategy,
                              String nodeGroupName, final Peer peer ) throws EnvironmentManagerException;

    @Deprecated
    void removeContainer( UUID environmentId, UUID hostId ) throws EnvironmentManagerException;

    @Deprecated

    public void createLocalContainer( final Environment environment, final String templateName,
                                      final String nodeGroupName, ResourceHost resourceHost )
            throws EnvironmentBuildException;
}
