package io.subutai.core.channel.impl.test;


import java.io.IOException;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;


/**
 * Created by dilshat on 9/6/15.
 */
public class Step3Interceptor extends AbstractPhaseInterceptor<Message>
{
    private static final Logger LOG = LoggerFactory.getLogger( Step3Interceptor.class );


    public Step3Interceptor()
    {
        super( Phase.PRE_STREAM );
    }


    @Override
    public void handleMessage( final Message message ) throws Fault
    {
        if ( StepUtil.isActive( message, Step.THREE ) )
        {
            LOG.debug( "STEP 3" );

            OutputStream os = message.getContent( OutputStream.class );

            CachedStream cs = new CachedStream();
            message.setContent( OutputStream.class, cs );

            message.getInterceptorChain().doIntercept( message );

            try
            {
                cs.flush();
                CachedOutputStream csnew = ( CachedOutputStream ) message.getContent( OutputStream.class );

                if ( csnew.getBytes() != null )
                {
                    LOG.debug( new String( csnew.getBytes() ) );
                }
                //                GZIPOutputStream zipOutput = new GZIPOutputStream( os );
                //                CachedOutputStream.copyStream( csnew.getInputStream(), zipOutput, 1024 );
                //
                //                cs.close();
                //                zipOutput.close();
                //                os.flush();

                message.setContent( OutputStream.class, os );
            }
            catch ( IOException ioe )
            {
                LOG.warn( "Unable to perform change.", ioe );
                throw new RuntimeException( ioe );
            }
        }
    }
}
