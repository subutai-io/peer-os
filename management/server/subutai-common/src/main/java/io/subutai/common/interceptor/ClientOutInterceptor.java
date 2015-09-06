package io.subutai.common.interceptor;


import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;


/**
 *
 */
public class ClientOutInterceptor  extends AbstractPhaseInterceptor<Message>
{

    private final static Logger LOG = LoggerFactory.getLogger( ClientOutInterceptor.class );

    public ClientOutInterceptor()
    {
        super( Phase.POST_INVOKE );
    }

    @Override
    public void handleMessage( final Message message ) throws Fault
    {
        LOG.info( " ********* Client OutInterceptor invoked *********** " );

        try
        {
            URL url = new URL( ( String ) message.get( Message.REQUEST_URL ) );

        }
        catch ( MalformedURLException ignore )
        {
            LOG.debug( "MalformedURLException", ignore.toString() );
        }

    }
}
