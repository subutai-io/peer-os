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
public class Step2Interceptor extends AbstractPhaseInterceptor<Message>
{
    private static final Logger LOG = LoggerFactory.getLogger( Step2Interceptor.class );


    public Step2Interceptor(  )
    {
        super( Phase.RECEIVE );
    }


    @Override
    public void handleMessage( final Message message ) throws Fault
    {
        if ( StepUtil.isActive( message, Step.TWO ) )
        {
            byte[] content = message.getContent( byte[].class );
            LOG.debug( "STEP 2" );
            LOG.debug( new String( content ) );
        }
    }
}
