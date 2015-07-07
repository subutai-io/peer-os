package io.subutai.core.network.impl;


import org.junit.Test;

import io.subutai.core.network.impl.ContainerInfoImpl;

import static org.junit.Assert.assertEquals;


public class ContainerInfoImplTest
{
    private static final int VLAN_ID = 100;
    private static final String CONTAINER_IP = "container ip";
    private static final int NET_MASK = 24;


    @Test
    public void testProperties() throws Exception
    {
        ContainerInfoImpl containerInfo = new ContainerInfoImpl( CONTAINER_IP, NET_MASK, VLAN_ID );

        assertEquals( CONTAINER_IP, containerInfo.getIp() );
        assertEquals( NET_MASK, containerInfo.getNetMask() );
        assertEquals( VLAN_ID, containerInfo.getVLanId() );
    }
}
