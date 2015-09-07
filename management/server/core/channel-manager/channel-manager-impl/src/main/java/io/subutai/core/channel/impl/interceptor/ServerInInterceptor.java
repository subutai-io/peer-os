package io.subutai.core.channel.impl.interceptor;


import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

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

import io.subutai.common.settings.ChannelSettings;
import io.subutai.common.settings.Common;
import io.subutai.core.channel.impl.ChannelManagerImpl;


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

                status = checkUrlAccessibility( status, url, basePath, message );
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

                        if ( !Strings.isNullOrEmpty( headers.getHeaderString( Common.SECURED_HEADER_NAME ) ) )
                        {
                            String envId = headers.getHeaderString( Common.ENVIRONMENT_ID_HEADER_NAME );
                            String peerId = headers.getHeaderString( Common.PEER_ID_HEADER_NAME );

                            if ( !Strings.isNullOrEmpty( envId ) )
                            {

                            }
                            else if ( !Strings.isNullOrEmpty( peerId ) )
                            {

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


    private int checkUrlAccessibility( final int currentStatus, final URL url, final String basePath,
                                       final Message message )
    {
        int status = currentStatus;
        if ( url.getPort() == Integer.parseInt( ChannelSettings.SECURE_PORT_X1 ) )
        {
            if ( ChannelSettings.checkURLArray( basePath, ChannelSettings.URL_ACCESS_PX1 ) == 0 )
            {
                status = 1;
            }
        }
        else if ( url.getPort() == Integer.parseInt( ChannelSettings.SECURE_PORT_X2 ) )
        {
        }
        else if ( url.getPort() == Integer.parseInt( ChannelSettings.SECURE_PORT_X3 ) )
        {
            //----------------------------------------------------------------------
        }
        else if ( url.getPort() == Integer.parseInt( ChannelSettings.SPECIAL_PORT_X1 ) || url.getPort() == Integer
                .parseInt( ChannelSettings.SPECIAL_SECURE_PORT_X1 ) )
        {
        }
        else
        {
            status = 0;
        }
        return status;
    }
}