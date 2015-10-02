package io.subutai.core.peer.impl.command;


import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import io.subutai.common.command.RequestBuilder;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;


@RunWith( MockitoJUnitRunner.class )
public class CommandRequestTest
{
    private static final String HOST_ID = UUID.randomUUID().toString();
    private static final String ENV_ID = UUID.randomUUID().toString();
    @Mock
    RequestBuilder requestBuilder;


    CommandRequest commandRequest;


    @Before
    public void setUp() throws Exception
    {
        commandRequest = new CommandRequest( requestBuilder, HOST_ID, ENV_ID );
    }


    @Test
    public void testGetRequestId() throws Exception
    {
        assertNotNull( commandRequest.getRequestId() );
    }


    @Test
    public void testGetRequestBuilder() throws Exception
    {
        assertEquals( requestBuilder, commandRequest.getRequestBuilder() );
    }


    @Test
    public void testGetHostId() throws Exception
    {
        assertEquals( HOST_ID, commandRequest.getHostId() );
    }


    @Test
    public void testGetEnvironmentId() throws Exception
    {
        assertEquals( ENV_ID, commandRequest.getEnvironmentId() );
    }
}
