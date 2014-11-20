package org.safehaus.subutai.core.hostregistry.api;


import org.junit.Test;


public class ContainerHostStateTest
{

    @Test
    public void testValues() throws Exception
    {
        ContainerHostState state = ContainerHostState.RUNNING;
        state = ContainerHostState.FROZEN;
        state = ContainerHostState.FREEZING;
        state = ContainerHostState.STARTING;
        state = ContainerHostState.ABORTING;
        state = ContainerHostState.STOPPING;
        state = ContainerHostState.STOPPED;
    }
}
