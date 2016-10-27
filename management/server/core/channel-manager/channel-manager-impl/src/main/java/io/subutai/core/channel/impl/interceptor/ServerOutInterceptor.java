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
import io.subutai.core.channel.impl.util.InterceptorState;
import io.subutai.core.channel.impl.util.MessageContentUtil;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.security.api.SecurityManager;


/**
 * Out Interceptor
 */
public class ServerOutInterceptor extends AbstractPhaseInterceptor<Message>
{

    private final PeerManager peerManager;
    private final SecurityManager securityManager;


    public ServerOutInterceptor( SecurityManager securityManager, PeerManager peerManager )
    {
        super( Phase.PRE_STREAM );
        this.securityManager = securityManager;
        this.peerManager = peerManager;
    }


    /**
     * Intercepts a message. interceptor chain will take care of this.
     */
    @Override
    public void handleMessage( final Message message )
    {
        try
        {
            if ( InterceptorState.SERVER_OUT.isActive( message ) )
            {
                //obtain client request
                HttpServletRequest req = ( HttpServletRequest ) message.getExchange().getInMessage()
                                                                       .get( AbstractHTTPDestination.HTTP_REQUEST );

                if ( req.getLocalPort() == Common.DEFAULT_PUBLIC_SECURE_PORT )
                {
                    HttpHeaders headers = new HttpHeadersImpl( message.getExchange().getInMessage() );
                    String subutaiHeader = headers.getHeaderString( Common.SUBUTAI_HTTP_HEADER );
                    String path = req.getRequestURI();

                    if ( path.startsWith( "/rest/v1/peer" ) )
                    {
                        handlePeerMessage( subutaiHeader, message );
                    }
                    else
                    {
                        final String prefix = "/rest/v1/env";
                        if ( path.startsWith( prefix ) )
                        {
                            String s = path.substring( prefix.length() + 1 );
                            String environmentId = s.substring( 0, s.indexOf( "/" ) );
                            handleEnvironmentMessage( subutaiHeader, environmentId, message );
                        }
                    }
                }
            }
        }
        catch ( Exception e )
        {
            throw new Fault( e );
        }
    }


    protected void handlePeerMessage( final String targetId, final Message message )
    {
        String sourceId = peerManager.getLocalPeer().getId();

        MessageContentUtil.encryptContent( securityManager, sourceId, targetId, message );
    }


    protected void handleEnvironmentMessage( final String peerId, final String environmentId, final Message message )
    {
        String sourceId = peerManager.getLocalPeer().getId() + "_" + environmentId;
        String targetId = peerId + "_" + environmentId;

        MessageContentUtil.encryptContent( securityManager, sourceId, targetId, message );
    }
}
