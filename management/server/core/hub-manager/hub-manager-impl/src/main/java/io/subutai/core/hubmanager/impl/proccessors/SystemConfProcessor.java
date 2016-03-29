package io.subutai.core.hubmanager.impl.proccessors;


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

import io.subutai.core.hubmanager.api.HubPluginException;
import io.subutai.core.hubmanager.api.StateLinkProccessor;
import io.subutai.core.hubmanager.impl.ConfigManager;
import io.subutai.hub.share.dto.SystemConfDto;
import io.subutai.hub.share.json.JsonUtil;


public class SystemConfProcessor implements StateLinkProccessor
{
    private static final Logger LOG = LoggerFactory.getLogger( SystemConfProcessor.class.getName() );
    private ConfigManager configManager;

    private static final Pattern SYSTEM_CONF_PATTERN = Pattern.compile( "/rest/v1/system-info/" + "." );


    public SystemConfProcessor( final ConfigManager hConfigManager )
    {
        this.configManager = hConfigManager;
    }


    @Override
    public void proccessStateLinks( final Set<String> stateLinks ) throws HubPluginException
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
                catch ( UnrecoverableKeyException | IOException | KeyStoreException | NoSuchAlgorithmException e )
                {
                    LOG.error( e.getMessage() );
                }
            }
        }
    }


    private SystemConfDto getSystemInfo( final String link ) throws HubPluginException
    {
        try
        {
            WebClient client = configManager.getTrustedWebClientWithAuth( link, configManager.getHubIp() );

            LOG.debug( "Sending request for getting System Info DTO..." );
            Response r = client.get();
            SystemConfDto result = null;

            if ( r.getStatus() == HttpStatus.SC_NO_CONTENT )
            {
                return result;
            }

            if ( r.getStatus() != HttpStatus.SC_OK )
            {
                LOG.error( r.readEntity( String.class ) );
                return result;
            }

            byte[] encryptedContent = configManager.readContent( r );
            byte[] plainContent = configManager.getMessenger().consume( encryptedContent );

            result = JsonUtil.fromCbor( plainContent, SystemConfDto.class );

            LOG.debug( "SystemConfDto: " + result.toString() );

            return result;
        }
        catch ( UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException | PGPException | IOException
                e )
        {
            throw new HubPluginException( "Could not retrieve system configurations", e );
        }
    }


    private void processSystemConf( final SystemConfDto systemConfDto )
            throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, HubPluginException,
            IOException
    {
        switch ( systemConfDto.getKey() )
        {
            //TODO write cases for System types
        }
    }
}
