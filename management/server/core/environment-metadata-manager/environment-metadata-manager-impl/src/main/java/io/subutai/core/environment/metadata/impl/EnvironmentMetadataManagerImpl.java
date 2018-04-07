package io.subutai.core.environment.metadata.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.subutai.common.command.CommandException;
import io.subutai.common.environment.Environment;
import io.subutai.common.environment.EnvironmentNotFoundException;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.environment.metadata.api.EnvironmentMetadataManager;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.exception.TokenCreateException;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.hub.share.dto.environment.EnvironmentInfoDto;
import io.subutai.hub.share.event.Event;
import io.subutai.hub.share.json.JsonUtil;


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
            String peerId = container.getPeerId();
            String origin = String.format( "%s.%s.%s", peerId, containerId, environmentId );
            final String token = identityManager.issueJWTToken( origin );
            environmentManager.placeTokenToContainer( environmentId, containerIp, token );
        }
        catch ( HostNotFoundException | EnvironmentNotFoundException | CommandException e )
        {
            throw new TokenCreateException( e.getMessage() );
        }
    }


    @Override
    public EnvironmentInfoDto getEnvironmentInfoDto( final String environmentId )
    {
        Environment environment = environmentManager.getEnvironment( environmentId );
        final EnvironmentInfoDto result = new EnvironmentInfoDto();
        result.setName( environment.getName() );
        result.setSubnetCidr( environment.getSubnetCidr() );
        return result;
    }


    @Override
    public void pushEvent( final Event event )
    {
        try
        {
            LOG.debug( "Event received: {} {}", event, JsonUtil.toJson( event ) );
            LOG.debug( "OS: {}", event.getCustomMetaByKey( "OS" ) );
        }
        catch ( JsonProcessingException e )
        {
            LOG.error( e.getMessage(), e );
        }
        // TODO: send event to consumers
    }
}

