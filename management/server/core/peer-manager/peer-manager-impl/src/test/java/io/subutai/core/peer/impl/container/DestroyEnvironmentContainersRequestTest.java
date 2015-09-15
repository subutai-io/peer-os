package io.subutai.core.peer.impl.container;


import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import io.subutai.core.peer.impl.container.DestroyEnvironmentContainersRequest;

import static junit.framework.TestCase.assertEquals;


public class DestroyEnvironmentContainersRequestTest
{
    private static final String ENV_ID = UUID.randomUUID().toString();

    DestroyEnvironmentContainersRequest request;


    @Before
    public void setUp() throws Exception
    {
        request = new DestroyEnvironmentContainersRequest( ENV_ID );
    }


    @Test
    public void testGetEnvironmentId() throws Exception
    {

        assertEquals( ENV_ID, request.getEnvironmentId() );
    }
}
