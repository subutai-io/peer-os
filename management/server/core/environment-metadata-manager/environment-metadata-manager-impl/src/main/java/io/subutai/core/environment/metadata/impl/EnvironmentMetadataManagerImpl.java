package io.subutai.core.environment.metadata.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.bazaar.share.dto.environment.EnvironmentInfoDto;
import io.subutai.bazaar.share.events.Event;
import io.subutai.common.command.CommandException;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.environment.Environment;
import io.subutai.common.environment.EnvironmentNotFoundException;
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
    public EnvironmentInfoDto getEnvironmentInfoDto( final String environmentId )
    {
        final EnvironmentInfoDto result = new EnvironmentInfoDto();
        try
        {
            Environment environment = environmentManager.loadEnvironment( environmentId );
            result.setName( environment.getName() );
            result.setSubnetCidr( environment.getSubnetCidr() );
        }
        catch ( EnvironmentNotFoundException e )
        {
            // ignore
        }
        return result;
    }


    @Override
    public void pushEvent( final Event event )
    {
        LOG.debug( "Event received: {}", event );
    }


    private void placeTokenIntoContainer( ContainerHost containerHost, String token ) throws CommandException
    {
        containerHost.executeAsync( new RequestBuilder(
                String.format( "mkdir -p /etc/subutai/ ; echo '%s' > /etc/subutai/jwttoken", token ) ) );
    }
}

