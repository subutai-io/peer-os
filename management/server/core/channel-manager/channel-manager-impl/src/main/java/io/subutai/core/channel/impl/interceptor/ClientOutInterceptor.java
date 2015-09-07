package io.subutai.core.channel.impl.interceptor;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

import io.subutai.core.channel.impl.ChannelManagerImpl;


/**
 *
 */
public class ClientOutInterceptor extends AbstractPhaseInterceptor<Message>
{

    private static final Logger LOG = LoggerFactory.getLogger( ClientOutInterceptor.class );
    private ChannelManagerImpl channelManagerImpl = null;

    //******************************************************************
    public ClientOutInterceptor( ChannelManagerImpl channelManagerImpl )
    {
        super( Phase.PRE_STREAM);
        this.channelManagerImpl = channelManagerImpl;
    }
    //******************************************************************


    @Override
    public void handleMessage( final Message message )
    {
        try
        {
            if ( InterceptorState.CLIENT_OUT.isActive( message ) )
            {
                LOG.info( " ****** Client OutInterceptor invoked ******** " );
            }
        }
        catch(Exception ex)
        {

        }
    }
}
