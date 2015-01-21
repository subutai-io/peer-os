package org.safehaus.subutai.core.env.impl;


import java.util.UUID;

import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;
import org.safehaus.subutai.core.env.api.EnvironmentManager;
import org.safehaus.subutai.core.env.api.build.Topology;
import org.safehaus.subutai.core.env.api.exception.EnvironmentCreationException;
import org.safehaus.subutai.core.env.api.exception.EnvironmentDestructionException;
import org.safehaus.subutai.core.env.api.exception.EnvironmentModificationException;
import org.safehaus.subutai.core.env.impl.builder.EnvironmentBuilder;
import org.safehaus.subutai.core.env.impl.exception.EnvironmentBuildException;

import com.google.common.base.Preconditions;


/**
 * Environment manager implementation
 */
public class EnvironmentManagerImpl implements EnvironmentManager
{

    private final TemplateRegistry templateRegistry;
    private final PeerManager peerManager;
    private final EnvironmentBuilder environmentBuilder;


    public EnvironmentManagerImpl( final TemplateRegistry templateRegistry, final PeerManager peerManager )
    {
        Preconditions.checkNotNull( templateRegistry );
        Preconditions.checkNotNull( peerManager );

        this.templateRegistry = templateRegistry;
        this.peerManager = peerManager;
        this.environmentBuilder = new EnvironmentBuilder( templateRegistry, peerManager );
    }


    @Override
    public void createEnvironment( final Topology topology ) throws EnvironmentCreationException
    {
        Preconditions.checkNotNull( topology, "Invalid topology" );
        Preconditions.checkArgument( !topology.getNodeGroupPlacement().isEmpty(), "Placement is empty" );

        try
        {
            environmentBuilder.build( topology );
        }
        catch ( EnvironmentBuildException e )
        {
            throw new EnvironmentCreationException( e );
        }
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
