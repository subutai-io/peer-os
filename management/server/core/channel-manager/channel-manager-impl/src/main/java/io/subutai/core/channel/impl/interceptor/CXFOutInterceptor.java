package io.subutai.core.channel.impl.interceptor;


import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

import io.subutai.core.channel.impl.ChannelManagerImpl;


/**
 * Out Interceptor
 */
public class CXFOutInterceptor  extends AbstractPhaseInterceptor<Message>
{

    private static final Logger LOG = LoggerFactory.getLogger( CXFOutInterceptor.class );
    private ChannelManagerImpl channelManagerImpl = null;


    public CXFOutInterceptor( ChannelManagerImpl channelManagerImpl )
    {
        super( Phase.RECEIVE );
        this.channelManagerImpl = channelManagerImpl;
    }


    /**
     * Intercepts a message.
     * interceptor chain will take care of this.
     */
    @Override
    public void handleMessage( final Message message ) throws Fault
    {
        try
        {
            URL url = new URL( ( String ) message.get( Message.REQUEST_URL ) );
            LOG.info( "OUT INTERCEPTOR " + message.getDestination());
        }
        catch(Exception ex)
        {

        }
    }
}
