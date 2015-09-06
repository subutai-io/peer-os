package io.subutai.core.channel.impl.test;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;


/**
 * Created by dilshat on 9/6/15.
 */
public class Step1Interceptor extends AbstractPhaseInterceptor<Message>
{
    private static final Logger LOG = LoggerFactory.getLogger( Step1Interceptor.class );


    public Step1Interceptor()
    {
        super( Phase.PRE_PROTOCOL );
    }


    @Override
    public void handleMessage( final Message message ) throws Fault
    {
        if ( StepUtil.isActive( message, Step.ONE ) )
        {
            byte[] content = message.getContent( byte[].class );
            LOG.debug( "STEP 1" );
            LOG.debug( new String( content ) );
        }
    }
}
