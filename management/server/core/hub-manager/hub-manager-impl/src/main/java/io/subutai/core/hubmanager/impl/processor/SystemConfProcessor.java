package io.subutai.core.hubmanager.impl.processor;


import java.io.IOException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.Response;

import org.bouncycastle.openpgp.PGPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.http.HttpStatus;

import com.google.common.base.Preconditions;

import io.subutai.core.hubmanager.api.StateLinkProcessor;
import io.subutai.core.hubmanager.api.exception.HubManagerException;
import io.subutai.core.hubmanager.impl.ConfigManager;
import io.subutai.hub.share.dto.SystemConfDto;
import io.subutai.hub.share.json.JsonUtil;


// TODO: Replace WebClient with HubRestClient.
public class SystemConfProcessor implements StateLinkProcessor
{
    private static final Logger LOG = LoggerFactory.getLogger( SystemConfProcessor.class.getName() );
    private ConfigManager configManager;

    private static final Pattern SYSTEM_CONF_PATTERN = Pattern.compile( "/rest/v1/system-info/" + "." );


    public SystemConfProcessor( final ConfigManager hConfigManager )
    {
        this.configManager = hConfigManager;
    }


    @Override
    public boolean processStateLinks( final Set<String> stateLinks ) throws HubManagerException
    {
        for ( String link : stateLinks )
        {
            Matcher systemConfMatcher = SYSTEM_CONF_PATTERN.matcher( link );
            if ( systemConfMatcher.matches() )
            {
                SystemConfDto systemConfDto = getSystemInfo( link );
                try
                {
                    processSystemConf( systemConfDto );
                }
                catch ( Exception e )
                {
                    LOG.error( e.getMessage() );
                }
            }
        }

        return false;
    }


    private SystemConfDto getSystemInfo( final String link ) throws HubManagerException
    {
        try
        {
            WebClient client = configManager.getTrustedWebClientWithAuth( link, configManager.getHubIp() );

            LOG.debug( "Sending request for getting System Info DTO..." );
            Response r = client.get();
            SystemConfDto result;

            if ( r.getStatus() == HttpStatus.SC_NO_CONTENT )
            {
                return null;
            }

            if ( r.getStatus() != HttpStatus.SC_OK )
            {
                LOG.error( r.readEntity( String.class ) );
                return null;
            }

            byte[] encryptedContent = configManager.readContent( r );
            byte[] plainContent = configManager.getMessenger().consume( encryptedContent );

            result = JsonUtil.fromCbor( plainContent, SystemConfDto.class );

            Preconditions.checkNotNull( result );

            LOG.debug( "SystemConfDto: " + result.toString() );

            return result;
        }
        catch ( PGPException | IOException e )
        {
            throw new HubManagerException( "Could not retrieve system configurations", e );
        }
    }


    private void processSystemConf( final SystemConfDto systemConfDto ) throws HubManagerException
    {
        //todo implement
    }
}
