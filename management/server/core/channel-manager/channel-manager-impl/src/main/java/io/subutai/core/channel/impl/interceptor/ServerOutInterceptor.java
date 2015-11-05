package io.subutai.core.channel.impl.interceptor;


import java.net.URL;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.transport.http.AbstractHTTPDestination;

import io.subutai.common.peer.PeerException;
import io.subutai.common.settings.ChannelSettings;
import io.subutai.core.channel.impl.ChannelManagerImpl;
import io.subutai.core.channel.impl.util.InterceptorState;
import io.subutai.core.channel.impl.util.MessageContentUtil;
import io.subutai.core.peer.api.PeerManager;


/**
 * Out Interceptor
 */
public class ServerOutInterceptor extends AbstractPhaseInterceptor<Message>
{

    private static final Logger LOG = LoggerFactory.getLogger( ServerOutInterceptor.class );
    private PeerManager peerManager;
    private ChannelManagerImpl channelManagerImpl = null;


    public ServerOutInterceptor( ChannelManagerImpl channelManagerImpl, PeerManager peerManager )
    {
        super( Phase.PRE_STREAM );
        this.channelManagerImpl = channelManagerImpl;
        this.peerManager = peerManager;
    }


    /**
     * Intercepts a message. interceptor chain will take care of this.
     */
    @Override
    public void handleMessage( final Message message )
    {
        if ( !channelManagerImpl.isEncryptionEnabled() )
        {
            return;
        }

        try
        {
            if ( InterceptorState.SERVER_OUT.isActive( message ) )
            {
                LOG.info( " *** Server OutInterceptor invoked *** " );

                URL url = new URL( ( String ) message.getExchange().getInMessage().get( Message.REQUEST_URL ) );

                if ( url.getPort() == Integer.parseInt( ChannelSettings.SECURE_PORT_X2 ) )
                {
                    LOG.info( " *** URL:" + url.getPath() );

                    HttpServletRequest request = ( HttpServletRequest ) message.getExchange().getInMessage()
                                                                               .get( AbstractHTTPDestination
                                                                                       .HTTP_REQUEST );
                    String remoteAddress = request.getRemoteAddr();
                    LOG.debug( "Remote address: " + remoteAddress );
                    String path = url.getPath();

                    if ( path.startsWith( "/rest/v1/peer" ) )
                    {
                        handlePeerMessage( remoteAddress, message );
                        LOG.debug( "Path handled by peer crypto handler: " + path );
                    }
                    else
                    {
                        final String prefix = "/rest/v1/env";
                        if ( path.startsWith( prefix ) )
                        {
                            String s = path.substring( prefix.length() + 1 );
                            String environmentId = s.substring( 0, s.indexOf( "/" ) );
                            handleEnvironmentMessage( remoteAddress, environmentId, message );
                            LOG.debug( "Path handled by environment crypto handler: " + path );
                        }
                        else
                        {
                            LOG.warn( "Path is not handled by crypto handler: " + path );
                        }
                    }
                }
                //***********************************************************************
            }
        }
        catch ( Exception ignore )
        {
            LOG.debug( "MalformedURLException", ignore.toString() );
        }
    }


    private void handlePeerMessage( final String ip, final Message message )
    {
        try
        {
            String targetId = peerManager.getPeerIdByIp( ip );
            String sourceId = peerManager.getLocalPeerInfo().getId();
            MessageContentUtil
                    .encryptContent( channelManagerImpl.getSecurityManager(), sourceId, targetId, ip, message );
        }
        catch ( PeerException e )
        {
            LOG.warn( e.getMessage() );
        }
    }


    private void handleEnvironmentMessage( final String ip, final String environmentId, final Message message )
    {
        try
        {
            String targetId = peerManager.getPeerIdByIp( ip ) + "-" + environmentId;
            String sourceId = peerManager.getLocalPeerInfo().getId() + "-" + environmentId;

            MessageContentUtil
                    .encryptContent( channelManagerImpl.getSecurityManager(), sourceId, targetId, ip, message );
        }
        catch ( PeerException e )
        {
            LOG.warn( e.getMessage() );
        }
    }
}
