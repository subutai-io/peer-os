package org.safehaus.subutai.core.peer.impl.container;


import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Sets;

import static junit.framework.TestCase.assertEquals;


public class ContainersDestructionResultImplTest
{
    private static final UUID PEER_ID = UUID.randomUUID();
    private static final UUID CONTAINER_ID = UUID.randomUUID();
    private static final String EXCEPTION = "exception";

    ContainersDestructionResultImpl containersDestructionResult;


    @Before
    public void setUp() throws Exception
    {
        containersDestructionResult =
                new ContainersDestructionResultImpl( PEER_ID, Sets.newHashSet( CONTAINER_ID ), EXCEPTION );
    }


    @Test
    public void testPeerId() throws Exception
    {
        assertEquals( PEER_ID, containersDestructionResult.peerId() );
    }


    @Test
    public void testGetDestroyedContainersIds() throws Exception
    {
        assertEquals( Sets.newHashSet( CONTAINER_ID ), containersDestructionResult.getDestroyedContainersIds() );
    }


    @Test
    public void testGetException() throws Exception
    {

        assertEquals( EXCEPTION, containersDestructionResult.getException() );
    }

}
