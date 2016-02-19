package io.subutai.core.channel.impl.interceptor;


import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.jaxrs.impl.HttpHeadersImpl;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.transport.http.AbstractHTTPDestination;

import io.subutai.common.peer.PeerException;
import io.subutai.common.settings.ChannelSettings;
import io.subutai.common.settings.Common;
import io.subutai.common.settings.PeerSettings;
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
        if ( !PeerSettings.getEncryptionState() )
        {
            return;
        }

        try
        {
            if ( InterceptorState.SERVER_OUT.isActive( message ) )
            {
                //LOG.info( " *** Server OutInterceptor invoked *** " );

                HttpServletRequest req = ( HttpServletRequest ) message.getExchange().getInMessage()
                                                                       .get( AbstractHTTPDestination.HTTP_REQUEST );

                if ( req.getLocalPort() == ChannelSettings.SECURE_PORT_X2 )
                {
                    //LOG.info( " *** URL:" + url.getPath() );
                    HttpHeaders headers = new HttpHeadersImpl( message.getExchange().getInMessage() );
                    String subutaiHeader = headers.getHeaderString( Common.SUBUTAI_HTTP_HEADER );
                    //LOG.debug( "Remote address: " + subutaiHeader );
                    String path = req.getRequestURI();

                    if ( path.startsWith( "/rest/v1/peer" ) )
                    {
                        handlePeerMessage( subutaiHeader, message );
                        //                        LOG.debug( "Path handled by peer crypto handler: " + path );
                    }
                    else
                    {
                        final String prefix = "/rest/v1/env";
                        if ( path.startsWith( prefix ) )
                        {
                            String s = path.substring( prefix.length() + 1 );
                            String environmentId = s.substring( 0, s.indexOf( "/" ) );
                            handleEnvironmentMessage( subutaiHeader, environmentId, message );
                            //LOG.debug( "Path handled by environment crypto handler: " +
                            // path );
                        }
                        else
                        {
                            //LOG.warn( "Path is not handled by crypto handler: " + path );
                        }
                    }
                }
                //***********************************************************************
            }
        }
        catch ( Exception e )
        {
            throw new Fault( e );
        }
    }


    private void handlePeerMessage( final String targetId, final Message message )
    {
        String sourceId = peerManager.getLocalPeer().getId();
        MessageContentUtil.encryptContent( channelManagerImpl.getSecurityManager(), sourceId, targetId, message );
    }


    private void handleEnvironmentMessage( final String targetId, final String environmentId, final Message message )
    {
        String sourceId = peerManager.getLocalPeer().getId() + "-" + environmentId;

        MessageContentUtil
                .encryptContent( channelManagerImpl.getSecurityManager(), sourceId, targetId + "-" + environmentId,
                        message );
    }
}
