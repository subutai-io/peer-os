package io.subutai.core.network.impl;


import java.util.Date;
import java.util.UUID;

import org.junit.Test;

import io.subutai.common.network.ProxyLoadBalanceStrategy;
import io.subutai.common.settings.Common;

import static org.junit.Assert.assertNotNull;


public class CommandsTest
{
    private static final String INTERFACE_NAME = "interface name";
    private static final String P2P_HASH = "community name";
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
    public void testGetP2pVersionCommand() throws Exception
    {
        assertNotNull( commands.getGetP2pVersionCommand() );
    }


    @Test
    public void testGetP2PConnectionsCommand() throws Exception
    {
        assertNotNull( commands.getP2PConnectionsCommand() );
    }


    @Test
    public void testGetJoinP2PSwarmCommand() throws Exception
    {
        assertNotNull( commands.getJoinP2PSwarmCommand( INTERFACE_NAME, LOCAL_IP, P2P_HASH, SECRET_KEY,
                Common.DEFAULT_P2P_SECRET_KEY_TTL_SEC, "0-65535" ) );
    }


    @Test
    public void testGetResetP2PSecretKey() throws Exception
    {
        assertNotNull( commands.getResetP2PSecretKey( P2P_HASH, SECRET_KEY, Common.DEFAULT_P2P_SECRET_KEY_TTL_SEC ) );
    }


    @Test
    public void testGetGetP2pLogsCommand() throws Exception
    {
        assertNotNull( commands.getGetP2pLogsCommand( new Date(), new Date() ) );
    }


    @Test
    public void testGetCreateTunnelCommand() throws Exception
    {
        assertNotNull( commands.getCreateTunnelCommand( TUNNEL_NAME, TUNNEL_IP, VLAN_ID, VNI ) );
    }


    @Test
    public void testGetGetTunnelsCommand() throws Exception
    {
        assertNotNull( commands.getGetTunnelsCommand() );
    }


    @Test
    public void testGetGetVlanDomainCommand() throws Exception
    {
        assertNotNull( commands.getGetVlanDomainCommand( VLAN_ID ) );
    }


    @Test
    public void testGetRemoveVlanDomainCommand() throws Exception
    {
        assertNotNull( commands.getRemoveVlanDomainCommand( VLAN_ID + "" ) );
    }


    @Test
    public void testGetSetVlanDomainCommand() throws Exception
    {
        assertNotNull( commands.getSetVlanDomainCommand( VLAN_ID + "", DOMAIN, ProxyLoadBalanceStrategy.STICKY_SESSION,
                "path" ) );
    }


    @Test
    public void testGetCheckIpInVlanDomainCommand() throws Exception
    {
        assertNotNull( commands.getCheckIpInVlanDomainCommand( LOCAL_IP, VLAN_ID ) );
    }


    @Test
    public void testGetAddIpToVlanDomainCommand() throws Exception
    {
        assertNotNull( commands.getAddIpToVlanDomainCommand( LOCAL_IP, VLAN_ID + "" ) );
    }


    @Test
    public void testGetRemoveIpFromVlanDomainCommand() throws Exception
    {
        assertNotNull( commands.getRemoveIpFromVlanDomainCommand( LOCAL_IP, VLAN_ID ) );
    }


    @Test
    public void testGetSetupContainerSshTunnelCommand() throws Exception
    {
        assertNotNull( commands.getSetupContainerSshTunnelCommand( LOCAL_IP, Common.CONTAINER_SSH_TIMEOUT_SEC ) );
    }
}