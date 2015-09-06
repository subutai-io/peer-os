package io.subutai.core.channel.impl.interceptor;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.apache.cxf.jaxrs.impl.HttpHeadersImpl;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.transport.http.AbstractHTTPDestination;


/**
 *
 */
public class TestOutInterceptor   extends AbstractPhaseInterceptor<Message>
{

    private static final Logger LOG = LoggerFactory.getLogger( TestOutInterceptor.class );
    public TestOutInterceptor()
    {
        super( Phase.PREPARE_SEND);
    }

    @Override
    public void handleMessage( final Message message )
    {
        try
        {
            LOG.info( "Server OutInterceptor invoked " );

            HttpServletResponse res = (HttpServletResponse) message.get(AbstractHTTPDestination.HTTP_RESPONSE);
            HttpServletRequest  req = (HttpServletRequest) message.get(AbstractHTTPDestination.HTTP_REQUEST);

            Object endpointAddress = message.getExchange().get( Message.ENDPOINT_ADDRESS );
            String clientAddress = req.getRemoteAddr();

            HttpHeaders headers = new HttpHeadersImpl( message.getExchange().getInMessage() );

        }
        catch(Exception ex)
        {

        }
    }
}
