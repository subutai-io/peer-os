package io.subutai.core.channel.impl.test;


import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;


public class Step4Interceptor extends AbstractPhaseInterceptor<Message>
{
    private static final Logger LOG = LoggerFactory.getLogger( Step4Interceptor.class );


    public Step4Interceptor()
    {
        super( Phase.RECEIVE );
    }


    @Override
    public void handleMessage( final Message message ) throws Fault
    {
        if ( StepUtil.isActive( message, Step.FOUR ) )
        {
            InputStream is = message.getContent( InputStream.class );
            CachedOutputStream os = new CachedOutputStream();
            try
            {
                IOUtils.copy( is, os );
                os.flush();
                message.setContent( InputStream.class, os.getInputStream() );
                is.close();
                LOG.warn( String.format( "STEP 4%n%s", IOUtils.toString( os.getInputStream() ) ) );
            }
            catch ( IOException e )
            {
                LOG.error( "STEP 4 error", e );
            }
        }
    }
}
