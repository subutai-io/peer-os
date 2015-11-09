package io.subutai.core.localpeer.impl.container;


import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Sets;

import io.subutai.common.peer.ContainerHost;

import static junit.framework.TestCase.assertEquals;


@RunWith( MockitoJUnitRunner.class )
public class DestroyEnvironmentContainerGroupResponseTest
{
    private static final String CONTAINER_ID = UUID.randomUUID().toString();
    private static final String EXCEPTION = "exception";

    DestroyEnvironmentContainerGroupResponse response;

    @Mock
    ContainerHost containerHost;

    @Before
    public void setUp() throws Exception
    {
        response = new DestroyEnvironmentContainerGroupResponse( Sets.newHashSet( containerHost ), EXCEPTION );
    }


    @Test
    public void testGetDestroyedContainerIds() throws Exception
    {
        assertEquals( Sets.newHashSet( containerHost ), response.getDestroyedContainersIds() );
    }


    @Test
    public void testGetException() throws Exception
    {
        assertEquals( EXCEPTION, response.getException() );
    }
}
