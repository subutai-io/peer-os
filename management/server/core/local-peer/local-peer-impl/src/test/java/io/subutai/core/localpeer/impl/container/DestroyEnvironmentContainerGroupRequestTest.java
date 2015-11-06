package io.subutai.core.peer.impl.container;


import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;


public class DestroyEnvironmentContainerGroupRequestTest
{
    private static final String ENV_ID = UUID.randomUUID().toString();

    DestroyEnvironmentContainerGroupRequest request;


    @Before
    public void setUp() throws Exception
    {
        request = new DestroyEnvironmentContainerGroupRequest( ENV_ID );
    }


    @Test
    public void testGetEnvironmentId() throws Exception
    {

        assertEquals( ENV_ID, request.getEnvironmentId() );
    }
}
