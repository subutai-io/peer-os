package io.subutai.core.environment.metadata.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.environment.Environment;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.environment.metadata.api.EnvironmentMetadataManager;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.exception.TokenCreateException;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.hub.share.Utils;
import io.subutai.hub.share.dto.environment.EnvironmentInfoDto;
import io.subutai.hub.share.event.Event;
import io.subutai.hub.share.json.JsonUtil;


/**
 * Environment metadata manager
 **/
public class EnvironmentMetadataManagerImpl implements EnvironmentMetadataManager
{
    private static final Logger LOG = LoggerFactory.getLogger( EnvironmentMetadataManagerImpl.class );
    private final IdentityManager identityManager;
    private PeerManager peerManager;
    private EnvironmentManager environmentManager;
    private String brokerURL;


    public EnvironmentMetadataManagerImpl( PeerManager peerManager, EnvironmentManager environmentManager,
                                           IdentityManager identityManager, final String brokerURL )
    {
        this.peerManager = peerManager;
        this.environmentManager = environmentManager;
        this.identityManager = identityManager;
        this.brokerURL = brokerURL;
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
            String origin = Utils.buildSubutaiOrigin( environmentId, peerId, containerId );
            final String token = identityManager.issueJWTToken( origin );

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
            String jsonEvent = JsonUtil.toJson( event );
            LOG.debug( "Event received: {} {}", event, jsonEvent );
            LOG.debug( "OS: {}", event.getCustomMetaByKey( "OS" ) );
            LOG.debug( "Nature: {}", event.getPayload().getNature() );
            String destination = "events." + event.getOrigin().getId();

            thread( new EventProducer( brokerURL, destination, jsonEvent ), true );
        }
        catch ( JsonProcessingException e )
        {
            LOG.error( e.getMessage(), e );
        }
    }


    private void thread( Runnable runnable, boolean daemon )
    {
        Thread brokerThread = new Thread( runnable );
        brokerThread.setDaemon( daemon );
        brokerThread.start();
    }


    private void placeTokenIntoContainer( ContainerHost containerHost, String token ) throws CommandException
    {
        containerHost.executeAsync( new RequestBuilder(
                String.format( "mkdir -p /etc/subutai/ ; echo '%s' > /etc/subutai/jwttoken", token ) ) );
    }
}

