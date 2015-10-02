package io.subutai.core.messenger.impl;


import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import io.subutai.common.peer.Peer;
import io.subutai.common.settings.ChannelSettings;
import io.subutai.common.settings.Common;
import io.subutai.common.settings.SecuritySettings;
import io.subutai.common.util.JsonUtil;
import io.subutai.common.util.RestUtil;
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
    private String localPeerId;


    public RemotePeerMessageSender( RestUtil restUtil, MessengerDao messengerDao, final Peer targetPeer,
                                    final Set<Envelope> envelopes, String localPeerId )
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
                String alias = SecuritySettings.KEYSTORE_PX2_ROOT_ALIAS;


                //*********construct Secure Header ****************************
                Map<String, String> headers = Maps.newHashMap();

                headers.put( Common.HEADER_SPECIAL, "ENC");
                headers.put( Common.HEADER_PEER_ID_SOURCE,localPeerId );
                headers.put( Common.HEADER_PEER_ID_TARGET,targetPeer.getId() );
                //*************************************************************

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

                restUtil.request( RestUtil.RequestType.POST, url, alias, params, headers );

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
