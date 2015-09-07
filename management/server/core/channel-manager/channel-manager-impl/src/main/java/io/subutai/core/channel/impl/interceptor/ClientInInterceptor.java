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
public class ClientInInterceptor extends AbstractPhaseInterceptor<Message>
{

    private static final Logger LOG = LoggerFactory.getLogger( ClientInInterceptor.class );
    private ChannelManagerImpl channelManagerImpl = null;

    //******************************************************************
    public ClientInInterceptor( ChannelManagerImpl channelManagerImpl )
    {
        super( Phase.RECEIVE);
        this.channelManagerImpl = channelManagerImpl;
    }
    //******************************************************************


    @Override
    public void handleMessage( final Message message )
    {
        try
        {
            if(InterceptorState.isActive( message,InterceptorState.CLIENT_IN ))
            {
                LOG.info( " ****** Client InInterceptor invoked ******** " );
            }
        }
        catch(Exception ex)
        {

        }
    }
}
