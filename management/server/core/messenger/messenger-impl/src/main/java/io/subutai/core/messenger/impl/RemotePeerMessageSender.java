package io.subutai.core.messenger.impl;


import java.util.Set;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.client.WebClient;

import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.PeerInfo;
import io.subutai.common.security.WebClientBuilder;
import io.subutai.common.util.JsonUtil;


/**
 * Delivers messages to a specific peer
 */
public class RemotePeerMessageSender implements Callable<Boolean>
{
    private static final Logger LOG = LoggerFactory.getLogger( RemotePeerMessageSender.class.getName() );

    private Peer localPeer;
    private Peer targetPeer;
    private Set<Envelope> envelopes;

    private MessengerDao messengerDao;


    public RemotePeerMessageSender( MessengerDao messengerDao, final Peer localPeer, final Peer targetPeer,
                                    final Set<Envelope> envelopes )
    {
        this.localPeer = localPeer;
        this.targetPeer = targetPeer;
        this.envelopes = envelopes;
        this.messengerDao = messengerDao;
    }


    @Override
    public Boolean call()
    {
        WebClient client = null;
        try
        {
            client = getWebClient( localPeer.getId(), targetPeer.getPeerInfo() );
            for ( Envelope envelope : envelopes )
            {
                try
                {
                    client.post( JsonUtil.toJson( envelope ) );

                    messengerDao.markAsSent( envelope );
                }
                catch ( Exception e )
                {
                    messengerDao.incrementDeliveryAttempts( envelope );

                    LOG.error( "Error in PeerMessenger", e );

                    //break transmission of all subsequent messages for this peer in this round
                    break;
                }
            }
            return true;
        }
        catch ( PeerException e )
        {
            LOG.error( e.getMessage() );
        }

        return false;
    }


    protected WebClient getWebClient( String localPeerId, PeerInfo peerInfo )
    {
        return WebClientBuilder.buildPeerWebClient( localPeerId, peerInfo, "/messenger/message" );
    }
}
