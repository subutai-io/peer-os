package io.subutai.core.messenger.impl;


import java.util.Map;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import org.apache.cxf.jaxrs.client.WebClient;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerInfo;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class RemotePeerMessageSenderTest
{
    private static final String IP = "1.1.1.1";
    private static final Object PAYLOAD = new Object();
    private static final String SOURCE_PEER_ID = UUID.randomUUID().toString();
    private static final String TARGET_PEER_ID = UUID.randomUUID().toString();
    private static final String RECIPIENT = "sender";
    private static final int TIME_TO_LIVE = 5;
    private static final Map<String, String> HEADERS = Maps.newHashMap();


    @Mock
    Peer peer;

    @Mock
    MessengerDao messengerDao;
    @Mock
    PeerInfo peerInfo;
    @Mock
    WebClient webClient;


    RemotePeerMessageSender remotePeerMessageSender;
    Envelope envelope;
    String uuid = UUID.randomUUID().toString();


    @Before
    public void setUp() throws Exception
    {
        MessageImpl message = new MessageImpl( SOURCE_PEER_ID, PAYLOAD );
        when( peerInfo.getId() ).thenReturn( uuid );

        envelope = new Envelope( message, TARGET_PEER_ID, RECIPIENT, TIME_TO_LIVE, HEADERS );
        remotePeerMessageSender = spy( new RemotePeerMessageSender( messengerDao, peer, Sets.newHashSet( envelope ) ) );

        when( peer.getPeerInfo() ).thenReturn( peerInfo );
        when( peerInfo.getIp() ).thenReturn( IP );
        doReturn( webClient ).when( remotePeerMessageSender ).getWebClient( anyString() );
    }


    @Test
    public void testCall() throws Exception
    {
        remotePeerMessageSender.call();

        verify( webClient ).post( anyString() );
        verify( messengerDao ).markAsSent( envelope );
    }


    @Test
    public void testCallException() throws Exception
    {
        doThrow( new RuntimeException() ).when( webClient ).post( anyString() );

        remotePeerMessageSender.call();

        verify( messengerDao ).incrementDeliveryAttempts( envelope );
    }
}
