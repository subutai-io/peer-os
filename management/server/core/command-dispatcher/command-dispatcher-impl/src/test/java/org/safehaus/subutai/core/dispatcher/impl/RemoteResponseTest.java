package org.safehaus.subutai.core.dispatcher.impl;


import java.util.UUID;

import org.junit.Test;
import org.safehaus.subutai.common.protocol.Response;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Test for RemoteResponse
 */
public class RemoteResponseTest
{
    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNullResponse()
    {
        new RemoteResponse( null );
    }

    @Test
    public void shouldReturnProperties(){
        UUID commandId = UUID.randomUUID();
        Response response = mock(Response.class);
        when( response.getTaskUuid() ).thenReturn( commandId );

        RemoteResponse remoteResponse = new RemoteResponse( response );

        assertEquals(commandId, remoteResponse.getCommandId());
        assertEquals(response, remoteResponse.getResponse());

    }
}
