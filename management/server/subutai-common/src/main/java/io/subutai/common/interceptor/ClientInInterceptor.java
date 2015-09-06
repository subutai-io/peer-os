package io.subutai.common.interceptor;


import java.net.MalformedURLException;
import java.net.URL;

import javax.ws.rs.core.HttpHeaders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.jaxrs.impl.HttpHeadersImpl;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

import com.google.common.base.Strings;

import io.subutai.common.settings.Common;


/**
 *
 */
public class ClientInInterceptor extends AbstractPhaseInterceptor<Message>
{
    private final static Logger LOG = LoggerFactory.getLogger( ClientInInterceptor.class );

    public ClientInInterceptor()
    {
        super( Phase.RECEIVE );
    }


    @Override
    public void handleMessage( final Message message ) throws Fault
    {
        LOG.info( " ********* Client InInterceptor invoked *********** " );

        try
        {
            URL url = new URL( ( String ) message.get( Message.REQUEST_URL ) );
            HttpHeaders headers = new HttpHeadersImpl(message.getExchange().getInMessage());




        }
        catch ( MalformedURLException ignore )
        {
            LOG.debug( "MalformedURLException", ignore.toString() );
        }

    }
}
