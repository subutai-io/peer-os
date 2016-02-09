package io.subutai.core.channel.impl.interceptor;


import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.jaxrs.impl.HttpHeadersImpl;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.transport.http.AbstractHTTPDestination;

import io.subutai.common.peer.PeerException;
import io.subutai.common.settings.ChannelSettings;
import io.subutai.common.settings.Common;
import io.subutai.core.channel.impl.ChannelManagerImpl;
import io.subutai.core.channel.impl.util.InterceptorState;
import io.subutai.core.channel.impl.util.MessageContentUtil;
import io.subutai.core.peer.api.PeerManager;


/**
 *
 */
public class ClientInInterceptor extends AbstractPhaseInterceptor<Message>
{

    //    private static final Logger LOG = LoggerFactory.getLogger( ClientInInterceptor.class );
    private ChannelManagerImpl channelManagerImpl = null;
    private PeerManager peerManager;


    //******************************************************************
    public ClientInInterceptor( ChannelManagerImpl channelManagerImpl, PeerManager peerManager )
    {
        super( Phase.PRE_STREAM );
        this.channelManagerImpl = channelManagerImpl;
        this.peerManager = peerManager;
    }
    //******************************************************************


    @Override
    public void handleMessage( final Message message )
    {
        if ( !channelManagerImpl.isEncryptionEnabled() )
        {
            return;
        }

        try
        {
            if ( InterceptorState.CLIENT_IN.isActive( message ) )
            {
                //                LOG.debug( " ****** Client InInterceptor invoked ******** " );
                HttpServletRequest req = ( HttpServletRequest ) message.getExchange().getOutMessage()
                                                                       .get( AbstractHTTPDestination.HTTP_REQUEST );

                URL url = new URL( ( String ) message.getExchange().getOutMessage().get( Message.ENDPOINT_ADDRESS ) );

                if ( req.getLocalPort() == Integer.parseInt( ChannelSettings.SECURE_PORT_X2 ) )
                {
                    String path = req.getRequestURI();
                    String ip = url.getHost();
                    HttpHeaders headers = new HttpHeadersImpl( message.getExchange().getInMessage() );
                    String spHeader = headers.getHeaderString( Common.SUBUTAI_HTTP_HEADER );
                    if ( path.startsWith( "/rest/v1/peer" ) )
                    {
                        handlePeerMessage( ip, message );
                        //                        LOG.debug( "Path handled by peer crypto handler: " + path );
                    }
                    else
                    {
                        final String prefix = "/rest/v1/env";
                        if ( path.startsWith( prefix ) )
                        {
                            String s = path.substring( prefix.length() + 1 );
                            String environmentId = s.substring( 0, s.indexOf( "/" ) );
                            handleEnvironmentMessage( ip, environmentId, message );
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
        }
        catch ( Exception ex )
        {
            throw new Fault( ex );
        }
    }


    private void handlePeerMessage( final String ip, final Message message )
    {
        try
        {
            String targetId = peerManager.getPeerIdByIp( ip );
            String sourceId = peerManager.getLocalPeer().getId();
            MessageContentUtil.decryptContent( channelManagerImpl.getSecurityManager(), message, sourceId, targetId );
        }
        catch ( PeerException e )
        {
            //            LOG.warn( e.getMessage() );
        }
    }


    private void handleEnvironmentMessage( final String ip, final String environmentId, final Message message )
    {
        try
        {
            String targetId = peerManager.getPeerIdByIp( ip ) + "-" + environmentId;
            String sourceId = peerManager.getLocalPeer().getId() + "-" + environmentId;

            MessageContentUtil.decryptContent( channelManagerImpl.getSecurityManager(), message, sourceId, targetId );
        }
        catch ( PeerException e )
        {
            //            LOG.warn( e.getMessage() );
        }
    }
}
