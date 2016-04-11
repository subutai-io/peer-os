package io.subutai.core.hubmanager.impl.proccessors;


import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Response;

import org.bouncycastle.openpgp.PGPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.http.HttpStatus;

import com.google.common.collect.Sets;

import io.subutai.core.hubmanager.api.HubPluginException;
import io.subutai.core.hubmanager.api.StateLinkProccessor;
import io.subutai.core.hubmanager.impl.ConfigManager;
import io.subutai.core.hubmanager.impl.IntegrationImpl;
import io.subutai.hub.share.dto.HeartbeatResponseDto;
import io.subutai.hub.share.json.JsonUtil;


public class HeartbeatProcessor implements Runnable
{
    private static final Logger LOG = LoggerFactory.getLogger( HeartbeatProcessor.class );
    private ConfigManager configManager;
    private Set<StateLinkProccessor> proccessors = Sets.newHashSet();
    private IntegrationImpl manager;


    public HeartbeatProcessor( final IntegrationImpl integration, final ConfigManager configManager )
    {
        this.configManager = configManager;
        this.manager = integration;
    }


    @Override
    public void run()
    {
        try
        {
            sendHeartbeat();
        }
        catch ( Exception e )
        {
            LOG.error( "Error to send heartbeat: ", e );
        }
    }


    public void sendHeartbeat() throws HubPluginException
    {
        if ( manager.getRegistrationState() )
        {

            final Set<String> result = new HashSet<>();
            try
            {
                String path = String.format( "/rest/v1.1/peers/%s/heartbeat", configManager.getPeerId() );

                WebClient client = configManager.getTrustedWebClientWithAuth( path, configManager.getHubIp() );

                Response r = client.put( null );


                if ( r.getStatus() != HttpStatus.SC_OK )
                {
                    throw new HubPluginException( "Could not send heartbeat: " + r.readEntity( String.class ) );
                }

                byte[] data = configManager.readContent( r );

                if ( data != null )
                {
                    HeartbeatResponseDto response = JsonUtil.fromCbor( configManager.getMessenger().consume( data ),
                            HeartbeatResponseDto.class );


                    LOG.debug( "State links from HUB: " + response.getStateLinks().toString() );

                    result.addAll( new HashSet<>( response.getStateLinks() ) );

                    for ( final StateLinkProccessor proccessor : proccessors )
                    {
                        try
                        {
                            proccessor.processStateLinks( result );
                        }
                        catch ( HubPluginException e )
                        {
                            LOG.error( e.getMessage() );
                        }
                    }
                }
                else
                {
                    LOG.debug( "Data is null." );
                }
            }
            catch ( PGPException | IOException | KeyStoreException | UnrecoverableKeyException |
                    NoSuchAlgorithmException e )
            {
                LOG.error( "Could not send heartbeat.", e );
            }
        }
    }


    public void addProccessor( StateLinkProccessor proccessor )
    {
        proccessors.add( proccessor );
    }
}
