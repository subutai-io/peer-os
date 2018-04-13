package io.subutai.core.environment.metadata.impl;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
import io.subutai.core.hubmanager.api.HubManager;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.exception.TokenCreateException;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.hub.share.Utils;
import io.subutai.hub.share.dto.BrokerSettingsDto;
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
    private BrokerSettings brokerSettings;
    private HubManager hubManager;


    public EnvironmentMetadataManagerImpl( PeerManager peerManager, EnvironmentManager environmentManager,
                                           IdentityManager identityManager, HubManager hubManager )
    {
        this.peerManager = peerManager;
        this.environmentManager = environmentManager;
        this.identityManager = identityManager;
        this.hubManager = hubManager;
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
            String destination = "events." + event.getOrigin().getId();

            checkBrokerSettings( false );
            // TODO: 4/12/18 need to implement connections pool something like below;  while creating connection
            // every time with random URI
            //            ActiveMQConnectionFactory amq = new ActiveMQConnectionFactory(
            //                    "vm://broker1?marshal=false&broker.persistent=false&broker.useJmx=false");
            //            JmsPoolConnectionFactory cf = new JmsPoolConnectionFactory();
            //            cf.setConnectionFactory(amq);
            //            cf.setMaxConnections(3);
            thread( new EventProducer( this.brokerSettings.getBroker(), destination, jsonEvent ), true );
        }
        catch ( JsonProcessingException | URISyntaxException | BrokerSettingException e )
        {
            LOG.error( e.getMessage(), e );
        }
    }


    private void checkBrokerSettings( boolean retrieve ) throws BrokerSettingException
    {
        if ( !( brokerSettings == null || retrieve ) )
        {
            return;
        }

        try
        {
            final BrokerSettingsDto response = hubManager.getBrokers();
            if ( response == null )
            {
                throw new BrokerSettingException( "Could not retrieve broker settings." );
            }
            List<URI> list = new ArrayList<>();
            for ( String s : response.getBrokers() )
            {
                list.add( new URI( s ) );
            }
            if ( list.size() == 0 )
            {
                throw new BrokerSettingException( "Broker URI list is empty." );
            }
            this.brokerSettings = new BrokerSettings( list );
        }
        catch ( URISyntaxException e )
        {
            throw new BrokerSettingException( e );
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


    private class BrokerSettings
    {
        List<URI> uriList;


        public BrokerSettings( final List<URI> uriList )
        {
            this.uriList = uriList;
        }


        public URI getBroker()
        {
            return uriList.get( new Random().nextInt( uriList.size() ) );
        }
    }
}

