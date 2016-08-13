package io.subutai.core.hubmanager.impl.processor;


import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.Response;

import org.bouncycastle.openpgp.PGPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.http.HttpStatus;

import io.subutai.core.hubmanager.api.StateLinkProcessor;
import io.subutai.core.hubmanager.impl.ConfigManager;
import io.subutai.core.registration.api.RegistrationManager;
import io.subutai.hub.share.dto.ResourceHostDataDto;
import io.subutai.hub.share.json.JsonUtil;


// TODO: Replace WebClient with HubRestClient.
public class ResourceHostRegisterProcessor implements StateLinkProcessor
{
    private static final Logger LOG = LoggerFactory.getLogger( ResourceHostRegisterProcessor.class.getName() );
    private ConfigManager configManager;
    private RegistrationManager registrationManager;

    private static final Pattern RH_DATA_PATTERN = Pattern.compile( "/rest/v1/system-info/" + "." );


    public ResourceHostRegisterProcessor( final ConfigManager hConfigManager , final RegistrationManager registrationManager)
    {
        this.configManager = hConfigManager;
        this.registrationManager = registrationManager;
    }


    @Override
    public boolean processStateLinks( final Set<String> stateLinks ) throws Exception
    {
        for ( String link : stateLinks )
        {
            Matcher matcher = RH_DATA_PATTERN.matcher( link );
            if ( matcher.matches() )
            {
                ResourceHostDataDto resourceHostDataDto = getResourceHostData( link );
                try
                {
                    process( resourceHostDataDto );
                }
                catch ( Exception e)
                {
                    LOG.error( e.getMessage() );
                }
            }
        }

        return false;
    }


    private ResourceHostDataDto getResourceHostData( final String link ) throws Exception
    {
        try
        {
            WebClient client = configManager.getTrustedWebClientWithAuth( link, configManager.getHubIp() );

            LOG.debug( "Sending request for getting System Info DTO..." );
            Response r = client.get();
            ResourceHostDataDto result = null;

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

            result = JsonUtil.fromCbor( plainContent, ResourceHostDataDto.class );

            assert result != null;
            LOG.debug( "ResourceHostDataDto: " + result.toString() );

            return result;
        }
        catch ( UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException | PGPException | IOException
                e )
        {
            throw new Exception( "Could not retrieve ResourceHostDataDto", e );
        }
    }


    private void process( final ResourceHostDataDto ResourceHostDataDto )
    {
        switch ( ResourceHostDataDto.getState() )
        {

        }
    }
}
