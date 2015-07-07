package io.subutai.core.channel.impl.interceptor;


import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

import javax.servlet.http.HttpServletResponse;

import org.safehaus.subutai.common.settings.ChannelSettings;
import org.safehaus.subutai.common.util.IPUtil;
import org.safehaus.subutai.common.util.UrlUtil;
import io.subutai.core.channel.api.entity.IUserChannelToken;
import io.subutai.core.channel.impl.ChannelManagerImpl;
import io.subutai.core.identity.api.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.transport.http.AbstractHTTPDestination;


/**
 * CXF interceptor that controls channel (tunnel)
 */
public class CXFInterceptor extends AbstractPhaseInterceptor<Message>
{
    private static final Logger LOG = LoggerFactory.getLogger( CXFInterceptor.class );
    private ChannelManagerImpl channelManagerImpl = null;


    public CXFInterceptor( ChannelManagerImpl channelManagerImpl )
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
                /*
                else
                {
                    User user  = channelManagerImpl.getIdentityManager().getUser();

                    if(channelManagerImpl.getIdentityManager().checkRestPermissions( user,basePath ) != 1)
                    {
                        status = 1;
                    }
                }*/
        }
        else if ( url.getPort() == Integer.parseInt( ChannelSettings.SECURE_PORT_X2 ) )
        {
            if ( ChannelSettings.checkURLArray( basePath, ChannelSettings.URL_ACCESS_PX2 ) == 0 )
            {
                status = 1;
            }
                /*
                else
                {
                    User user  = channelManagerImpl.getIdentityManager().getUser();

                    if(channelManagerImpl.getIdentityManager().checkRestPermissions( user,basePath ) != 1)
                    {
                        status = 1;
                    }
                }*/
        }
        else if ( url.getPort() == Integer.parseInt( ChannelSettings.SECURE_PORT_X3 ) )
        {
            //----------------------------------------------------------------------
        }
        else if ( url.getPort() == Integer.parseInt( ChannelSettings.SPECIAL_PORT_X1 ) || url.getPort() == Integer
                .parseInt( ChannelSettings.SPECIAL_SECURE_PORT_X1 ) )
        {
            String query = ( String ) message.get( Message.QUERY_STRING );
            String paramValue = UrlUtil.getQueryParameterValue( "sptoken", query );

            if ( !"".equals( paramValue ) )
            {
                IUserChannelToken userChannelToken =
                        channelManagerImpl.getChannelTokenManager().getUserChannelToken( paramValue );
                status = validateUserPermissions( userChannelToken, url, basePath, status );
            }
            else
            {
                status = 1;
            }
        }
        return status;
    }


    private int validateUserPermissions( final IUserChannelToken userChannelToken, final URL url, final String basePath,
                                         final int currentStatus )
    {
        int status = currentStatus;
        if ( userChannelToken != null )
        {
            if ( IPUtil.isValidIPRange( userChannelToken.getIpRangeStart(), userChannelToken.getIpRangeStart(),
                    url.getHost() ) )
            {
                User user = channelManagerImpl.getIdentityManager().getUser( userChannelToken.getUserId() );
                if ( channelManagerImpl.getIdentityManager().checkRestPermissions( user, basePath ) != 1 )
                {
                    status = 1;
                }
                else
                {
                    channelManagerImpl.getIdentityManager().loginWithToken( user.getUsername() );
                }
            }
            else
            {
                status = 1;
            }
        }
        else
        {
            status = 1;
        }
        return status;
    }
}