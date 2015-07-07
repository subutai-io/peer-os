package io.subutai.core.peer.impl.request;


import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import io.subutai.core.peer.api.Payload;
import io.subutai.core.peer.impl.request.MessageResponse;

import static junit.framework.TestCase.assertEquals;


@RunWith( MockitoJUnitRunner.class )
public class MessageResponseTest
{
    private static final String EXCEPTION = "exception";
    private static final UUID REQUEST_ID = UUID.randomUUID();

    @Mock
    Payload payload;

    MessageResponse response;


    @Before
    public void setUp() throws Exception
    {
        response = new MessageResponse( REQUEST_ID, payload, EXCEPTION );
    }


    @Test
    public void testGetRequestId() throws Exception
    {

        assertEquals( REQUEST_ID, response.getRequestId() );
    }


    @Test
    public void testGetPayload() throws Exception
    {
        assertEquals( payload, response.getPayload() );
    }


    @Test
    public void testGetException() throws Exception
    {
        assertEquals( EXCEPTION, response.getException() );
    }
}
