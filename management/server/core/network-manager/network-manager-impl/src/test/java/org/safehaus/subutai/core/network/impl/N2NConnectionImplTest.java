package org.safehaus.subutai.core.network.impl;


import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class N2NConnectionImplTest
{
    private static final String SUPER_NODE_IP = "super.node.ip";
    private static final int SUPER_NODE_PORT = 1234;
    private static final String INTERFACE_NAME = "interface name";
    private static final String COMMUNITY_NAME = "community name";
    private static final String LOCAL_IP = "local.ip";


    @Test
    public void testProperties() throws Exception
    {
        N2NConnectionImpl connection =
                new N2NConnectionImpl( LOCAL_IP, SUPER_NODE_IP, SUPER_NODE_PORT, INTERFACE_NAME, COMMUNITY_NAME );

        assertEquals( LOCAL_IP, connection.getLocalIp() );
        assertEquals( SUPER_NODE_IP, connection.getSuperNodeIp() );
        assertEquals( SUPER_NODE_PORT, connection.getSuperNodePort() );
        assertEquals( INTERFACE_NAME, connection.getInterfaceName() );
        assertEquals( COMMUNITY_NAME, connection.getCommunityName() );
    }
}
