package org.safehaus.subutai.core.messenger.impl;


import java.sql.Timestamp;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityManagerFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.core.messenger.impl.dao.MessageDataService;
import org.safehaus.subutai.core.messenger.impl.model.MessageEntity;

import com.google.common.collect.Lists;

import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class MessengerDaoTest
{

    private static final UUID TARGET_PEER_ID = UUID.randomUUID();
    private static final String RECIPIENT = "recipient";
    private static final int TIME_TO_LIVE = 5;
    private static final Timestamp CREATE_DATE = new Timestamp( System.currentTimeMillis() );

    private static final UUID SOURCE_PEER_ID = UUID.randomUUID();
    private static final Object PAYLOAD = new Object();


    @Mock
    EntityManagerFactory entityManagerFactory;
    @Mock
    MessageDataService messageDataService;
    @Mock
    MessageEntity messageEntity;

    MessageImpl message;
    Envelope envelope;

    MessengerDao messengerDao;


    @Before
    public void setUp() throws Exception
    {
        messengerDao = new MessengerDao( entityManagerFactory );
        messengerDao.messageDataService = messageDataService;
        message = new MessageImpl( SOURCE_PEER_ID, PAYLOAD );
        envelope = new Envelope( message, TARGET_PEER_ID, RECIPIENT, TIME_TO_LIVE );
        envelope.setCreateDate( CREATE_DATE );
    }


    @Test( expected = NullPointerException.class )
    public void testConstructor() throws Exception
    {
        new MessengerDao( null );
    }


    @Test
    public void testPurgeExpiredMessages() throws Exception
    {
        messengerDao.purgeExpiredMessages();

        verify( messageDataService ).purgeMessages();
    }


    @Test
    public void testGetEnvelopes() throws Exception
    {
        when( messageDataService.getTargetPeers() ).thenReturn( Lists.newArrayList( TARGET_PEER_ID.toString() ) );
        when( messageDataService.getMessages( eq( TARGET_PEER_ID.toString() ), anyInt(), anyInt() ) )
                .thenReturn( Lists.newArrayList( messageEntity ) );

        Set<Envelope> envelopeSet = messengerDao.getEnvelopes();

        assertFalse( envelopeSet.isEmpty() );
    }


    @Test
    public void testBuildEnvelopes() throws Exception
    {
        Set<Envelope> envelopeSet = messengerDao.buildEnvelopes( Lists.newArrayList(messageEntity) );

        assertFalse( envelopeSet.isEmpty() );
    }


    @Test
    public void testMarkAsSent() throws Exception
    {
        messengerDao.markAsSent( envelope );

        verify( messageDataService ).markAsSent( eq( envelope.getMessage().getId().toString() ) );
    }


    @Test
    public void testIncrementDeliveryAttempts() throws Exception
    {

        messengerDao.incrementDeliveryAttempts( envelope );

        verify( messageDataService ).incrementDeliveryAttempts( eq( envelope.getMessage().getId().toString() ) );
    }


    @Test
    public void testSaveEnvelope() throws Exception
    {
        messengerDao.saveEnvelope( envelope );

        verify( messageDataService ).persist( isA( MessageEntity.class ) );
    }


    @Test
    public void testGetEnvelope() throws Exception
    {
        when( messageDataService.find( message.getId().toString() ) ).thenReturn( messageEntity );

        Envelope envelope1 = messengerDao.getEnvelope( message.getId() );

        verify( messageDataService ).find( message.getId().toString() );
    }
}
