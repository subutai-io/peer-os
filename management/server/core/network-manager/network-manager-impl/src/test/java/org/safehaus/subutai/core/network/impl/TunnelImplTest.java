package org.safehaus.subutai.core.network.impl;


import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class TunnelImplTest
{

    private static final String TUNNEL_NAME = "tunnel name";
    private static final String TUNNEL_IP = "tunnel ip";


    @Test
    public void testProperties() throws Exception
    {
        TunnelImpl tunnel = new TunnelImpl( TUNNEL_NAME, TUNNEL_IP );

        assertEquals( TUNNEL_NAME, tunnel.getTunnelName() );
        assertEquals( TUNNEL_IP, tunnel.getTunnelIp() );
    }
}
