package io.subutai.core.hubmanager.impl.processor;


import java.util.List;

import io.subutai.common.peer.HostNotFoundException;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.ResourceHost;
import io.subutai.core.hubmanager.api.HubRequester;
import io.subutai.core.hubmanager.api.RestResult;
import io.subutai.core.hubmanager.api.exception.HubManagerException;
import io.subutai.core.hubmanager.impl.HubManagerImpl;
import io.subutai.core.hubmanager.impl.http.HubRestClient;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.registration.api.HostRegistrationManager;
import io.subutai.core.registration.api.service.RequestedHost;
import io.subutai.hub.share.dto.host.RequestedHostDto;
import io.subutai.hub.share.dto.host.RequestedHostsDto;

import static java.lang.String.format;


public class RegistrationRequestProcessor extends HubRequester
{
    private PeerManager peerManager;

    private HostRegistrationManager registrationManager;


    public RegistrationRequestProcessor( final HubManagerImpl hubManager, final PeerManager peerManager,
                                         final HostRegistrationManager registrationManager,
                                         final HubRestClient restClient )
    {
        super( hubManager, restClient );

        this.peerManager = peerManager;
        this.registrationManager = registrationManager;
    }


    @Override
    public void request() throws HubManagerException
    {
        sendRegistrationRequests();
    }


    private void sendRegistrationRequests() throws HubManagerException
    {
        String path = format( "/rest/v1/peers/%s/requested-hosts", peerManager.getLocalPeer().getId() );

        LocalPeer localPeer = peerManager.getLocalPeer();
        RequestedHostsDto requestedHostsDto = new RequestedHostsDto();
        requestedHostsDto.setPeerId( localPeer.getId() );

        List<RequestedHost> requestedHosts = registrationManager.getRequests();
        ResourceHost managementHost;
        try
        {
            managementHost = localPeer.getManagementHost();
        }
        catch ( HostNotFoundException e )
        {
            throw new HubManagerException( e );
        }

        for ( RequestedHost requestedHost : requestedHosts )
        {
            RequestedHostDto requestedHostDto = new RequestedHostDto();
            if ( managementHost.getId().equalsIgnoreCase( requestedHost.getId() ) )
            {
                requestedHostDto.setManagement( true );
            }
            requestedHostDto.setHostname( requestedHost.getHostname() );
            requestedHostDto.setId( requestedHost.getId() );
            requestedHostDto.setStatus( RequestedHostDto.Status.valueOf( requestedHost.getStatus().name() ) );

            requestedHostsDto.addRequestedHostsDto( requestedHostDto );
        }

        RestResult<Object> restResult = restClient.post( path, requestedHostsDto );

        if ( !restResult.isSuccess() )
        {
            throw new HubManagerException( "Error on sending requested host data: " + restResult.getError() );
        }
    }
}
