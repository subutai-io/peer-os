package org.safehaus.subutai.core.dispatcher.impl;


import java.util.Set;

import org.junit.Test;
import org.safehaus.subutai.common.protocol.Response;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;


/**
 * Test for DispatcherMessage
 */
public class DispatcherMessageTest
{
    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNullMessageType()
    {
        new DispatcherMessage( null, mock( Set.class ) );
    }


    @Test( expected = IllegalArgumentException.class )
    public void constructorShouldFailOnNullRequests()
    {
        new DispatcherMessage( DispatcherMessageType.REQUEST, null );
    }


    @Test( expected = IllegalArgumentException.class )
    public void constructorShouldFailOnNullResponses()
    {
        new DispatcherMessage( null, DispatcherMessageType.RESPONSE );
    }


    @Test
    public void shouldReturnSameRequests()
    {
        Set<BatchRequest> requests = mock( Set.class );
        DispatcherMessage message = new DispatcherMessage( DispatcherMessageType.REQUEST, requests );

        assertEquals( requests, message.getBatchRequests() );
    }


    @Test
    public void shouldReturnSameResponses()
    {
        Set<Response> responses = mock( Set.class );
        DispatcherMessage message = new DispatcherMessage( responses, DispatcherMessageType.RESPONSE );

        assertEquals( responses, message.getResponses() );
        assertEquals( DispatcherMessageType.RESPONSE, message.getDispatcherMessageType() );
    }
}
