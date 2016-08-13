package io.subutai.core.hubmanager.impl.processor;


import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.core.hubmanager.api.StateLinkProcessor;
import io.subutai.core.hubmanager.impl.http.HubRestClient;
import io.subutai.core.hubmanager.impl.http.RestResult;
import io.subutai.core.registration.api.RegistrationManager;
import io.subutai.core.registration.api.exception.HostRegistrationException;
import io.subutai.hub.share.dto.ResourceHostDataDto;


public class ResourceHostRegisterProcessor implements StateLinkProcessor
{
    private static final Logger LOG = LoggerFactory.getLogger( ResourceHostRegisterProcessor.class.getName() );
    private RegistrationManager registrationManager;
    private HubRestClient restClient;

    private static final Pattern RH_DATA_PATTERN = Pattern.compile( "/rest/v1/system-info/" + "." );


    public ResourceHostRegisterProcessor( final RegistrationManager registrationManager,
                                          final HubRestClient restClient )
    {
        this.registrationManager = registrationManager;
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
                registrationManager.approveRequest( resourceHostDataDto.getResourceHostId() );
            case REJECT:
                registrationManager.rejectRequest( resourceHostDataDto.getResourceHostId() );
            case REMOVE:
                registrationManager.removeRequest( resourceHostDataDto.getResourceHostId() );
        }
    }
}
