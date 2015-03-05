package org.safehaus.subutai.core.channel.impl.interceptor;


import java.net.MalformedURLException;
import java.net.URL;

import org.safehaus.subutai.common.settings.ChannelSettings;
import org.safehaus.subutai.common.util.UrlUtil;
import org.safehaus.subutai.core.channel.impl.ChannelManagerImpl;
import org.safehaus.subutai.core.identity.api.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;


/**
 * Created by nisakov on 2/23/15.
 */
public class CXFInterceptor extends AbstractPhaseInterceptor<Message>
{
    private final static Logger LOG = LoggerFactory.getLogger( CXFInterceptor.class );
    private ChannelManagerImpl channelManagerImpl = null;


    public CXFInterceptor(ChannelManagerImpl channelManagerImpl)
    {
        super( Phase.RECEIVE );
        this.channelManagerImpl = channelManagerImpl;
    }


    /**
     * Intercepts a message. Interceptors should NOT invoke handleMessage or handleFault on the next interceptor - the
     * interceptor chain will take care of this.
     */
    @Override
    public void handleMessage( final Message message ) throws Fault
    {
        try
        {
            URL url = new URL( ( String ) message.get( Message.REQUEST_URL ) );
            String basePath = url.getPath();


            int status = 0;

            if(url.getPort() == Integer.parseInt( ChannelSettings.SECURE_PORT_X1))
            {
                if(ChannelSettings.checkURL(basePath,ChannelSettings.URL_ACCESS_PX1) == 0)
                {
                    status = 1;
                }
            }
            else if(url.getPort() == Integer.parseInt( ChannelSettings.SECURE_PORT_X2))
            {
                if(ChannelSettings.checkURL(basePath,ChannelSettings.URL_ACCESS_PX2) == 0)
                {
                    status = 1;
                }
            }
            else if(url.getPort() == Integer.parseInt( ChannelSettings.SECURE_PORT_X3))
            {
                //----------------------------------------------------------------------
            }
            else if(url.getPort() == Integer.parseInt( ChannelSettings.SPECIAL_PORT_X1))
            {
                String query      =  ( String ) message.get( Message.QUERY_STRING ) ;
                String paramValue =  UrlUtil.getQueryParameterValue(  "sptoken", query );

                if(!"".equals(paramValue))
                {
                    long userId = channelManagerImpl.getChannelTokenManager().getUserChannelToken(paramValue);

                    if(userId != 0)
                    {
                        User user = channelManagerImpl.getIdentityManager().getUser(userId);
                        channelManagerImpl.getIdentityManager().loginWithToken(user.getUsername() );
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

            }


            //----------------------------------------------------------------------------------------------
            //--------------- Redirect ---------------------------------------------------------------------
            if(status == 1)
            {
                LOG.warn( "*********  Access to" + basePath + "  is blocked (403) **********************" );

                message.put( Message.RESPONSE_CODE, 403 );
                message.getInterceptorChain().abort();
            }
            else if(status == 2)
            {
                LOG.warn( "*********  Access to" + basePath + "  is blocked (404) **********************" );

                message.put( Message.RESPONSE_CODE, 403 );
                message.getInterceptorChain().abort();
            }
            else
            {

            }
            //-----------------------------------------------------------------------------------------------
        }
        catch ( MalformedURLException ignore )
        {
        }
    }


    @Override
    public void handleFault( final Message message )
    {
        super.handleFault( message );
    }


}
