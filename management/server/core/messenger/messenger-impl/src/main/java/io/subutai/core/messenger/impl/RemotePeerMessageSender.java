package io.subutai.core.messenger.impl;


import java.util.Set;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.client.WebClient;

import io.subutai.common.peer.Peer;
import io.subutai.common.security.WebClientBuilder;
import io.subutai.common.util.JsonUtil;


/**
 * Delivers messages to a specific peer
 */
public class RemotePeerMessageSender implements Callable<Boolean>
{
    private static final Logger LOG = LoggerFactory.getLogger( RemotePeerMessageSender.class.getName() );

    private Peer targetPeer;
    private Set<Envelope> envelopes;

    private MessengerDao messengerDao;


    public RemotePeerMessageSender( MessengerDao messengerDao, final Peer targetPeer, final Set<Envelope> envelopes )
    {
        this.targetPeer = targetPeer;
        this.envelopes = envelopes;
        this.messengerDao = messengerDao;
    }


    @Override
    public Boolean call()
    {
        WebClient client = getWebClient( targetPeer.getPeerInfo().getIp() );

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


    protected WebClient getWebClient( String targetPeerIP )
    {
        return WebClientBuilder.buildPeerWebClient( targetPeerIP, "/messenger/message" );
    }
}
