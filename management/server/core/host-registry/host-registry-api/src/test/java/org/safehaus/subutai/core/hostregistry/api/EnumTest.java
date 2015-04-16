package org.safehaus.subutai.core.hostregistry.api;


import org.junit.Test;
import org.safehaus.subutai.common.host.ContainerHostState;
import org.safehaus.subutai.common.host.HostArchitecture;


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
