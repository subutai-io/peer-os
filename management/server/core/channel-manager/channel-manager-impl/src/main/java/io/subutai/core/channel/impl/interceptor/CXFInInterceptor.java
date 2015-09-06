package io.subutai.core.channel.impl.interceptor;


import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.transport.http.AbstractHTTPDestination;

import io.subutai.common.settings.ChannelSettings;
import io.subutai.core.channel.impl.ChannelManagerImpl;


/**
 * CXF interceptor that controls channel (tunnel)
 */
public class CXFInInterceptor extends AbstractPhaseInterceptor<Message>
{
    private static final Logger LOG = LoggerFactory.getLogger( CXFInInterceptor.class );
    private ChannelManagerImpl channelManagerImpl = null;


    public CXFInInterceptor( ChannelManagerImpl channelManagerImpl )
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