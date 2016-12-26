package io.subutai.core.messenger.impl;


import java.util.Set;
import java.util.concurrent.Callable;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.client.WebClient;

import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerInfo;
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

    private MessengerDataService messengerDataService;


    public RemotePeerMessageSender( MessengerDataService messengerDataService, final Peer targetPeer, final Set<Envelope> envelopes )
    {
        this.targetPeer = targetPeer;
        this.envelopes = envelopes;
        this.messengerDataService = messengerDataService;
    }


    @Override
    public Boolean call()
    {
        WebClient client = null;
        try
        {
            client = getWebClient( targetPeer.getPeerInfo() );
            for ( Envelope envelope : envelopes )
            {
                Response response = null;
                try
                {
                    response = client.post( JsonUtil.toJson( envelope ) );

                    WebClientBuilder.checkResponse( response, Response.Status.ACCEPTED );

                    messengerDataService.markAsSent( envelope );
                }
                catch ( Exception e )
                {
                    messengerDataService.incrementDeliveryAttempts( envelope );

                    LOG.error( "Error in RemotePeerMessageSender", e );

                    //break transmission of all subsequent messages for this peer in this round
                    break;
                }
            }
            return true;
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage() );
        }
        finally
        {
            WebClientBuilder.close( client );
        }

        return false;
    }


    protected WebClient getWebClient( PeerInfo peerInfo )
    {
        return WebClientBuilder.buildPeerWebClient( peerInfo, "/messenger/message" );
    }
}
