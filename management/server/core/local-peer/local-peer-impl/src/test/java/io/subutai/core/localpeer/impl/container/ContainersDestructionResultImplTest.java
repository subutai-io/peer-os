package io.subutai.core.localpeer.impl.container;


import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.google.common.collect.Sets;

import io.subutai.common.peer.ContainerHost;

import static junit.framework.TestCase.assertEquals;


public class ContainersDestructionResultImplTest
{
    private static final String PEER_ID = UUID.randomUUID().toString();
    private static final String CONTAINER_ID = UUID.randomUUID().toString();
    private static final String EXCEPTION = "exception";

    ContainersDestructionResultImpl containersDestructionResult;

    @Mock
    ContainerHost containerHost;

    @Before
    public void setUp() throws Exception
    {
        containersDestructionResult =
                new ContainersDestructionResultImpl( PEER_ID, Sets.newHashSet( containerHost ), EXCEPTION );
    }


    @Test
    public void testPeerId() throws Exception
    {
        assertEquals( PEER_ID, containersDestructionResult.peerId() );
    }


    @Test
    public void testGetDestroyedContainersIds() throws Exception
    {
        assertEquals( Sets.newHashSet( containerHost ), containersDestructionResult.getDestroyedContainersIds() );
    }


    @Test
    public void testGetException() throws Exception
    {

        assertEquals( EXCEPTION, containersDestructionResult.getException() );
    }

}
