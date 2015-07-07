package io.subutai.core.messenger.impl;


import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.safehaus.subutai.common.peer.Peer;
import org.safehaus.subutai.common.settings.ChannelSettings;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.common.util.RestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;


/**
 * Delivers messages to a specific peer
 */
public class RemotePeerMessageSender implements Callable<Boolean>
{
    private static final Logger LOG = LoggerFactory.getLogger( RemotePeerMessageSender.class.getName() );

    private Peer targetPeer;
    private Set<Envelope> envelopes;
    private RestUtil restUtil;
    private MessengerDao messengerDao;
    private UUID localPeerId;


    public RemotePeerMessageSender( RestUtil restUtil, MessengerDao messengerDao, final Peer targetPeer,
                                    final Set<Envelope> envelopes, UUID localPeerId )
    {
        this.targetPeer = targetPeer;
        this.envelopes = envelopes;
        this.restUtil = restUtil;
        this.messengerDao = messengerDao;
        this.localPeerId = localPeerId;
    }


    @Override
    public Boolean call()
    {
        for ( Envelope envelope : envelopes )
        {
            try
            {
                Map<String, String> params = Maps.newHashMap();
                params.put( "envelope", JsonUtil.toJson( envelope ) );

                String targetPeerIP = targetPeer.getPeerInfo().getIp();
                int targetPeerPort = targetPeer.getPeerInfo().getPort();
                String port = String.valueOf( targetPeerPort );

                String url = "";
                String alias = //SecuritySettings.KEYSTORE_PX2_ROOT_ALIAS;
                        String.format( "env_%s_%s", localPeerId.toString(),
                                envelope.getHeaders().get( Common.ENVIRONMENT_ID_HEADER_NAME ) );

                switch ( port )
                {
                    case ChannelSettings.OPEN_PORT:
                    case ChannelSettings.SPECIAL_PORT_X1:
                        url = String.format( "http://%s:%d/cxf/messenger/message", targetPeerIP, targetPeerPort );
                        break;
                    case ChannelSettings.SECURE_PORT_X1:
                    case ChannelSettings.SECURE_PORT_X2:
                    case ChannelSettings.SECURE_PORT_X3:
                        url = String.format( "https://%s:%d/cxf/messenger/message", targetPeerIP, targetPeerPort );
                        break;
                }

                restUtil.request( RestUtil.RequestType.POST, url, alias, params, envelope.getHeaders() );

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
}
