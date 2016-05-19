package io.subutai.core.hubmanager.impl.environment;


import java.util.Set;

import io.subutai.core.hubmanager.api.HubPluginException;
import io.subutai.core.hubmanager.api.StateLinkProcessor;
import io.subutai.core.hubmanager.impl.environment.state.Context;
import io.subutai.core.hubmanager.impl.environment.state.StateHandler;
import io.subutai.core.hubmanager.impl.environment.state.StateHandlerFactory;
import io.subutai.hub.share.dto.environment.EnvironmentPeerDto;


public class HubEnvironmentProcessor implements StateLinkProcessor
{
    private final Context ctx;

    private final StateHandlerFactory handlerFactory;

    private final String linkPattern;


    public HubEnvironmentProcessor( Context ctx )
    {
        this.ctx = ctx;

        handlerFactory = new StateHandlerFactory( ctx );

        linkPattern = "/rest/v1/environments/.*/peers/" + ctx.localPeer.getId();
    }


    @Override
    public void processStateLinks( Set<String> stateLinks ) throws HubPluginException
    {
        for ( String link : stateLinks )
        {
            if ( link.matches( linkPattern ) )
            {
                processStateLink( link );
            }
        }
    }


    private void processStateLink( String link ) throws HubPluginException
    {
        EnvironmentPeerDto peerDto = ctx.restClient.getStrict( link, EnvironmentPeerDto.class );

        StateHandler handler = handlerFactory.getHandler( peerDto.getState() );

        handler.handle( peerDto );
    }


/*    public void sendLogToHub( EnvironmentPeerDto peerDto, String msg, String exMsg, EnvironmentPeerLogDto.LogEvent logE,
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
    }*/

}
