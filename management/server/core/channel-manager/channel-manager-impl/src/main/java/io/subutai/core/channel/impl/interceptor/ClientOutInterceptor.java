package io.subutai.core.channel.impl.interceptor;


import java.net.URL;

import javax.ws.rs.core.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cxf.jaxrs.impl.HttpHeadersImpl;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

import com.google.common.base.Strings;

import io.subutai.common.settings.ChannelSettings;
import io.subutai.common.settings.Common;
import io.subutai.core.channel.impl.ChannelManagerImpl;
import io.subutai.core.channel.impl.util.MessageContentUtil;


/**
 *
 */
public class ClientOutInterceptor extends AbstractPhaseInterceptor<Message>
{

    private static final Logger LOG = LoggerFactory.getLogger( ClientOutInterceptor.class );
    private ChannelManagerImpl channelManagerImpl = null;


    //******************************************************************
    public ClientOutInterceptor( ChannelManagerImpl channelManagerImpl )
    {
        super( Phase.PRE_STREAM );
        this.channelManagerImpl = channelManagerImpl;
    }
    //******************************************************************


    @Override
    public void handleMessage( final Message message )
    {
        try
        {
            if ( InterceptorState.CLIENT_OUT.isActive( message ) )
            {
                LOG.info( " ****** Client OutInterceptor invoked ******** " );

                URL url = new URL( ( String ) message.getExchange().getOutMessage().get( Message.ENDPOINT_ADDRESS ) );

                if ( url.getPort() == Integer.parseInt( ChannelSettings.SECURE_PORT_X2 ) )
                {
                    HttpHeaders headers = new HttpHeadersImpl( message );

                    LOG.info( " *** URL:" + url.getPath());

                    String spHeader = headers.getHeaderString( Common.HEADER_SPECIAL );

                    if ( !Strings.isNullOrEmpty( spHeader ) )
                    {
                        String envIdOut = headers.getHeaderString( Common.HEADER_ENV_ID_TARGET );
                        String peerIdOut = headers.getHeaderString( Common.HEADER_PEER_ID_TARGET );
                        String ip = url.getHost();

                        if ( !Strings.isNullOrEmpty( envIdOut ) )
                        {
                            MessageContentUtil.encryptMessageContent( channelManagerImpl.getSecurityManager(), envIdOut,
                                    ip, message );
                        }
                        else if ( !Strings.isNullOrEmpty( peerIdOut ) )
                        {
                            MessageContentUtil.encryptMessageContent( channelManagerImpl.getSecurityManager(), peerIdOut,
                                    ip, message );
                        }
                    }
                }
            }
        }
        catch ( Exception ex )
        {

        }
    }
}
