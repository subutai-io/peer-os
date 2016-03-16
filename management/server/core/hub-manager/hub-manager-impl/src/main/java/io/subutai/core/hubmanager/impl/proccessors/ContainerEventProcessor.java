package io.subutai.core.hubmanager.impl.proccessors;


import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.client.WebClient;

import io.subutai.common.host.ContainerHostState;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.ResourceHost;
import io.subutai.core.hubmanager.api.HubPluginException;
import io.subutai.core.hubmanager.impl.ConfigManager;
import io.subutai.core.hubmanager.impl.IntegrationImpl;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.hub.share.dto.ContainerEventDto;
import io.subutai.hub.share.json.JsonUtil;


public class ContainerEventProcessor
{
    private final Logger log = LoggerFactory.getLogger( getClass() );

    private IntegrationImpl manager;

    private ConfigManager configManager;

    private PeerManager peerManager;

    public ContainerEventProcessor( IntegrationImpl integration, ConfigManager configManager, PeerManager peerManager )
    {
        this.manager = integration;
        this.configManager = configManager;
        this.peerManager = peerManager;
    }


    public void process() throws HubPluginException
    {
        if ( !manager.getRegistrationState() )
        {
            return;
        }

        try
        {
            for ( ResourceHost rh : peerManager.getLocalPeer().getResourceHosts() )
            {
                sendContainerStates( rh );
            }
        }
        catch ( Exception e )
        {
            log.error( "Oops error: ", e );
        }
    }


    private void sendContainerStates( ResourceHost rh ) throws Exception
    {
        log.info( "ResourceHost: id={}, hostname={}, containers={}", rh.getId(), rh.getHostname(), rh.getContainerHosts().size() );

        for ( ContainerHost ch : rh.getContainerHosts() )
        {
            if ( !"management".equals( ch.getContainerName() ) )
            {
                sendContainerState( ch );
            }
        }
    }


    private void sendContainerState( ContainerHost ch ) throws Exception
    {
        log.info( "- ContainerHost: id={}, name={}, environmentId={}, state={}", ch.getId(), ch.getContainerName(), ch.getEnvironmentId(),
                ch.getState() );

        // For now Hub needs RUNNING only
        if ( ch.getState() != ContainerHostState.RUNNING )
        {
           return;
        }

        ContainerEventDto.Type type = ContainerEventDto.Type.valueOf( ch.getState().name() );

        ContainerEventDto dto = new ContainerEventDto( ch.getId(), type );

        Response res = doRequest( dto );

        log.info( "Response status: {}", res.getStatus() );
    }


    private Response doRequest( ContainerEventDto dto ) throws Exception
    {
        String path = String.format( "/rest/v1/containers/%s/events", dto.getContainerId() );

        WebClient client = configManager.getTrustedWebClientWithAuth( path, configManager.getHubIp() );

        byte[] plainData = JsonUtil.toCbor( dto );

        byte[] encryptedData = configManager.getMessenger().produce( plainData );

        return client.post( encryptedData );
    }

}
