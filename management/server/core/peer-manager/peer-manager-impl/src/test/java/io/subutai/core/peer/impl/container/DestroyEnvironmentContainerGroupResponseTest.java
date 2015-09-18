package io.subutai.core.peer.impl.container;


import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Sets;

import static junit.framework.TestCase.assertEquals;


@RunWith( MockitoJUnitRunner.class )
public class DestroyEnvironmentContainerGroupResponseTest
{
    private static final String CONTAINER_ID = UUID.randomUUID().toString();
    private static final String EXCEPTION = "exception";

    DestroyEnvironmentContainerGroupResponse response;


    @Before
    public void setUp() throws Exception
    {
        response = new DestroyEnvironmentContainerGroupResponse( Sets.newHashSet( CONTAINER_ID ), EXCEPTION );
    }


    @Test
    public void testGetDestroyedContainerIds() throws Exception
    {
        assertEquals( Sets.newHashSet( CONTAINER_ID ), response.getDestroyedContainersIds() );
    }


    @Test
    public void testGetException() throws Exception
    {
        assertEquals( EXCEPTION, response.getException() );
    }
}
