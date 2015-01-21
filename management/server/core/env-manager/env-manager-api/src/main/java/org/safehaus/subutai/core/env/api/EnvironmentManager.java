package org.safehaus.subutai.core.env.api;


import java.util.UUID;

import org.safehaus.subutai.common.protocol.PlacementStrategy;
import org.safehaus.subutai.core.env.api.build.NodeGroup;
import org.safehaus.subutai.core.env.api.build.Topology;
import org.safehaus.subutai.core.env.api.exception.EnvironmentCreationException;
import org.safehaus.subutai.core.env.api.exception.EnvironmentDestructionException;
import org.safehaus.subutai.core.env.api.exception.EnvironmentModificationException;


/**
 * Environment Manager
 */
public interface EnvironmentManager
{
    public Environment createEnvironment( String name, Topology topology ) throws EnvironmentCreationException;

    public NodeGroup newNodeGroup( final String name, final String templateName, final String domainName,
                                   final int numberOfNodes, final int sshGroupId, final int hostsGroupId,
                                   final PlacementStrategy nodePlacementStrategy );

    public void destroyEnvironment( UUID environmentId ) throws EnvironmentDestructionException;

    public void growEnvironment( UUID environmentId, Topology topology ) throws EnvironmentModificationException;

    public void destroyContainer( UUID containerId ) throws EnvironmentModificationException;

    public Topology newTopology();
}
