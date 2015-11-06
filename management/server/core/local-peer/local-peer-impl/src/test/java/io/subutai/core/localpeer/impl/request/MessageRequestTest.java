package io.subutai.core.peer.impl.request;


import java.util.Map;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import io.subutai.core.peer.api.Payload;
import io.subutai.core.peer.impl.request.MessageRequest;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;


@RunWith( MockitoJUnitRunner.class )
public class MessageRequestTest
{
    private static final String RECIPIENT = "recipient";
    private static final UUID MESSAGE_ID = UUID.randomUUID();
    @Mock
    Payload payload;
    @Mock
    Map<String, String> headers;

    MessageRequest request;


    @Before
    public void setUp() throws Exception
    {
        request = new MessageRequest( payload, RECIPIENT, headers );
    }


    @Test
    public void testGetId() throws Exception
    {
        assertNotNull( request.getId() );
    }


    @Test
    public void testMessageId() throws Exception
    {
        request.setMessageId( MESSAGE_ID );

        assertEquals( MESSAGE_ID, request.getMessageId() );
    }


    @Test
    public void testGetRecipient() throws Exception
    {
        assertEquals( RECIPIENT, request.getRecipient() );
    }


    @Test
    public void testGetPayload() throws Exception
    {
        assertEquals( payload, request.getPayload() );
    }


    @Test
    public void testGetHeaders() throws Exception
    {
        assertEquals( headers, request.getHeaders() );
    }
}
