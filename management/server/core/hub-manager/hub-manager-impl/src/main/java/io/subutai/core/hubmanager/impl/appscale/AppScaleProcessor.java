package io.subutai.core.hubmanager.impl.appscale;


import java.util.Set;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.http.HttpStatus;

import io.subutai.core.hubmanager.api.StateLinkProccessor;
import io.subutai.core.hubmanager.impl.ConfigManager;
import io.subutai.hub.share.dto.AppScaleConfigDto;
import io.subutai.hub.share.json.JsonUtil;


public class AppScaleProcessor implements StateLinkProccessor
{
    private final Logger log = LoggerFactory.getLogger( getClass() );

    private final ConfigManager configManager;

    private final AppScaleManager appScaleManager;


    public AppScaleProcessor( ConfigManager configManager, AppScaleManager appScaleManager )
    {
        this.configManager = configManager;
        this.appScaleManager = appScaleManager;
    }


    @Override
    public void processStateLinks( final Set<String> stateLinks )
    {
        for ( String stateLink : stateLinks )
        {
            if ( stateLink.contains( "appscale" ) )
            {
                appScaleManager.installCluster( getData( stateLink ) );
            }
        }

    }


    private AppScaleConfigDto getData( String link )
    {
        log.debug( "Getting AppScale data from Hub: {}", link );

        try
        {
            WebClient client = configManager.getTrustedWebClientWithAuth( link, configManager.getHubIp() );

            Response res = client.get();

            log.debug( "Response: HTTP {} - {}", res.getStatus(), res.getStatusInfo().getReasonPhrase() );

            if ( res.getStatus() != HttpStatus.SC_OK )
            {
                log.error( "Error to get AppScale data from Hub: HTTP {} - {}", res.getStatus(), res.getStatusInfo().getReasonPhrase() );

                return null;
            }

            byte[] encryptedContent = configManager.readContent( res );

            byte[] plainContent = configManager.getMessenger().consume( encryptedContent );

            return JsonUtil.fromCbor( plainContent, AppScaleConfigDto.class );
        }
        catch ( Exception e )
        {
            log.error( "Error to get AppScale data from Hub: ", e );

            return null;
        }
    }

}
