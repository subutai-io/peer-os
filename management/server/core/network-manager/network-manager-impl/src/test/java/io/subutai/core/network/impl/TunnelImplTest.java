package io.subutai.core.network.impl;


import org.junit.Test;

import io.subutai.common.protocol.Tunnel;

import static org.junit.Assert.assertEquals;


public class TunnelImplTest
{

    private static final String TUNNEL_NAME = "tunnel1";
    private static final String TUNNEL_IP = "tunnel ip";
    private static final long TUNNEL_ID = 1;


    @Test
    public void testProperties() throws Exception
    {
        Tunnel tunnel = new Tunnel( TUNNEL_NAME, TUNNEL_IP );

        assertEquals( TUNNEL_NAME, tunnel.getTunnelName() );
        assertEquals( TUNNEL_IP, tunnel.getTunnelIp() );
        assertEquals( TUNNEL_ID, tunnel.getTunnelId() );
    }
}
