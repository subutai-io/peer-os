package org.safehaus.subutai.core.messenger.impl;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import javax.persistence.EntityManager;
import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.messenger.api.Message;
import org.safehaus.subutai.core.messenger.api.MessageException;
import org.safehaus.subutai.core.messenger.api.MessageListener;
import org.safehaus.subutai.core.messenger.api.MessageStatus;
import org.safehaus.subutai.core.messenger.api.MessengerException;
import org.safehaus.subutai.core.peer.api.LocalPeer;
import org.safehaus.subutai.core.peer.api.PeerManager;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class MessengerImplTest
{

    private static final UUID LOCAL_PEER_ID = UUID.randomUUID();
    private static final UUID MESSAGE_ID = UUID.randomUUID();
    private static final String RECIPIENT = "sender";
    private static final Object PAYLOAD = new Object();


    private static final int TIME_TO_LIVE = 5;
    @Mock
    DataSource dataSource;
    @Mock
    PeerManager peerManager;
    @Mock
    MessageSender messageSender;
    @Mock
    ExecutorService notificationExecutor;
    @Mock
    MessengerDao messengerDao;
    @Mock
    LocalPeer localPeer;
    @Mock
    MessageImpl message;
    @Mock
    EntityManager entityManager;

    MessengerImpl messenger;


    @Before
    public void setUp() throws Exception
    {
        Connection connection = mock( Connection.class );
        PreparedStatement preparedStatement = mock( PreparedStatement.class );
        when( connection.prepareStatement( anyString() ) ).thenReturn( preparedStatement );
        when( dataSource.getConnection() ).thenReturn( connection );

        messenger = new MessengerImpl( peerManager );
        messenger.messageSender = messageSender;
        messenger.notificationExecutor = notificationExecutor;
        messenger.messengerDao = messengerDao;
        when( localPeer.getId() ).thenReturn( LOCAL_PEER_ID );
        when( peerManager.getLocalPeer() ).thenReturn( localPeer );
    }


    @Test( expected = NullPointerException.class )
    public void testConstructor() throws Exception
    {
        new MessengerImpl( null);
    }


    @Test( expected = NullPointerException.class )
    public void testConstructor2() throws Exception
    {
        new MessengerImpl( peerManager );
    }


    @Test( expected = MessengerException.class )
    public void testConstructorWithException() throws Exception
    {
        //doThrow( new RuntimeException() ).when( entityManagerFactory ).createEntityManager();

        new MessengerImpl( peerManager );
    }


    @Test
    public void testInit() throws Exception
    {
        messenger.init();

        verify( messageSender ).init();
    }


    @Test
    public void testDestroy() throws Exception
    {
        messenger.destroy();

        verify( messageSender ).dispose();
        verify( notificationExecutor ).shutdown();
    }


    @Test
    public void testCreateMessage() throws Exception
    {
        Message message = messenger.createMessage( PAYLOAD );

        assertNotNull( message );
    }


    @Test( expected = MessageException.class )
    public void testSendMessage() throws Exception
    {
        messenger.sendMessage( localPeer, message, RECIPIENT, TIME_TO_LIVE );

        verify( messengerDao ).saveEnvelope( isA( Envelope.class ) );

        doThrow( new RuntimeException() ).when( messengerDao ).saveEnvelope( any( Envelope.class ) );

        messenger.sendMessage( localPeer, message, RECIPIENT, TIME_TO_LIVE );
    }


    @Test( expected = MessageException.class )
    public void testGetMessageStatus() throws Exception
    {
        Envelope envelope = mock( Envelope.class );
        when( envelope.isSent() ).thenReturn( false );


        //test NOT_FOUND
        MessageStatus status = messenger.getMessageStatus( MESSAGE_ID );

        assertEquals( MessageStatus.NOT_FOUND, status );


        when( messengerDao.getEnvelope( MESSAGE_ID ) ).thenReturn( envelope );

        //test IN_PROCESS
        when( envelope.getTimeToLive() ).thenReturn( TIME_TO_LIVE );
        Timestamp createDate = mock( Timestamp.class );
        when( envelope.getCreateDate() ).thenReturn( createDate );
        when( createDate.getTime() ).thenReturn( System.currentTimeMillis() - 1000000 );

        status = messenger.getMessageStatus( MESSAGE_ID );

        assertEquals( MessageStatus.IN_PROCESS, status );

        //test EXPIRED
        when( createDate.getTime() ).thenReturn( System.currentTimeMillis() + 1000000 );

        status = messenger.getMessageStatus( MESSAGE_ID );

        assertEquals( MessageStatus.EXPIRED, status );


        //test SENT
        when( envelope.isSent() ).thenReturn( true );

        status = messenger.getMessageStatus( MESSAGE_ID );

        assertEquals( MessageStatus.SENT, status );

        //test exception
        doThrow( new RuntimeException() ).when( messengerDao ).getEnvelope( any( UUID.class ) );

        status = messenger.getMessageStatus( MESSAGE_ID );
    }


    @Test
    public void testAddRemoveMessageListener() throws Exception
    {
        MessageListener listener = mock( MessageListener.class );

        messenger.addMessageListener( listener );

        assertTrue( messenger.listeners.contains( listener ) );

        messenger.removeMessageListener( listener );

        assertFalse( messenger.listeners.contains( listener ) );
    }


    @Test
    public void testProcessMessage() throws Exception
    {
        MessageImpl message = new MessageImpl( LOCAL_PEER_ID, PAYLOAD );
        Envelope envelope = new Envelope( message, LOCAL_PEER_ID, RECIPIENT, TIME_TO_LIVE );
        MessageListener listener = mock( MessageListener.class );
        messenger.addMessageListener( listener );
        when( listener.getRecipient() ).thenReturn( RECIPIENT );


        messenger.processMessage( JsonUtil.toJson( envelope ) );

        verify( notificationExecutor ).execute( isA( MessageNotifier.class ) );


        //test exception
        try
        {
            messenger.processMessage( null );
            fail( "Expected message exception" );
        }
        catch ( MessageException e )
        {
        }
    }
}
