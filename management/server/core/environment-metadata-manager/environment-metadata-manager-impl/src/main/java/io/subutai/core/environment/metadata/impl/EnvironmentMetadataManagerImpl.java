package io.subutai.core.environment.metadata.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.bazaar.share.common.BazaaarAdapter;
import io.subutai.bazaar.share.event.payload.Payload;
import io.subutai.common.command.CommandException;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.host.SubutaiOrigin;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.environment.metadata.api.EnvironmentMetadataManager;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.exception.TokenCreateException;
import io.subutai.core.peer.api.PeerManager;


/**
 * Environment metadata manager
 **/
public class EnvironmentMetadataManagerImpl implements EnvironmentMetadataManager
{
    private static final Logger LOG = LoggerFactory.getLogger( EnvironmentMetadataManagerImpl.class );
    private final IdentityManager identityManager;
    private PeerManager peerManager;
    private EnvironmentManager environmentManager;
    private BazaaarAdapter bazaarAdapter;


    public EnvironmentMetadataManagerImpl( PeerManager peerManager, EnvironmentManager environmentManager,
                                           IdentityManager identityManager, BazaaarAdapter bazaaarAdapter )
    {
        this.peerManager = peerManager;
        this.environmentManager = environmentManager;
        this.identityManager = identityManager;
        this.bazaarAdapter = bazaaarAdapter;
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
            String peerId = container.getPeerId();
            final String token =
                    identityManager.issueJWTToken( new SubutaiOrigin( environmentId, peerId, containerId ) );

            placeTokenIntoContainer( container, token );
        }
        catch ( HostNotFoundException | CommandException e )
        {
            throw new TokenCreateException( e.getMessage() );
        }
    }


    @Override
    public Payload getEnvironmentInfoDto( final String environmentId, final String type )
    {
        return bazaarAdapter.getMetaData( environmentId, type );
    }


    @Override
    public void pushEvent( final Payload eventMessage )
    {
        LOG.debug( "Event received: {}", eventMessage );
        bazaarAdapter.pushEvent( eventMessage );
    }


    private void placeTokenIntoContainer( ContainerHost containerHost, String token ) throws CommandException
    {
        containerHost.executeAsync( new RequestBuilder(
                String.format( "mkdir -p /etc/subutai/ ; echo '%s' > /etc/subutai/jwttoken", token ) ) );
    }
}

