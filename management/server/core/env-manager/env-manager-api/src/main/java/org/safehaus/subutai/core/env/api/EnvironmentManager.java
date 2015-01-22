package org.safehaus.subutai.core.env.api;


import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.common.protocol.PlacementStrategy;
import org.safehaus.subutai.core.env.api.build.NodeGroup;
import org.safehaus.subutai.core.env.api.build.Topology;
import org.safehaus.subutai.core.env.api.exception.EnvironmentCreationException;
import org.safehaus.subutai.core.env.api.exception.EnvironmentDestructionException;
import org.safehaus.subutai.core.env.api.exception.EnvironmentModificationException;
import org.safehaus.subutai.core.env.api.exception.EnvironmentNotFoundException;


/**
 * Environment Manager
 */
public interface EnvironmentManager
{

    public Set<Environment> getEnvironments();

    public Environment findEnvironment( UUID environmentId ) throws EnvironmentNotFoundException;

    public Environment createEnvironment( String name, Topology topology ) throws EnvironmentCreationException;


    public void destroyEnvironment( UUID environmentId )
            throws EnvironmentDestructionException, EnvironmentNotFoundException;

    public Environment growEnvironment( UUID environmentId, Topology topology )
            throws EnvironmentModificationException, EnvironmentNotFoundException;

    public void destroyContainer( ContainerHost containerHost )
            throws EnvironmentModificationException, EnvironmentNotFoundException;

    public NodeGroup newNodeGroup( final String name, final String templateName, final String domainName,
                                   final int numberOfNodes, final int sshGroupId, final int hostsGroupId,
                                   final PlacementStrategy nodePlacementStrategy );

    public Topology newTopology();

    /**
     * Removes environment from database only. Used to cleanup environment records.
     *
     * @param environmentId - environment id
     *
     * @throws EnvironmentNotFoundException - thrown if environment not found
     */
    public void removeEnvironment( UUID environmentId ) throws EnvironmentNotFoundException;
}
