package io.subutai.core.hubmanager.impl.processor;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.subutai.common.peer.HostNotFoundException;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.ResourceHost;
import io.subutai.core.hubmanager.api.HubRequester;
import io.subutai.core.hubmanager.api.RestClient;
import io.subutai.core.hubmanager.api.RestResult;
import io.subutai.core.hubmanager.api.exception.HubManagerException;
import io.subutai.core.hubmanager.impl.HubManagerImpl;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.registration.api.HostRegistrationManager;
import io.subutai.core.registration.api.ResourceHostRegistrationStatus;
import io.subutai.core.registration.api.service.RequestedHost;
import io.subutai.hub.share.dto.host.RequestedHostDto;
import io.subutai.hub.share.dto.host.RequestedHostsDto;

import static java.lang.String.format;


public class RegistrationRequestProcessor extends HubRequester
{
    private PeerManager peerManager;

    private HostRegistrationManager registrationManager;

    //RH_ID Status: APPROVED,REJECTED
    private Map<String, ResourceHostRegistrationStatus> SENT_CACHE = new HashMap<>();


    public RegistrationRequestProcessor( final HubManagerImpl hubManager, final PeerManager peerManager,
                                         final HostRegistrationManager registrationManager,
                                         final RestClient restClient )
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

            if ( !isToSend( requestedHost ) )
            {
                continue;
            }

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

        if ( requestedHostsDto.getRequestedHostsDto().size() > 0 )
        {
            RestResult<Object> restResult = restClient.post( path, requestedHostsDto );

            if ( !restResult.isSuccess() )
            {
                SENT_CACHE.clear();
                throw new HubManagerException( "Error on sending requested host data: " + restResult.getError() );
            }
        }
    }


    private boolean isToSend( final RequestedHost rqh )
    {
        if ( SENT_CACHE.containsKey( rqh.getId() ) && SENT_CACHE.get( rqh ).equals( rqh.getStatus() ) )
        {
            return false;
        }

        SENT_CACHE.put( rqh.getId(), rqh.getStatus() );

        return true;
    }
}
