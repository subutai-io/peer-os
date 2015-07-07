package io.subutai.core.peer.api;


import org.junit.Test;

import io.subutai.core.peer.api.ContainerState;


public class ContainerStateTest
{
    @Test
    public void testContainerState() throws Exception
    {
        ContainerState containerState;

        for ( ContainerState state : ContainerState.values() )
        {
            containerState = state;
        }
    }
}
