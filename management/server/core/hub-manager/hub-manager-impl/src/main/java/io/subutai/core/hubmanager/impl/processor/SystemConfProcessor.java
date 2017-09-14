package io.subutai.core.hubmanager.impl.processor;


import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.http.HttpStatus;

import com.google.common.base.Preconditions;

import io.subutai.core.hubmanager.api.RestClient;
import io.subutai.core.hubmanager.api.RestResult;
import io.subutai.core.hubmanager.api.StateLinkProcessor;
import io.subutai.core.hubmanager.api.exception.HubManagerException;
import io.subutai.hub.share.dto.SystemConfDto;

@Deprecated
//TODO remove since it does not perform any meaningful operations
public class SystemConfProcessor implements StateLinkProcessor
{
    private static final Logger LOG = LoggerFactory.getLogger( SystemConfProcessor.class.getName() );
    private RestClient restClient;

    private static final Pattern SYSTEM_CONF_PATTERN = Pattern.compile( "/rest/v1/system-info/" + "." );


    public SystemConfProcessor( final RestClient restClient )
    {
        this.restClient = restClient;
    }


    @Override
    public boolean processStateLinks( final Set<String> stateLinks ) throws HubManagerException
    {
        for ( String link : stateLinks )
        {
            Matcher systemConfMatcher = SYSTEM_CONF_PATTERN.matcher( link );
            if ( systemConfMatcher.matches() )
            {
                getSystemInfo( link );
            }
        }

        return false;
    }


    private void getSystemInfo( final String link ) throws HubManagerException
    {
        try
        {
            LOG.debug( "Sending request for getting System Info DTO..." );

            RestResult<SystemConfDto> restResult = restClient.get( link, SystemConfDto.class );

            if ( restResult.getStatus() == HttpStatus.SC_NO_CONTENT )
            {
                return;
            }

            if ( restResult.getStatus() != HttpStatus.SC_OK )
            {
                LOG.error( restResult.getError() );
                return;
            }

            SystemConfDto result = restResult.getEntity();

            Preconditions.checkNotNull( result );

            LOG.debug( "SystemConfDto: " + result.toString() );

            processSystemConf( result );
        }
        catch ( Exception e )
        {
            throw new HubManagerException( "Could not retrieve system configurations", e );
        }
    }


    private void processSystemConf( SystemConfDto result ) throws HubManagerException
    {
        //todo implement
    }
}
