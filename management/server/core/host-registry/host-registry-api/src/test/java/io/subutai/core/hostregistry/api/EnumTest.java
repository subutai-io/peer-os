package io.subutai.core.hostregistry.api;


import org.junit.Test;
import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.HostArchitecture;


public class EnumTest
{

    @Test
    public void testContainerState() throws Exception
    {
        for ( ContainerHostState state : ContainerHostState.values() )
        {
        }
    }


    @Test
    public void testHostArchitecture() throws Exception
    {

        for ( HostArchitecture arch : HostArchitecture.values() )
        {
        }
    }
}
