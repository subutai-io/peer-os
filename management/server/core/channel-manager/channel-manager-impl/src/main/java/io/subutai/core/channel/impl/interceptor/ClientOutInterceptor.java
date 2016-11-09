package io.subutai.core.channel.impl.interceptor;


import java.net.URL;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

import io.subutai.common.peer.PeerNotRegisteredException;
import io.subutai.core.channel.impl.util.InterceptorState;
import io.subutai.core.channel.impl.util.MessageContentUtil;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.security.api.SecurityManager;


/**
 *
 */
public class ClientOutInterceptor extends AbstractPhaseInterceptor<Message>
{
    private final PeerManager peerManager;

    private final SecurityManager securityManager;


    //******************************************************************
    public ClientOutInterceptor( SecurityManager securityManager, PeerManager peerManager )
    {
        super( Phase.PRE_STREAM );
        this.securityManager = securityManager;
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
                //obtain server url
                URL url = new URL( ( String ) message.getExchange().getOutMessage().get( Message.ENDPOINT_ADDRESS ) );

                String ip = url.getHost();
                String targetId = getPeerIdByIp( ip );
                if ( targetId != null && url.getPort() == peerManager.getPeer( targetId ).getPeerInfo()
                                                                     .getPublicSecurePort() )
                {
                    String path = url.getPath();

                    if ( path.startsWith( "/rest/v1/peer" ) )
                    {
                        handlePeerMessage( targetId, message );
                    }
                    else
                    {
                        final String prefix = "/rest/v1/env";
                        if ( path.startsWith( prefix ) )
                        {
                            String s = path.substring( prefix.length() + 1 );
                            String environmentId = s.substring( 0, s.indexOf( "/" ) );
                            handleEnvironmentMessage( targetId, environmentId, message );
                        }
                    }
                }
            }
        }
        catch ( Exception ex )
        {
            throw new Fault( ex );
        }
    }


    protected String getPeerIdByIp( String ip )
    {
        try
        {
            return peerManager.getRemotePeerIdByIp( ip );
        }
        catch ( PeerNotRegisteredException e )
        {
            return null;
        }
    }


    protected void handlePeerMessage( final String targetId, final Message message )
    {
        String sourceId = peerManager.getLocalPeer().getId();

        MessageContentUtil.encryptContent( securityManager, sourceId, targetId, message );
    }


    protected void handleEnvironmentMessage( final String peerId, final String environmentId, final Message message )
    {
        String sourceId = peerManager.getLocalPeer().getId() + "_" + environmentId;
        String targetId = peerId + "_" + environmentId;

        MessageContentUtil.encryptContent( securityManager, sourceId, targetId, message );
    }
}
