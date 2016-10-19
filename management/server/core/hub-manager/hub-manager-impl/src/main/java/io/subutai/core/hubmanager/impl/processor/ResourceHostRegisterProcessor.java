package io.subutai.core.hubmanager.impl.processor;


import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.core.hubmanager.api.StateLinkProcessor;
import io.subutai.core.hubmanager.impl.http.HubRestClient;
import io.subutai.core.hubmanager.impl.http.RestResult;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.registration.api.HostRegistrationManager;
import io.subutai.core.registration.api.exception.HostRegistrationException;
import io.subutai.hub.share.dto.ResourceHostDataDto;
import io.subutai.hub.share.dto.host.RequestedHostDto;

import static java.lang.String.format;


public class ResourceHostRegisterProcessor implements StateLinkProcessor
{
    private static final Logger LOG = LoggerFactory.getLogger( ResourceHostRegisterProcessor.class.getName() );

    private HostRegistrationManager registrationManager;
    private HubRestClient restClient;
    private PeerManager peerManager;

    private static final Pattern RH_DATA_PATTERN = Pattern.compile( "/rest/v1/peers/.*/requested-hosts/.*" );


    public ResourceHostRegisterProcessor( final HostRegistrationManager registrationManager, final PeerManager peerManager,
                                          final HubRestClient restClient )
    {
        this.registrationManager = registrationManager;
        this.peerManager = peerManager;
        this.restClient = restClient;
    }


    @Override
    public boolean processStateLinks( final Set<String> stateLinks ) throws Exception
    {
        for ( String link : stateLinks )
        {
            Matcher matcher = RH_DATA_PATTERN.matcher( link );
            if ( matcher.matches() )
            {
                ResourceHostDataDto resourceHostDataDto = getResourceHostRegisterData( link );
                try
                {
                    process( resourceHostDataDto );
                }
                catch ( HostRegistrationException e )
                {
                    LOG.error( e.getMessage() );
                }
            }
        }

        return false;
    }


    private ResourceHostDataDto getResourceHostRegisterData( final String link )
    {

        RestResult<ResourceHostDataDto> result = restClient.get( link, ResourceHostDataDto.class );

        if ( !result.isSuccess() )
        {
            LOG.error( result.getError() );
            return null;
        }

        LOG.debug( "ResourceHostDataDto: " + result.getError() );

        return result.getEntity();
    }


    private void process( final ResourceHostDataDto resourceHostDataDto ) throws HostRegistrationException
    {
        switch ( resourceHostDataDto.getState() )
        {
            case APPROVE:
                registrationManager.approveRequest( resourceHostDataDto.getRequestId() );
                resourceHostDataDto.setStatus( RequestedHostDto.Status.APPROVED );
                updateResourceHostData( resourceHostDataDto );
                break;
            case REJECT:
                registrationManager.rejectRequest( resourceHostDataDto.getRequestId() );
                resourceHostDataDto.setStatus( RequestedHostDto.Status.REJECTED );
                updateResourceHostData( resourceHostDataDto );
                break;
            case REMOVE:
                registrationManager.removeRequest( resourceHostDataDto.getRequestId() );
                deleteResourceHostData( resourceHostDataDto );
                break;
        }
    }


    public void updateResourceHostData( ResourceHostDataDto resourceHostDataDto )
    {

        String path = format( "/rest/v1/peers/%s/requested-hosts/%s", peerManager.getLocalPeer().getId(),
                resourceHostDataDto.getRequestId() );

        RestResult<Object> restResult = restClient.post( path, resourceHostDataDto );
        if ( !restResult.isSuccess() )
        {
            LOG.error( "Error on sending requested host data: " + restResult.getError() );
        }
    }


    public void deleteResourceHostData( ResourceHostDataDto resourceHostDataDto )
    {

        String path = format( "/rest/v1/peers/%s/requested-hosts/%s", peerManager.getLocalPeer().getId(),
                resourceHostDataDto.getRequestId() );

        RestResult<Object> restResult = restClient.delete( path );
        if ( !restResult.isSuccess() )
        {
            LOG.error( "Error on sending requested host data: " + restResult.getError() );
        }
    }
}
