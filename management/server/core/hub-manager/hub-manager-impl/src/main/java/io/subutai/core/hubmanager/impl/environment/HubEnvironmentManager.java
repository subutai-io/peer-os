package io.subutai.core.hubmanager.impl.environment;


import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.http.HttpStatus;

import io.subutai.core.hubmanager.impl.ConfigManager;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.hub.share.dto.environment.EnvironmentPeerDto;
import io.subutai.hub.share.dto.environment.EnvironmentPeerLogDto;
import io.subutai.hub.share.json.JsonUtil;


// TODO: Replace WebClient with HubRestClient.
public class HubEnvironmentManager
{
    private final Logger log = LoggerFactory.getLogger( getClass() );

    private PeerManager peerManager;

    private ConfigManager configManager;


    public HubEnvironmentManager( ConfigManager configManager, PeerManager peerManager )
    {
        this.configManager = configManager;
        this.peerManager = peerManager;
    }


    public void sendLogToHub( EnvironmentPeerDto peerDto, String msg, String exMsg, EnvironmentPeerLogDto.LogEvent logE,
                              EnvironmentPeerLogDto.LogType logType, String contId )
    {
        try
        {
            String envPeerLogPath =
                    String.format( "/rest/v1/environments/%s/peers/%s/log", peerDto.getEnvironmentInfo().getId(),
                            peerManager.getLocalPeer().getId() );
            WebClient client = configManager.getTrustedWebClientWithAuth( envPeerLogPath, configManager.getHubIp() );

            EnvironmentPeerLogDto peerLogDto = new EnvironmentPeerLogDto( peerDto.getPeerId(), peerDto.getState(),
                    peerDto.getEnvironmentInfo().getId(), logType );
            peerLogDto.setMessage( msg );
            peerLogDto.setExceptionMessage( exMsg );
            peerLogDto.setLogEvent( logE );
            peerLogDto.setContainerId( contId );
            peerLogDto.setLogCode( null );

            byte[] cborData = JsonUtil.toCbor( peerLogDto );
            byte[] encryptedData = configManager.getMessenger().produce( cborData );
            Response r = client.post( encryptedData );
            if ( r.getStatus() == HttpStatus.SC_OK )
            {
                log.debug( "Environment peer log successfully sent to hub" );
            }
        }
        catch ( Exception e )
        {
            log.error( "Could not sent environment peer log to hub.", e.getMessage() );
        }
    }
}
