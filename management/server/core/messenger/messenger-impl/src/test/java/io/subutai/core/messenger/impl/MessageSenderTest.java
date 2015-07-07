package io.subutai.core.messenger.impl;


import java.sql.Timestamp;
import java.util.UUID;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.peer.Peer;
import org.safehaus.subutai.common.peer.PeerInfo;
import org.safehaus.subutai.common.util.RestUtil;
import org.safehaus.subutai.core.peer.api.LocalPeer;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.slf4j.Logger;

import com.google.common.collect.Sets;

import io.subutai.core.messenger.impl.Envelope;
import io.subutai.core.messenger.impl.LocalPeerMessageSender;
import io.subutai.core.messenger.impl.MessageSender;
import io.subutai.core.messenger.impl.MessengerDao;
import io.subutai.core.messenger.impl.MessengerImpl;
import io.subutai.core.messenger.impl.RemotePeerMessageSender;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class MessageSenderTest
{
    private static final UUID LOCAL_PEER_ID = UUID.randomUUID();
    private static final UUID TARGET_PEER_ID = UUID.randomUUID();
    private static final int TIME_TO_LIVE = 5;
    private static final Timestamp CREATE_DATE = new Timestamp( System.currentTimeMillis() );
    @Mock
    PeerManager peerManager;
    @Mock
    MessengerDao messengerDao;
    @Mock
    MessengerImpl messenger;
    @Mock
    ScheduledExecutorService mainLoopExecutor;
    @Mock
    ExecutorService restExecutor;
    @Mock
    RestUtil restUtil;
    @Mock
    Envelope envelope;
    @Mock
    Peer peer;
    @Mock
    LocalPeer localPeer;
    @Mock
    Logger logger;
    @Mock
    PeerInfo peerInfo;
    @Mock
    CompletionService completer;
    @Mock
    Future future;

    MessageSender messageSender;


    @Before
    public void setUp() throws Exception
    {
        messageSender = new MessageSender( messengerDao, messenger );
        messageSender.mainLoopExecutor = mainLoopExecutor;
        messageSender.restExecutor = restExecutor;
        messageSender.restUtil = restUtil;
        messageSender.LOG = logger;
        messageSender.completer = completer;
    }


    @Test
    public void testInit() throws Exception
    {
        messageSender.init();

        verify( mainLoopExecutor ).scheduleWithFixedDelay( isA( Runnable.class ), eq( 0L ),
                eq( ( long ) MessageSender.SLEEP_BETWEEN_ITERATIONS_SEC ), eq( TimeUnit.SECONDS ) );
    }


    @Test
    public void testDispose() throws Exception
    {
        messageSender.dispose();

        verify( restExecutor ).shutdown();
        verify( mainLoopExecutor ).shutdown();
    }


    @Test
    public void testPurgeExpiredMessage() throws Exception
    {
        messageSender.purgeExpiredMessages();

        verify( messengerDao ).purgeExpiredMessages();
    }


    @Test
    public void testDeliverMessages() throws Exception
    {
        when( messengerDao.getEnvelopes() ).thenReturn( Sets.newHashSet( envelope ) );
        when( envelope.getTimeToLive() ).thenReturn( TIME_TO_LIVE );
        when( envelope.getTargetPeerId() ).thenReturn( TARGET_PEER_ID );
        when( envelope.getCreateDate() ).thenReturn( CREATE_DATE );
        when( peerManager.getPeer( TARGET_PEER_ID ) ).thenReturn( peer );
        when( peer.getId() ).thenReturn( TARGET_PEER_ID );
        when( peerManager.getLocalPeer() ).thenReturn( localPeer );
        when( peerManager.getLocalPeerInfo() ).thenReturn( peerInfo );
        when( peerInfo.getId() ).thenReturn( UUID.randomUUID() );
        when( localPeer.isLocal() ).thenReturn( false );
        when( localPeer.getId() ).thenReturn( UUID.randomUUID() );
        when( completer.take() ).thenReturn( future );
        when( messenger.getPeerManager() ).thenReturn( peerManager );

        messageSender.deliverMessages();

        verify( completer ).submit( isA( RemotePeerMessageSender.class ) );

        when( peer.isLocal() ).thenReturn( true );

        messageSender.deliverMessages();

        verify( completer ).submit( isA( LocalPeerMessageSender.class ) );

        doThrow( new InterruptedException() ).when( completer ).take();

        messageSender.deliverMessages();

        verify( logger ).warn( anyString(), isA( InterruptedException.class ) );
    }
}
