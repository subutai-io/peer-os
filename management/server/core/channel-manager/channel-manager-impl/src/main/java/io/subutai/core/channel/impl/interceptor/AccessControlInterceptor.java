package io.subutai.core.channel.impl.interceptor;


import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.PrivilegedAction;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.interceptor.InterceptorChain;
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
import io.subutai.core.identity.api.model.User;


/**
 * CXF interceptor that controls channel (tunnel)
 */
public class AccessControlInterceptor extends AbstractPhaseInterceptor<Message>
{
    private static final Logger LOG = LoggerFactory.getLogger( AccessControlInterceptor.class );
    private ChannelManagerImpl channelManagerImpl = null;


    public AccessControlInterceptor( ChannelManagerImpl channelManagerImpl )
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

            status = MessageContentUtil.checkUrlAccessibility( status, url, basePath );
            //----------------------------------------------------------------------------------------------
            //--------------- if error occurs --------------------------------------------------------------
            if ( status != 0 )
            {
                String error = "";
                int errorStatus = 0;

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

                MessageContentUtil.abortChain(message,errorStatus,error);

            }
            else
            {

                User user = channelManagerImpl.getIdentityManager().login( "admin","secret");

                if(user!=null)
                {
                    Subject.doAs( user.getSubject(), new PrivilegedAction<Void>()
                    {
                        @Override
                        public Void run()
                        {
                            try
                            {
                                message.getInterceptorChain().doIntercept( message );
                            }
                            catch(Exception ex)
                            {
                                MessageContentUtil.abortChain(message,403,"Access Denied");
                            }
                            return null;
                        }
                    });
                }
                else
                {
                    MessageContentUtil.abortChain( message, 403, "Access Denied" );
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