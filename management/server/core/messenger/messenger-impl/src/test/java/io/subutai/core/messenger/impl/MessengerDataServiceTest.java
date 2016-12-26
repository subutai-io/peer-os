package io.subutai.core.messenger.impl;


import java.sql.Timestamp;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityManagerFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import io.subutai.core.messenger.impl.dao.MessageDao;
import io.subutai.core.messenger.impl.entity.MessageEntity;

import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class MessengerDataServiceTest
{

    private static final String TARGET_PEER_ID = UUID.randomUUID().toString();
    private static final String RECIPIENT = "recipient";
    private static final int TIME_TO_LIVE = 5;
    private static final Timestamp CREATE_DATE = new Timestamp( System.currentTimeMillis() );
    private static final Map<String, String> HEADERS = Maps.newHashMap();


    private static final String SOURCE_PEER_ID = UUID.randomUUID().toString();
    private static final Object PAYLOAD = new Object();


    @Mock
    EntityManagerFactory entityManagerFactory;
    @Mock
    MessageDao messageDao;
    @Mock
    MessageEntity messageEntity;

    MessageImpl message;
    Envelope envelope;

    MessengerDataService messengerDataService;


    @Before
    public void setUp() throws Exception
    {
        messengerDataService = new MessengerDataService( entityManagerFactory );
        messengerDataService.messageDao = messageDao;
        message = new MessageImpl( SOURCE_PEER_ID, PAYLOAD );
        envelope = new Envelope( message, TARGET_PEER_ID, RECIPIENT, TIME_TO_LIVE, HEADERS );
        envelope.setCreateDate( CREATE_DATE );
    }


    @Test( expected = NullPointerException.class )
    public void testConstructor() throws Exception
    {
        new MessengerDataService( null );
    }


    @Test
    public void testPurgeExpiredMessages() throws Exception
    {
        messengerDataService.purgeExpiredMessages();

        verify( messageDao ).purgeMessages();
    }


    @Test
    public void testGetEnvelopes() throws Exception
    {
        when( messageDao.getTargetPeers() ).thenReturn( Lists.newArrayList( TARGET_PEER_ID ) );
        when( messageDao.getMessages( eq( TARGET_PEER_ID ), anyInt(), anyInt() ) )
                .thenReturn( Lists.newArrayList( messageEntity ) );

        Set<Envelope> envelopeSet = messengerDataService.getEnvelopes();

        assertFalse( envelopeSet.isEmpty() );
    }


    @Test
    public void testBuildEnvelopes() throws Exception
    {
        Set<Envelope> envelopeSet = messengerDataService.buildEnvelopes( Lists.newArrayList( messageEntity ) );

        assertFalse( envelopeSet.isEmpty() );
    }


    @Test
    public void testMarkAsSent() throws Exception
    {
        messengerDataService.markAsSent( envelope );

        verify( messageDao ).markAsSent( eq( envelope.getMessage().getId().toString() ) );
    }


    @Test
    public void testIncrementDeliveryAttempts() throws Exception
    {

        messengerDataService.incrementDeliveryAttempts( envelope );

        verify( messageDao ).incrementDeliveryAttempts( eq( envelope.getMessage().getId().toString() ) );
    }


    @Test
    public void testSaveEnvelope() throws Exception
    {
        messengerDataService.saveEnvelope( envelope );

        verify( messageDao ).persist( isA( MessageEntity.class ) );
    }


    @Test
    public void testGetEnvelope() throws Exception
    {
        when( messageDao.find( message.getId().toString() ) ).thenReturn( messageEntity );

        Envelope envelope1 = messengerDataService.getEnvelope( message.getId() );

        verify( messageDao ).find( message.getId().toString() );
    }
}
