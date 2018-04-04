package io.subutai.core.environment.metadata.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.command.CommandException;
import io.subutai.common.environment.EnvironmentNotFoundException;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.environment.metadata.api.EnvironmentMetadataManager;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.exception.TokenCreateException;
import io.subutai.core.peer.api.PeerManager;


/**
 **/
public class EnvironmentMetadataManagerImpl implements EnvironmentMetadataManager
{
    private static final Logger LOG = LoggerFactory.getLogger( EnvironmentMetadataManagerImpl.class );
    private final IdentityManager identityManager;
    private PeerManager peerManager;
    private EnvironmentManager environmentManager;


    public EnvironmentMetadataManagerImpl( PeerManager peerManager, EnvironmentManager environmentManager,
                                           IdentityManager identityManager )
    {
        this.peerManager = peerManager;
        this.environmentManager = environmentManager;
        this.identityManager = identityManager;
    }


    @Override
    public void init()
    {
    }


    @Override
    public void dispose()
    {
    }


    @Override
    public void issueToken( String containerIp ) throws TokenCreateException
    {
        try
        {
            ContainerHost container = peerManager.getLocalPeer().getContainerHostByIp( containerIp );
            String environmentId = container.getEnvironmentId().getId();
            String containerId = container.getContainerId().getId();
            final String token = identityManager.issueJWTToken( environmentId, containerId );
            environmentManager.placeTokenToContainer( environmentId, containerIp, token );
        }
        catch ( HostNotFoundException | EnvironmentNotFoundException | CommandException e )
        {
            throw new TokenCreateException( e.getMessage() );
        }
    }
}

