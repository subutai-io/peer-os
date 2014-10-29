package org.safehaus.subutai.core.message.impl;


import java.util.Map;
import java.util.Set;

import org.safehaus.subutai.common.exception.HTTPException;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.common.util.RestUtil;
import org.safehaus.subutai.core.peer.api.Peer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;


/**
 * Delivers messages to a specific peer
 */
public class PeerMessageSender implements Runnable
{
    private static final Logger LOG = LoggerFactory.getLogger( PeerMessageSender.class.getName() );

    private Peer targetPeer;
    private Set<Envelope> envelopes;
    private RestUtil restUtil;
    private MessengerDao messengerDao;


    public PeerMessageSender( RestUtil restUtil, MessengerDao messengerDao, final Peer targetPeer,
                              final Set<Envelope> envelopes )
    {
        this.targetPeer = targetPeer;
        this.envelopes = envelopes;
        this.restUtil = restUtil;
        this.messengerDao = messengerDao;
    }


    @Override
    public void run()
    {
        for ( Envelope envelope : envelopes )
        {
            try
            {
                Map<String, String> params = Maps.newHashMap();
                params.put( "envelope", JsonUtil.toJson( envelope ) );

                //TODO use targetPeer to obtain peer IP and port once it is implemented
                String targetPeerIP = "172.16.131.203";
                int targetPeerPort = 8181;

                restUtil.request( RestUtil.RequestType.POST,
                        String.format( "http://%s:%d/cxf/messenger/message", targetPeerIP, targetPeerPort ), params );

                messengerDao.markAsSent( envelope );
            }
            catch ( HTTPException e )
            {
                messengerDao.incrementDeliveryAttempts( envelope );

                LOG.error( "Error in PeerMessenger", e );

                //break transmission of all subsequent messages for this peer in this round
                break;
            }
        }
    }
}
