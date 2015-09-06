package io.subutai.core.channel.impl.test;


import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.io.CacheAndWriteOutputStream;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.io.CachedOutputStreamCallback;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;


public class Step2Interceptor extends AbstractPhaseInterceptor<Message>
{
    private static final Logger LOG = LoggerFactory.getLogger( Step2Interceptor.class );


    public Step2Interceptor()
    {
        super( Phase.RECEIVE );
    }


    @Override
    public void handleMessage( final Message message ) throws Fault
    {
        if ( StepUtil.isActive( message, Step.TWO ) )
        {
            OutputStream out = message.getContent( OutputStream.class );
            final CacheAndWriteOutputStream newOut = new CacheAndWriteOutputStream( out );
            message.setContent( OutputStream.class, newOut );
            newOut.registerCallback( new LoggingCallback() );
        }
    }


    public class LoggingCallback implements CachedOutputStreamCallback
    {

        @Override
        public void onFlush( CachedOutputStream cos )
        {
        }


        @Override
        public void onClose( CachedOutputStream cos )
        {
            try
            {
                StringBuilder builder = new StringBuilder();
                cos.writeCacheTo( builder );
                String out = builder.toString();
                System.out.println( String.format( "STEP 2%n%s", out ) );
            }
            catch ( Exception e )
            {
                LOG.error( "STEP 2 error", e );
            }
        }
    }
}
