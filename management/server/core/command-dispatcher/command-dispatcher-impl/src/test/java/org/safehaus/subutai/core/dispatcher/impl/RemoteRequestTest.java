package org.safehaus.subutai.core.dispatcher.impl;


import java.util.UUID;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * Test for RemoteRequest
 */
public class RemoteRequestTest
{
    private static final UUID PEER_ID = UUID.randomUUID();
    private static final UUID COMMAND_ID = UUID.randomUUID();
    private static final int REQUESTS_COUNT = 1;


    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNullPeerId()
    {
        new RemoteRequest( null, COMMAND_ID, REQUESTS_COUNT );
    }


    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnCommandIdId()
    {
        new RemoteRequest( PEER_ID, null, REQUESTS_COUNT );
    }


    @Test( expected = IllegalArgumentException.class )
    public void constructorShouldFailOnLessEqualZeroRequests()
    {
        new RemoteRequest( PEER_ID, COMMAND_ID, 0 );
    }


    @Test
    public void testPropertiesBeforeUpdate()
    {
        RemoteRequest remoteRequest = new RemoteRequest( PEER_ID, COMMAND_ID, REQUESTS_COUNT );

        assertEquals( PEER_ID, remoteRequest.getPeerId() );
        assertEquals( COMMAND_ID, remoteRequest.getCommandId() );
        assertEquals( 1, remoteRequest.getAttempts() );
        assertFalse( remoteRequest.isCompleted() );
    }


    @Test
    public void testPropertiesAfterUpdate() throws InterruptedException
    {
        RemoteRequest remoteRequest = new RemoteRequest( PEER_ID, COMMAND_ID, REQUESTS_COUNT );

        long ts = remoteRequest.getTimestamp();

        int attempts = remoteRequest.getAttempts();
        remoteRequest.incrementAttempts();
        remoteRequest.incrementCompletedRequestsCount();

        Thread.sleep( 1 );
        remoteRequest.updateTimestamp();

        assertTrue( remoteRequest.getTimestamp() > ts );
        assertEquals( attempts + 1, remoteRequest.getAttempts() );
        assertTrue( remoteRequest.isCompleted() );
    }
}
