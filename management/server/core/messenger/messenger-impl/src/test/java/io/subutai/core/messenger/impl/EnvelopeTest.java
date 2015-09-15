package io.subutai.core.messenger.impl;


import java.sql.Timestamp;
import java.util.Map;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.core.messenger.impl.Envelope;
import io.subutai.core.messenger.impl.MessageImpl;
import io.subutai.core.messenger.impl.entity.MessageEntity;

import com.google.common.collect.Maps;

import junit.framework.TestCase;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.fail;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class EnvelopeTest
{
    private static final String TARGET_PEER_ID = UUID.randomUUID().toString();
    private static final String RECIPIENT = "recipient";
    private static final int TIME_TO_LIVE = 5;
    private static final Map<String, String> HEADERS = Maps.newHashMap();
    private static final Timestamp CREATE_DATE = new Timestamp( System.currentTimeMillis() );

    @Mock
    MessageImpl message;
    @Mock
    MessageEntity messageEntity;

    Envelope envelope;


    @Before
    public void setUp() throws Exception
    {
        envelope = new Envelope( message, TARGET_PEER_ID, RECIPIENT, TIME_TO_LIVE, HEADERS );
        //        when(messageEntity.getTargetPeerId()).thenReturn( TARGET_PEER_ID );
        //        when( messageEntity.getRecipient() ).thenReturn( RECIPIENT );
        //        when( messageEntity.getTimeToLive() ).thenReturn( TIME_TO_LIVE );
    }


    @Test
    public void testConstructor() throws Exception
    {
        try
        {
            new Envelope( null );
            fail( "Exception was expected for null message" );
        }
        catch ( NullPointerException e )
        {
        }
        try
        {
            new Envelope( null, TARGET_PEER_ID, RECIPIENT, TIME_TO_LIVE, HEADERS );
            fail( "Exception was expected for null message" );
        }
        catch ( NullPointerException e )
        {
        }
        try
        {
            new Envelope( message, null, RECIPIENT, TIME_TO_LIVE, HEADERS );
            fail( "Exception was expected for null target peer id" );
        }
        catch ( NullPointerException e )
        {
        }
        try
        {
            new Envelope( message, TARGET_PEER_ID, null, TIME_TO_LIVE, HEADERS );
            fail( "Exception was expected for null recipient" );
        }
        catch ( IllegalArgumentException e )
        {
        }
        try
        {
            new Envelope( message, TARGET_PEER_ID, RECIPIENT, -1, HEADERS );
            fail( "Exception was expected for invalid ttl" );
        }
        catch ( IllegalArgumentException e )
        {
        }
    }


    @Test
    public void testProperties() throws Exception
    {
        assertEquals( message.getId(), envelope.getMessage().getId() );
        assertEquals( RECIPIENT, envelope.getRecipient() );
        assertEquals( TARGET_PEER_ID, envelope.getTargetPeerId() );
        assertEquals( TIME_TO_LIVE, envelope.getTimeToLive() );
    }


    @Test
    public void testSent() throws Exception
    {

        assertFalse( envelope.isSent() );

        envelope.setSent( true );

        assertTrue( envelope.isSent() );
    }


    @Test
    public void testCreateDate() throws Exception
    {

        envelope.setCreateDate( CREATE_DATE );

        TestCase.assertEquals( CREATE_DATE, envelope.getCreateDate() );
    }


    @Test
    public void testConstructorWithMessageEntity() throws Exception
    {
        long ts = System.currentTimeMillis();
        when( messageEntity.getCreateDate() ).thenReturn( ts );

        envelope = new Envelope( messageEntity );

        TestCase.assertEquals( envelope.getCreateDate(), new Timestamp( ts ) );
    }
}
