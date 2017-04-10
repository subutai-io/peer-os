package io.subutai.core.hubmanager.impl.processor;


import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.core.hubmanager.api.RestResult;
import io.subutai.core.hubmanager.api.StateLinkProcessor;
import io.subutai.core.hubmanager.api.exception.HubManagerException;
import io.subutai.core.hubmanager.impl.http.HubRestClient;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.registration.api.HostRegistrationManager;
import io.subutai.core.registration.api.exception.HostRegistrationException;
import io.subutai.hub.share.dto.AnsibleDataDto;
import io.subutai.hub.share.dto.ResourceHostDataDto;

import static java.lang.String.format;


public class AnsibleTaskProcessor implements StateLinkProcessor
{
    private static final Logger LOG = LoggerFactory.getLogger( AnsibleTaskProcessor.class );

    private HostRegistrationManager registrationManager;
    private HubRestClient restClient;
    private PeerManager peerManager;

    private static final Pattern ANSIBLE_DATA_PATTERN = Pattern.compile( "/rest/v1/peers/.*/ansible/.*" );


    public AnsibleTaskProcessor( final HostRegistrationManager registrationManager, final PeerManager peerManager,
                                 final HubRestClient restClient )
    {
        this.registrationManager = registrationManager;
        this.peerManager = peerManager;
        this.restClient = restClient;
    }


    @Override
    public boolean processStateLinks( final Set<String> stateLinks ) throws HubManagerException
    {
        for ( String link : stateLinks )
        {
            Matcher matcher = ANSIBLE_DATA_PATTERN.matcher( link );
            if ( matcher.matches() )
            {
                AnsibleDataDto ansibleDataDto = getAnsibleData( link );
                try
                {
                    process( ansibleDataDto );
                }
                catch ( HostRegistrationException e )
                {
                    LOG.error( e.getMessage() );
                }
            }
        }

        return false;
    }


    private AnsibleDataDto getAnsibleData( final String link )
    {

        RestResult<AnsibleDataDto> result = restClient.get( link, AnsibleDataDto.class );

        if ( !result.isSuccess() )
        {
            LOG.error( result.getError() );
            return null;
        }

        LOG.debug( "AnsibleDataDto: " + result.getError() );

        return result.getEntity();
    }


    private void process( final AnsibleDataDto ansibleDataDto ) throws HostRegistrationException
    {
        switch ( ansibleDataDto.getAction() )
        {
            case CONFIGURE:
                // TODO: 4/6/17 not implement yet
                break;
            case GROW:
                // TODO: 4/6/17 not implement yet
                break;
            case SHRINK:
                // TODO: 4/6/17 not implement yet
                break;
            case DESTROY:
                // TODO: 4/6/17 not implement yet
                break;
            default:
                LOG.info( "Requested {}", ansibleDataDto.getAction() );
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
