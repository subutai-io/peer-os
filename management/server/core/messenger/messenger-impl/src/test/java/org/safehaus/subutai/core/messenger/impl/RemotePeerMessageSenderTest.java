package org.safehaus.subutai.core.messenger.impl;


import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.exception.HTTPException;
import org.safehaus.subutai.common.peer.Peer;
import org.safehaus.subutai.common.peer.PeerInfo;
import org.safehaus.subutai.common.util.RestUtil;

import com.google.common.collect.Sets;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class RemotePeerMessageSenderTest
{
    private static final String IP = "1.1.1.1";
    private static final Object PAYLOAD = new Object();
    private static final UUID SOURCE_PEER_ID = UUID.randomUUID();
    private static final UUID TARGET_PEER_ID = UUID.randomUUID();
    private static final String RECIPIENT = "sender";
    private static final int TIME_TO_LIVE = 5;


    @Mock
    Peer peer;
    @Mock
    RestUtil restUtil;
    @Mock
    MessengerDao messengerDao;
    @Mock
    PeerInfo peerInfo;


    RemotePeerMessageSender remotePeerMessageSender;
    Envelope envelope;


    @Before
    public void setUp() throws Exception
    {
        MessageImpl message = new MessageImpl( SOURCE_PEER_ID, PAYLOAD );

        envelope = new Envelope( message, TARGET_PEER_ID, RECIPIENT, TIME_TO_LIVE );
        remotePeerMessageSender =
                new RemotePeerMessageSender( restUtil, messengerDao, peer, Sets.newHashSet( envelope ) );

        when( peer.getPeerInfo() ).thenReturn( peerInfo );
        when( peerInfo.getIp() ).thenReturn( IP );
    }


    @Test
    public void testCall() throws Exception
    {
        boolean result = remotePeerMessageSender.call();

        assertTrue( result );
        verify( restUtil ).request( isA( RestUtil.RequestType.class ), anyString(), anyMap(), anyMap() );
        verify( messengerDao ).markAsSent( envelope );
    }


    @Test
    public void testCallException() throws Exception
    {
        when( restUtil.request( any( RestUtil.RequestType.class ), anyString(), anyMap(), anyMap() ) )
                .thenThrow( new HTTPException( "" ) );

        remotePeerMessageSender.call();

        verify( messengerDao ).incrementDeliveryAttempts( envelope );
    }
}
