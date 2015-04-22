package org.safehaus.subutai.core.peer.api;


import org.junit.Test;


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
