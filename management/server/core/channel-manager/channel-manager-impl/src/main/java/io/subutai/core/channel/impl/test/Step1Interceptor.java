package io.subutai.core.channel.impl.test;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.io.IOUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;


public class Step1Interceptor extends AbstractPhaseInterceptor<Message>
{
    private static final Logger LOG = LoggerFactory.getLogger( Step1Interceptor.class );


    public Step1Interceptor()
    {
        super( Phase.PRE_STREAM_ENDING );
    }


    @Override
    public void handleMessage( final Message message ) throws Fault
    {
        if ( StepUtil.isActive( message, Step.ONE ) )
        {
            OutputStream os = message.getContent( OutputStream.class );

            CachedStream cs = new CachedStream();
            message.setContent( OutputStream.class, cs );

            message.getInterceptorChain().doIntercept( message );

            try
            {
                cs.flush();
                IOUtils.closeQuietly( cs );
                CachedOutputStream csnew = ( CachedOutputStream ) message.getContent( OutputStream.class );

                byte[] originalMessage = IOUtils.toByteArray( csnew.getInputStream() );
                csnew.flush();
                IOUtils.closeQuietly( csnew );

                //do something with original message to produce finalMessage
                byte[] finalMessage = new byte[100];


                InputStream replaceInStream = new ByteArrayInputStream( finalMessage );

                IOUtils.copy( replaceInStream, os );
                replaceInStream.close();
                IOUtils.closeQuietly( replaceInStream );

                os.flush();
                message.setContent( OutputStream.class, os );
                IOUtils.closeQuietly( os );
            }
            catch ( IOException ioe )
            {
                LOG.warn( "Unable to perform change.", ioe );
                throw new RuntimeException( ioe );
            }
        }
    }
}
