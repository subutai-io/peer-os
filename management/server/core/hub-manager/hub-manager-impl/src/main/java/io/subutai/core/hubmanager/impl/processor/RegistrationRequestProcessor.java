package io.subutai.core.hubmanager.impl.processor;


import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.core.hubmanager.impl.HubManagerImpl;
import io.subutai.core.hubmanager.impl.http.HubRestClient;
import io.subutai.core.hubmanager.impl.http.RestResult;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.registration.api.RegistrationManager;
import io.subutai.core.registration.api.RegistrationStatus;
import io.subutai.core.registration.api.service.RequestedHost;
import io.subutai.hub.share.dto.host.RequestedHostDto;
import io.subutai.hub.share.dto.host.RequestedHostsDto;

import static java.lang.String.format;


public class RegistrationRequestProcessor implements Runnable
{
    private final Logger log = LoggerFactory.getLogger( getClass() );

    private HubManagerImpl manager;

    private PeerManager peerManager;

    private RegistrationManager registrationManager;

    private final HubRestClient restClient;


    public RegistrationRequestProcessor( final HubManagerImpl integration, final PeerManager peerManager,
                                         final RegistrationManager registrationManager, final HubRestClient restClient )
    {
        this.peerManager = peerManager;
        this.manager = integration;
        this.registrationManager = registrationManager;
        this.restClient = restClient;
    }


    @Override
    public void run()
    {
        try
        {
            sendRegistrationRequests();
        }
        catch ( Exception e )
        {
            log.debug( "Sending resource host registration request failed." );

            log.error( e.getMessage(), e );
        }
    }


    public void sendRegistrationRequests() throws Exception
    {
        if ( manager.isRegistered() )
        {
            String path = format( "/rest/v1/peers/%s/resource-hosts/requests", peerManager.getLocalPeer().getId() );

            RequestedHostsDto requestedHostsDto = new RequestedHostsDto();

            List<RequestedHost> requestedHosts = registrationManager.getRequests();
            for ( RequestedHost requestedHost : requestedHosts )
            {
                if ( requestedHost.getStatus().equals( RegistrationStatus.REQUESTED ) )
                {
                    RequestedHostDto requestedHostDto = new RequestedHostDto();
                    requestedHostDto.setHostname( requestedHost.getHostname() );
                    requestedHostDto.setId( requestedHost.getId() );
                    requestedHostDto.setState( RequestedHostDto.Status.valueOf( requestedHost.getStatus().name() ) );

                    requestedHostsDto.addRequestedHostsDto( requestedHostDto );
                }
            }

            RestResult<Object> restResult = restClient.post( path, requestedHostsDto );
            if ( !restResult.isSuccess() )
            {
                throw new Exception( "Error on sending requested host data: " + restResult.getError() );
            }
        }
    }
}
