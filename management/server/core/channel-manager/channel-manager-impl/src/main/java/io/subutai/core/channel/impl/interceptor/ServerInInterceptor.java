package io.subutai.core.channel.impl.interceptor;


import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.impl.HttpHeadersImpl;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.transport.http.AbstractHTTPDestination;

import com.google.common.base.Strings;

import io.subutai.common.peer.PeerException;
import io.subutai.common.settings.ChannelSettings;
import io.subutai.common.settings.Common;
import io.subutai.core.channel.impl.ChannelManagerImpl;
import io.subutai.core.channel.impl.util.MessageContentUtil;
import io.subutai.core.peer.api.PeerManager;


/**
 * CXF interceptor that controls channel (tunnel)
 */
public class ServerInInterceptor extends AbstractPhaseInterceptor<Message>
{
    private static final Logger LOG = LoggerFactory.getLogger( ServerInInterceptor.class );
    private ChannelManagerImpl channelManagerImpl = null;
    private PeerManager peerManager;


    public ServerInInterceptor( ChannelManagerImpl channelManagerImpl, PeerManager peerManager )
    {
        super( Phase.RECEIVE );
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
        try
        {
            if ( InterceptorState.SERVER_IN.isActive( message ) )
            {
                URL url = new URL( ( String ) message.get( Message.REQUEST_URL ) );

                String basePath = url.getPath();
                int status = 0;

                //                status = MessageContentUtil.checkUrlAccessibility( status, url, basePath );
                status = 0;
                //----------------------------------------------------------------------------------------------
                //--------------- if error occurs --------------------------------------------------------------
                if ( status != 0 )
                {
                    String error = "";
                    int errorStatus = 0;
                    HttpServletResponse response = ( HttpServletResponse ) message.getExchange().getInMessage()
                                                                                  .get( AbstractHTTPDestination
                                                                                          .HTTP_RESPONSE );
                    if ( status == 1 )
                    {
                        errorStatus = 403;
                        error = "*********  Access to " + basePath + "  is blocked (403) **********************";
                    }
                    else if ( status == 2 )
                    {
                        errorStatus = 404;
                        error = "*********  Access to " + basePath + "  is blocked (404) **********************";
                    }

                    try
                    {
                        response.setStatus( errorStatus );
                        response.getOutputStream().write( error.getBytes( Charset.forName( "UTF-8" ) ) );
                        response.getOutputStream().flush();
                    }
                    catch ( Exception e )
                    {
                        LOG.error( "Error writing to response: " + e.toString(), e );
                    }
                    LOG.warn( error );
                    message.getInterceptorChain().abort();
                }
                else
                {
                    if ( url.getPort() == Integer.parseInt( ChannelSettings.SECURE_PORT_X2 ) )
                    {
                        HttpHeaders headers = new HttpHeadersImpl( message.getExchange().getInMessage() );
                        String spHeader = headers.getHeaderString( Common.HEADER_SPECIAL );
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
                }
            }
            //-----------------------------------------------------------------------------------------------
        }
        catch ( MalformedURLException ignore )
        {
            LOG.debug( "MalformedURLException", ignore.toString() );
        }
    }


    private void handlePeerMessage( final String remoteIp, final Message message )
    {
        try
        {
            String targetId = peerManager.getPeerIdByIp( remoteIp );
            String sourceId = peerManager.getLocalPeerInfo().getId();
            MessageContentUtil.decryptContent( channelManagerImpl.getSecurityManager(), message, sourceId, targetId );
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

            MessageContentUtil.decryptContent( channelManagerImpl.getSecurityManager(), message, sourceId, targetId );
        }
        catch ( PeerException e )
        {
            LOG.warn( e.getMessage() );
        }
    }
    //******************************************************************
}