package io.subutai.core.channel.impl.interceptor;


import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cxf.jaxrs.impl.HttpHeadersImpl;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.transport.http.AbstractHTTPDestination;

import com.google.common.base.Strings;

import io.subutai.common.settings.ChannelSettings;
import io.subutai.common.settings.Common;
import io.subutai.core.channel.impl.ChannelManagerImpl;
import io.subutai.core.channel.impl.util.MessageContentUtil;


/**
 * Out Interceptor
 */
public class ServerOutInterceptor extends AbstractPhaseInterceptor<Message>
{

    private static final Logger LOG = LoggerFactory.getLogger( ServerOutInterceptor.class );
    private ChannelManagerImpl channelManagerImpl = null;


    public ServerOutInterceptor( ChannelManagerImpl channelManagerImpl )
    {
        super( Phase.PRE_STREAM );
        this.channelManagerImpl = channelManagerImpl;
    }


    /**
     * Intercepts a message. interceptor chain will take care of this.
     */
    @Override
    public void handleMessage( final Message message )
    {
        try
        {
            if ( InterceptorState.SERVER_OUT.isActive( message ) )
            {
                LOG.info( "Server OutInterceptor invoked " );

                URL url = new URL( ( String ) message.getExchange().getInMessage().get( Message.REQUEST_URL ) );

                if ( url.getPort() == Integer.parseInt( ChannelSettings.SECURE_PORT_X2 ) )
                {
                    HttpHeaders headers = new HttpHeadersImpl( message.getExchange().getInMessage() );

                    String spHeader = headers.getHeaderString( Common.HEADER_SPECIAL );

                    if (!Strings.isNullOrEmpty( spHeader ) )
                    {
                        HttpServletRequest req = ( HttpServletRequest ) message.getExchange().getInMessage()
                                                                               .get( AbstractHTTPDestination
                                                                                       .HTTP_REQUEST );
                        String remoteIp = req.getRemoteAddr();
                        String envIdIn = headers.getHeaderString( Common.HEADER_ENV_ID_SOURCE );
                        String peerIdIn = headers.getHeaderString( Common.HEADER_PEER_ID_SOURCE );

                        if ( !Strings.isNullOrEmpty( envIdIn ) )
                        {
                            MessageContentUtil.encryptMessageContent( channelManagerImpl.getSecurityManager(), envIdIn,
                                    remoteIp, message );
                        }
                        else if ( !Strings.isNullOrEmpty( peerIdIn ) )
                        {
                            MessageContentUtil.encryptMessageContent( channelManagerImpl.getSecurityManager(), peerIdIn,
                                    remoteIp, message );
                        }
                    }
                }
                //***********************************************************************
            }
        }
        catch ( Exception ignore )
        {
            LOG.debug( "MalformedURLException", ignore.toString() );
        }
    }

}
