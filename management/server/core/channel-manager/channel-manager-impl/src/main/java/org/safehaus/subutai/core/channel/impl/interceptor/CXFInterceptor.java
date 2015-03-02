package org.safehaus.subutai.core.channel.impl.interceptor;


import java.net.MalformedURLException;
import java.net.URL;

import org.safehaus.subutai.common.settings.ChannelSettings;
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

    public CXFInterceptor()
    {
        super( Phase.RECEIVE );
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

            int status = 1;

            if(url.getPort() == Integer.parseInt( ChannelSettings.SECURE_PORT_X1))
            {
                if(ChannelSettings.checkURL(basePath,ChannelSettings.URL_ACCESS_PX1) == 0)
                {
                    status = 0;
                }
            }
            else if(url.getPort() == Integer.parseInt( ChannelSettings.SECURE_PORT_X2))
            {
                if(ChannelSettings.checkURL(basePath,ChannelSettings.URL_ACCESS_PX2) == 0)
                {
                    status = 0;
                }
            }
            else if(url.getPort() == Integer.parseInt( ChannelSettings.SECURE_PORT_X3))
            {

            }


            if(status == 0)
            {
                LOG.warn( "*********  Access to" + basePath + "  is blocked **********************" );

                message.put( Message.RESPONSE_CODE, 403 );
                message.getInterceptorChain().abort();
            }
            else
            {

            }
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
