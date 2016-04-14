package io.subutai.core.network.impl;


import java.util.UUID;

import org.junit.Test;

import io.subutai.common.settings.Common;

import static org.junit.Assert.assertNotNull;


public class CommandsTest
{
    private static final String INTERFACE_NAME = "interface name";
    private static final String COMMUNITY_NAME = "community name";
    private static final String LOCAL_IP = "local.ip";
    private static final String TUNNEL_NAME = "tunnel name";
    private static final String TUNNEL_IP = "tunnel.ip";
    private static final String TUNNEL_TYPE = "tunnel type";
    private static final String GATEWAY_IP = "gateway.ip";
    private static final int VLAN_ID = 100;
    private static final String ENVIRONMENT_ID = UUID.randomUUID().toString();
    private static final String CONTAINER_NAME = "container";
    private static final String SECRET_KEY = "secret key";
    private static final int NET_MASK = 24;
    private static final int VNI = 100;
    private static final String KEY = "KEY";
    private static final String DOMAIN = "domain";
    Commands commands = new Commands();


    @Test
    public void testGetSetupP2PConnectionCommand() throws Exception
    {
        assertNotNull( commands.getJoinP2PSwarmCommand( INTERFACE_NAME, LOCAL_IP, COMMUNITY_NAME, SECRET_KEY,
                Common.DEFAULT_P2P_SECRET_KEY_TTL_SEC ) );
    }
}