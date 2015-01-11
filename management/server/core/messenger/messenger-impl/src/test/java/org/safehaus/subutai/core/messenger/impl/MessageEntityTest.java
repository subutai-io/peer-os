package org.safehaus.subutai.core.messenger.impl;


import java.util.Map;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.core.messenger.api.Message;
import org.safehaus.subutai.core.messenger.impl.entity.MessageEntity;

import com.google.common.collect.Maps;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class MessageEntityTest
{
    private static final UUID ID = UUID.randomUUID();
    private static final UUID SOURCE_PEER_ID = UUID.randomUUID();
    private static final UUID TARGET_PEER_ID = UUID.randomUUID();
    private static final String SENDER = "sender";
    private static final String SENDER2 = "sender2";
    private static final String PAYLOAD = "payload";
    private static final String RECIPIENT = "recipient";
    private static final Integer TIME_TO_LIVE = 5;
    private static final int ATTEMPTS = 5;
    private static final boolean IS_SENT = false;
    private static final long CREATE_DATE = System.currentTimeMillis();

    @Mock
    Envelope envelope;
    @Mock
    Message message;

    MessageEntity messageEntity;


    @Before
    public void setUp() throws Exception
    {
        when( message.getId() ).thenReturn( ID );
        when( message.getSourcePeerId() ).thenReturn( SOURCE_PEER_ID );
        when( message.getPayload() ).thenReturn( PAYLOAD );
        when( message.getSender() ).thenReturn( SENDER );
        when( envelope.getMessage() ).thenReturn( message );
        when( envelope.getTargetPeerId() ).thenReturn( TARGET_PEER_ID );
        when( envelope.getRecipient() ).thenReturn( RECIPIENT );
        when( envelope.getTimeToLive() ).thenReturn( TIME_TO_LIVE );
        when( envelope.isSent() ).thenReturn( IS_SENT );

        messageEntity = new MessageEntity( envelope );
    }


    @Test
    public void testProperties() throws Exception
    {
        assertEquals( ID, messageEntity.getId() );
        assertEquals( SOURCE_PEER_ID, messageEntity.getSourcePeerId() );
        assertEquals( PAYLOAD, messageEntity.getPayload() );
        assertEquals( PAYLOAD, messageEntity.getPayload( String.class ) );
        assertEquals( SENDER, messageEntity.getSender() );
        assertEquals( TARGET_PEER_ID, messageEntity.getTargetPeerId() );
        assertEquals( RECIPIENT, messageEntity.getRecipient() );
        assertEquals( TIME_TO_LIVE, messageEntity.getTimeToLive() );
        assertEquals( IS_SENT, messageEntity.getIsSent() );
        assertTrue( messageEntity.getCreateDate() > 0 );
    }


    @Test
    public void testToString() throws Exception
    {
        String toString = messageEntity.toString();

        assertThat( toString, containsString( ID.toString() ) );
    }


    @Test
    public void testSetSender() throws Exception
    {

        messageEntity.setSender( SENDER2 );
        assertEquals( SENDER2, messageEntity.getSender() );

    }


    @Test
    public void testEqualsHashCode() throws Exception
    {
        Map<MessageEntity, MessageEntity> map = Maps.newHashMap();

        map.put( messageEntity, messageEntity );

        assertEquals( new MessageEntity( envelope ), map.get( messageEntity ) );

    }
}
