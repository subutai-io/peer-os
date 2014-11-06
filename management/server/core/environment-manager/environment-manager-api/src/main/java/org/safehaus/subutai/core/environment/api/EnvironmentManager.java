/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.environment.api;


import java.util.List;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.protocol.EnvironmentBuildTask;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentBuildException;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentDestroyException;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentManagerException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.environment.api.helper.EnvironmentBuildProcess;
import org.safehaus.subutai.core.environment.api.topology.TopologyData;
import org.safehaus.subutai.core.peer.api.Peer;


/**
 *
 */
public interface EnvironmentManager
{

    Environment buildEnvironment( EnvironmentBlueprint blueprint ) throws EnvironmentBuildException;

    List<Environment> getEnvironments();

    Environment getEnvironment( String environmentName );

    boolean destroyEnvironment( UUID environmentId ) throws EnvironmentDestroyException;

    boolean saveBlueprint( String blueprint );

    List<EnvironmentBuildTask> getBlueprintTasks();

    List<EnvironmentBlueprint> getBlueprints();

    boolean deleteBlueprintTask( String name );

    boolean deleteBlueprint( UUID blueprintId );

    void saveEnvironment( final Environment environment );

    boolean saveBuildProcess( EnvironmentBuildProcess buildProgress );

    List<EnvironmentBuildProcess> getBuildProcesses();

    Environment buildEnvironment( EnvironmentBuildProcess environmentBuildProcess ) throws EnvironmentBuildException;

    public Environment buildEnvironmentNew( final EnvironmentBuildProcess process ) throws EnvironmentBuildException;

    void deleteBuildProcess( EnvironmentBuildProcess environmentBuildProcess );


    Environment getEnvironmentByUUID( UUID environmentId );

    /*boolean saveBuildProcess( UUID blueprintId, Map<Object, Peer> topology, Map<Object, NodeGroup> map,
                              TopologyEnum topologyEnum );*/


//    boolean saveBuildProcessB2PG( UUID blueprintId, UUID peerGroupId ) throws EnvironmentManagerException;

    boolean saveBuildProcess(TopologyData topologyData) throws EnvironmentManagerException;

    EnvironmentBlueprint getEnvironmentBlueprint( UUID blueprintId ) throws EnvironmentManagerException;

    void createAdditionalContainers( UUID id, String ngJson, Peer peer ) throws EnvironmentBuildException;

    //    void createEnvironment( EnvironmentBlueprint blueprint, Peer peer );

//    void createEnvironment( List<NodeData> nodeData );


}
