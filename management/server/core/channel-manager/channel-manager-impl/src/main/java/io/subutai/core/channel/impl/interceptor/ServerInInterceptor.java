package io.subutai.core.channel.impl.interceptor;


import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

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

import io.subutai.common.settings.ChannelSettings;
import io.subutai.common.settings.Common;
import io.subutai.core.channel.impl.ChannelManagerImpl;
import io.subutai.core.channel.impl.util.MessageContentUtil;


/**
 * CXF interceptor that controls channel (tunnel)
 */
public class ServerInInterceptor extends AbstractPhaseInterceptor<Message>
{
    private static final Logger LOG = LoggerFactory.getLogger( ServerInInterceptor.class );
    private ChannelManagerImpl channelManagerImpl = null;


    public ServerInInterceptor( ChannelManagerImpl channelManagerImpl )
    {
        super( Phase.RECEIVE );
        this.channelManagerImpl = channelManagerImpl;
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

                status = MessageContentUtil.checkUrlAccessibility( status, url, basePath );
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
                        String spHeader =  headers.getHeaderString( Common.HEADER_SPECIAL );
                        LOG.info( " *** URL:" + url.getPath());

                        if ( !Strings.isNullOrEmpty( spHeader ) )
                        {
                            String envIdSource = headers.getHeaderString( Common.HEADER_ENV_ID_SOURCE );
                            String envIdTarget = headers.getHeaderString( Common.HEADER_ENV_ID_TARGET );

                            String peerIdSource = headers.getHeaderString( Common.HEADER_PEER_ID_SOURCE );
                            String peerIdTarget = headers.getHeaderString( Common.HEADER_PEER_ID_TARGET );

                            if ( !Strings.isNullOrEmpty( envIdSource ) )
                            {
                                MessageContentUtil.decryptContent( channelManagerImpl.getSecurityManager(), message,
                                        envIdTarget,envIdSource );
                            }
                            else if ( !Strings.isNullOrEmpty( peerIdSource ) )
                            {
                                MessageContentUtil.decryptContent( channelManagerImpl.getSecurityManager(), message,
                                        peerIdTarget,peerIdSource );
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


    //******************************************************************
}