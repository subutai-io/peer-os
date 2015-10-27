package io.subutai.core.channel.impl.interceptor;


import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

import io.subutai.common.peer.PeerException;
import io.subutai.common.settings.ChannelSettings;
import io.subutai.core.channel.impl.ChannelManagerImpl;
import io.subutai.core.channel.impl.util.MessageContentUtil;
import io.subutai.core.peer.api.PeerManager;


/**
 *
 */
public class ClientOutInterceptor extends AbstractPhaseInterceptor<Message>
{
    private static final Logger LOG = LoggerFactory.getLogger( ClientOutInterceptor.class );
    private final PeerManager peerManager;

    private ChannelManagerImpl channelManagerImpl = null;


    //******************************************************************
    public ClientOutInterceptor( ChannelManagerImpl channelManagerImpl, PeerManager peerManager )
    {
        super( Phase.PRE_STREAM );
        this.channelManagerImpl = channelManagerImpl;
        this.peerManager = peerManager;
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

                URL url = getUrl( message );

                if ( url.getPort() == Integer.parseInt( ChannelSettings.SECURE_PORT_X2 ) )
                {
                    String path = url.getPath();
                    String ip = url.getHost();

                    if ( path.startsWith( "/rest/v1/peer" ) )
                    {
                        handlePeerMessage( ip, message );
                        LOG.debug( "Path handled by peer crypto handler: " + path );
                    }
                    else
                    {
                        final String prefix = "/rest/v1/env";
                        if ( path.startsWith( prefix ) )
                        {
                            String s = path.substring( prefix.length() + 1 );
                            String environmentId = s.substring( 0, s.indexOf( "/" ) );
                            handleEnvironmentMessage( ip, environmentId, message );
                            LOG.debug( "Path handled by environment crypto handler: " + path );
                        }
                        else
                        {
                            LOG.warn( "Path is not handled by crypto handler: " + path );
                        }
                    }
                }
            }
        }
        catch ( Exception ex )
        {
            LOG.warn( ex.getMessage() );
        }
    }


    private URL getUrl( final Message message ) throws MalformedURLException
    {
        return new URL( ( String ) message.getExchange().getOutMessage().get( Message.ENDPOINT_ADDRESS ) );
    }


    private void handlePeerMessage( final String ip, final Message message )
    {
        try
        {
            String targetId = peerManager.getPeerIdByIp( ip );
            String sourceId = peerManager.getLocalPeerInfo().getId();
            MessageContentUtil
                    .encryptContent( channelManagerImpl.getSecurityManager(), sourceId, targetId, ip, message );
        }
        catch ( PeerException e )
        {
            LOG.warn( e.getMessage() );
        }
    }


    private void handleEnvironmentMessage( final String ip, final String environmentId, final Message message )
    {
        try
        {
            String targetId = peerManager.getPeerIdByIp( ip ) + "-" + environmentId;
            String sourceId = peerManager.getLocalPeerInfo().getId() + "-" + environmentId;

            MessageContentUtil
                    .encryptContent( channelManagerImpl.getSecurityManager(), sourceId, targetId, ip, message );
        }
        catch ( PeerException e )
        {
            LOG.warn( e.getMessage() );
        }
    }
}
