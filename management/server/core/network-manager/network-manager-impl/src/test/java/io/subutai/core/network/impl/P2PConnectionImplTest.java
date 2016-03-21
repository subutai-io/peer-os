package io.subutai.core.network.impl;


import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class P2PConnectionImplTest
{
    private static final String INTERFACE_NAME = "interface name";
    private static final String COMMUNITY_NAME = "community name";
    private static final String LOCAL_IP = "local.ip";


    @Test
    public void testProperties() throws Exception
    {
        P2PConnectionImpl connection = new P2PConnectionImpl( INTERFACE_NAME, LOCAL_IP, COMMUNITY_NAME );

        assertEquals( LOCAL_IP, connection.getLocalIp() );
        assertEquals( INTERFACE_NAME, connection.getInterfaceName() );
        assertEquals( COMMUNITY_NAME, connection.getP2pHash() );
    }
}
