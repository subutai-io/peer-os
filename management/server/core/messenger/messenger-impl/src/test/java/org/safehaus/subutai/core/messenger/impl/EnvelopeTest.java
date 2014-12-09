package org.safehaus.subutai.core.messenger.impl;


import java.sql.Timestamp;
import java.util.UUID;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import junit.framework.TestCase;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.fail;


@Ignore
@RunWith( MockitoJUnitRunner.class )
public class EnvelopeTest
{
    private static final UUID TARGET_PEER_ID = UUID.randomUUID();
    private static final String RECIPIENT = "recipient";
    private static final int TIME_TO_LIVE = 5;
    private static final Timestamp CREATE_DATE = new Timestamp( System.currentTimeMillis() );

    @Mock
    MessageImpl message;
    Envelope envelope;


    @Before
    public void setUp() throws Exception
    {
        envelope = new Envelope( message, TARGET_PEER_ID, RECIPIENT, TIME_TO_LIVE );
    }


    @Test
    public void testConstructor() throws Exception
    {
        try
        {
            new Envelope( null, TARGET_PEER_ID, RECIPIENT, TIME_TO_LIVE );
            fail( "Exception was expected for null message" );
        }
        catch ( NullPointerException e )
        {
        }
        try
        {
            new Envelope( message, null, RECIPIENT, TIME_TO_LIVE );
            fail( "Exception was expected for null target peer id" );
        }
        catch ( NullPointerException e )
        {
        }
        try
        {
            new Envelope( message, TARGET_PEER_ID, null, TIME_TO_LIVE );
            fail( "Exception was expected for null recipient" );
        }
        catch ( IllegalArgumentException e )
        {
        }
        try
        {
            new Envelope( message, TARGET_PEER_ID, RECIPIENT, -1 );
            fail( "Exception was expected for invalid ttl" );
        }
        catch ( IllegalArgumentException e )
        {
        }
    }


    @Test
    public void testProperties() throws Exception
    {
        assertEquals( message, envelope.getMessage() );
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
}
