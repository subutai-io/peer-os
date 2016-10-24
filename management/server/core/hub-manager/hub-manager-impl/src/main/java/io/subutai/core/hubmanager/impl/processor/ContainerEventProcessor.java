package io.subutai.core.hubmanager.impl.processor;


import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.client.WebClient;

import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.settings.Common;
import io.subutai.core.hubmanager.api.exception.HubManagerException;
import io.subutai.core.hubmanager.impl.ConfigManager;
import io.subutai.core.hubmanager.impl.HubManagerImpl;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.hub.share.dto.environment.ContainerStateDto;
import io.subutai.hub.share.dto.environment.container.ContainerEventDto;
import io.subutai.hub.share.json.JsonUtil;


// TODO: Replace WebClient with HubRestClient.
public class ContainerEventProcessor implements Runnable
{
    private final Logger log = LoggerFactory.getLogger( getClass() );

    private HubManagerImpl hubManager;

    private ConfigManager configManager;

    private PeerManager peerManager;


    public ContainerEventProcessor( HubManagerImpl hubManager, ConfigManager configManager, PeerManager peerManager )
    {
        this.hubManager = hubManager;
        this.configManager = configManager;
        this.peerManager = peerManager;
    }


    @Override
    public void run()
    {
        try
        {
            process();
        }
        catch ( Exception e )
        {
            log.error( "Error to process container event: {}", e );
        }
    }


    public void process() throws HubManagerException
    {
        if ( !hubManager.isRegistered() )
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


    private void sendContainerStates( ResourceHost rh ) throws HubManagerException
    {
        log.info( "ResourceHost: id={}, hostname={}, containers={}", rh.getId(), rh.getHostname(),
                rh.getContainerHosts().size() );

        for ( ContainerHost ch : rh.getContainerHosts() )
        {
            if ( !Common.MANAGEMENT_HOSTNAME.equals( ch.getContainerName() ) )
            {
                sendContainerState( ch );
            }
        }
    }


    private void sendContainerState( ContainerHost ch ) throws HubManagerException
    {
        log.info( "- ContainerHost: id={}, name={}, environmentId={}, state={}", ch.getId(), ch.getContainerName(),
                ch.getEnvironmentId(), ch.getState() );

        ContainerStateDto state = ContainerStateDto.valueOf( ch.getState().name() );

        ContainerEventDto dto = new ContainerEventDto( ch.getId(), ch.getEnvironmentId().getId(), state );

        Response res = doRequest( dto );

        log.info( "Response status: {}", res.getStatus() );
    }


    private Response doRequest( ContainerEventDto dto ) throws HubManagerException
    {
        try
        {
            String path = String.format( "/rest/v2/containers/%s/events", dto.getContainerId() );

            WebClient client = configManager.getTrustedWebClientWithAuth( path, configManager.getHubIp() );

            byte[] plainData = JsonUtil.toCbor( dto );

            byte[] encryptedData = configManager.getMessenger().produce( plainData );

            return client.post( encryptedData );
        }
        catch ( Exception e )
        {
            throw new HubManagerException( e );
        }
    }
}
