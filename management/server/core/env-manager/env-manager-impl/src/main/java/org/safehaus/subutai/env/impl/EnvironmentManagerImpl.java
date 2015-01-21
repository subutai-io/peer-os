package org.safehaus.subutai.env.impl;


import java.util.UUID;

import org.safehaus.subutai.env.api.EnvironmentManager;
import org.safehaus.subutai.env.api.build.Topology;
import org.safehaus.subutai.env.api.exception.EnvironmentCreationException;
import org.safehaus.subutai.env.api.exception.EnvironmentDestructionException;
import org.safehaus.subutai.env.api.exception.EnvironmentModificationException;

import com.google.common.base.Preconditions;


/**
 * Environment manager implementation
 */
public class EnvironmentManagerImpl implements EnvironmentManager
{
    @Override
    public void createEnvironment( final Topology topology ) throws EnvironmentCreationException
    {
        Preconditions.checkNotNull( topology, "Invalid topology" );
        Preconditions.checkArgument( !topology.getNodeGroupPlacement().isEmpty(), "Placement is empty" );
    }


    @Override
    public void destroyEnvironment( final UUID environmentId ) throws EnvironmentDestructionException
    {
        Preconditions.checkNotNull( environmentId, "Invalid environment id" );
    }


    @Override
    public void growEnvironment( final UUID environmentId, final Topology topology )
            throws EnvironmentModificationException
    {
        Preconditions.checkNotNull( environmentId, "Invalid environment id" );
        Preconditions.checkNotNull( topology, "Invalid topology" );
        Preconditions.checkArgument( !topology.getNodeGroupPlacement().isEmpty(), "Placement is empty" );
    }


    @Override
    public void destroyContainer( final UUID containerId ) throws EnvironmentModificationException
    {
        Preconditions.checkNotNull( containerId, "Invalid container id" );
    }
}
