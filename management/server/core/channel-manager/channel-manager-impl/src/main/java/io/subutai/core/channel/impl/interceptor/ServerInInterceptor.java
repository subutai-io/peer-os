package io.subutai.core.channel.impl.interceptor;


import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.jaxrs.impl.HttpHeadersImpl;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.transport.http.AbstractHTTPDestination;

import io.subutai.common.settings.Common;
import io.subutai.common.settings.SystemSettings;
import io.subutai.core.channel.impl.ChannelManagerImpl;
import io.subutai.core.channel.impl.util.InterceptorState;
import io.subutai.core.channel.impl.util.MessageContentUtil;
import io.subutai.core.peer.api.PeerManager;


/**
 * CXF interceptor that controls channel (tunnel)
 */
public class ServerInInterceptor extends AbstractPhaseInterceptor<Message>
{
    private ChannelManagerImpl channelManagerImpl = null;
    private PeerManager peerManager;


    public ServerInInterceptor( ChannelManagerImpl channelManagerImpl, PeerManager peerManager )
    {
        super( Phase.PRE_STREAM );
        this.channelManagerImpl = channelManagerImpl;
        this.peerManager = peerManager;
    }


    /**
     * Intercepts a message. Interceptors should NOT invoke handleMessage or handleFault on the next interceptor - the
     * interceptor chain will take care of this.
     */
    @Override
    public void handleMessage( final Message message )
    {
        if ( !SystemSettings.getEncryptionState() )
        {
            return;
        }

        try
        {
            if ( InterceptorState.SERVER_IN.isActive( message ) )
            {

                HttpServletRequest req = ( HttpServletRequest ) message.get( AbstractHTTPDestination.HTTP_REQUEST );

                if ( req.getLocalPort() == /*SystemSettings.getSecurePortX2()*/ peerManager.getLocalPeer().getPeerInfo()
                                                                                           .getPort() )
                {
                    HttpHeaders headers = new HttpHeadersImpl( message.getExchange().getInMessage() );
                    String subutaiHeader = headers.getHeaderString( Common.SUBUTAI_HTTP_HEADER );

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
                            //                            LOG.debug( "Path handled by environment crypto handler: " +
                            // path );
                        }
                        else
                        {
                            //                            LOG.warn( "Path is not handled by crypto handler: " + path );
                        }
                    }
                }
            }

            //-----------------------------------------------------------------------------------------------
        }
        catch ( Exception e )
        {
            throw new Fault( e );
        }
    }


    private void handlePeerMessage( final String targetId, final Message message )
    {
        String sourceId = peerManager.getLocalPeer().getId();
        MessageContentUtil.decryptContent( channelManagerImpl.getSecurityManager(), message, sourceId, targetId );
    }


    private void handleEnvironmentMessage( final String peerId, final String environmentId, final Message message )
    {
        String targetId = peerId + "_" + environmentId;
        String sourceId = peerManager.getLocalPeer().getId() + "_" + environmentId;

        MessageContentUtil.decryptContent( channelManagerImpl.getSecurityManager(), message, sourceId, targetId );
    }
    //******************************************************************
}