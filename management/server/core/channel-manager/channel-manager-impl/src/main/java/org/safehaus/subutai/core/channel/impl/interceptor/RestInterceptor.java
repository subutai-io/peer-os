package org.safehaus.subutai.core.channel.impl.interceptor;


import java.net.MalformedURLException;
import java.net.URL;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;


/**
 * Created by talas on 2/23/15.
 */
public class RestInterceptor extends AbstractPhaseInterceptor<Message>
{
    public RestInterceptor()
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
        String basePath = ( String ) message.get( Message.REQUEST_URI );
        try
        {
            URL url = new URL( ( String ) message.get( Message.REQUEST_URL ) );
            //            if ( !basePath.contains( "peer/register" ) && url.getPort() != 8443 )
            //            {
            //                message.put( Message.RESPONSE_CODE, 403 );
            //                message.getInterceptorChain().abort();
            //            }
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
