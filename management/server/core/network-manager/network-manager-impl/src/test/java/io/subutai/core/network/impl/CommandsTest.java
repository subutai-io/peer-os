package io.subutai.core.network.impl;


import java.util.UUID;

import org.junit.Test;

import com.google.common.collect.Sets;

import io.subutai.common.host.HostInterface;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.settings.Common;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


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
        assertNotNull( commands.getSetupP2PConnectionCommand( INTERFACE_NAME, LOCAL_IP, COMMUNITY_NAME, SECRET_KEY,
                Common.DEFAULT_P2P_SECRET_KEY_TTL_SEC ) );
    }


    @Test
    public void testGetRemoveP2PConnectionCommand() throws Exception
    {
        assertNotNull( commands.getRemoveP2PConnectionCommand( COMMUNITY_NAME ) );
    }


    @Test
    public void testGetListP2PConnectionsCommand() throws Exception
    {
        assertNotNull( commands.getListP2PConnectionsCommand() );
    }


    @Test
    public void testGetSetupTunnelCommand() throws Exception
    {
        assertNotNull( commands.getSetupTunnelCommand( TUNNEL_NAME, TUNNEL_IP, TUNNEL_TYPE ) );
    }


    @Test
    public void testGetRemoveTunnelCommand() throws Exception
    {
        assertNotNull( commands.getRemoveTunnelCommand( TUNNEL_NAME ) );
    }


    @Test
    public void testGetListTunnelsCommand() throws Exception
    {
        assertNotNull( commands.getListTunnelsCommand() );
    }


    @Test
    public void testGetSetContainerIpCommand() throws Exception
    {
        assertNotNull( commands.getSetContainerIpCommand( CONTAINER_NAME, GATEWAY_IP, NET_MASK, VLAN_ID ) );
    }


    @Test
    public void testGetShowContainerIpCommand() throws Exception
    {
        assertNotNull( commands.getShowContainerIpCommand( CONTAINER_NAME ) );
    }


    @Test
    public void testGetRemoveContainerIpCommand() throws Exception
    {
        assertNotNull( commands.getRemoveContainerIpCommand( CONTAINER_NAME ) );
    }


    @Test
    public void testGetSetupGatewayOnContainerCommand() throws Exception
    {
        assertNotNull( commands.getSetupGatewayOnContainerCommand( GATEWAY_IP, INTERFACE_NAME ) );
    }


    @Test
    public void testGetRemoveGatewayCommand() throws Exception
    {
        assertNotNull( commands.getRemoveGatewayCommand( VLAN_ID ) );
    }


    @Test
    public void testGetRemoveGatewayOnContainerCommand() throws Exception
    {
        assertNotNull( commands.getRemoveGatewayOnContainerCommand() );
    }


    @Test
    public void testGetSetupVniVlanMappingCommand() throws Exception
    {
        assertNotNull( commands.getSetupVniVlanMappingCommand( TUNNEL_NAME, VNI, VLAN_ID, ENVIRONMENT_ID ) );
    }


    @Test
    public void testGetRemoveVniVlanMappingCommand() throws Exception
    {
        assertNotNull( commands.getRemoveVniVlanMappingCommand( TUNNEL_NAME, VNI, VLAN_ID ) );
    }


    @Test
    public void testGetCleanupEnvironmentNetworkSettingsCommand() throws Exception
    {
        assertNotNull( commands.getCleanupEnvironmentNetworkSettingsCommand( VLAN_ID ) );
    }


    @Test
    public void testGetListVniVlanMappingsCommand() throws Exception
    {
        assertNotNull( commands.getListVniVlanMappingsCommand() );
    }


    @Test
    public void testReserveVniCommand() throws Exception
    {
        assertNotNull( commands.getReserveVniCommand( VNI, VLAN_ID, ENVIRONMENT_ID ) );
    }


    @Test
    public void testGetListReservedVnisCommand() throws Exception
    {
        assertNotNull( commands.getListReservedVnisCommand() );
    }


    @Test
    public void testGetAppendSshKeyCommand() throws Exception
    {
        assertNotNull( commands.getAppendSshKeyCommand( KEY ) );
    }


    @Test
    public void testGetRemoveSshKeyCommand() throws Exception
    {
        assertNotNull( commands.getRemoveSshKeyCommand( KEY ) );
    }


    @Test
    public void testGetConfigSSHCommand() throws Exception
    {
        assertNotNull( commands.getConfigSSHCommand() );
    }


    @Test
    public void testGetReplaceSshKeyCommand() throws Exception
    {
        assertNotNull( commands.getReplaceSshKeyCommand( KEY, KEY ) );
    }


    @Test
    public void testGetAddIpHostToEtcHostsCommand() throws Exception
    {
        ContainerHost containerHost = mock( ContainerHost.class );
        final HostInterface hostInterface = mock( HostInterface.class );
        when( containerHost.getInterfaceByName( anyString() ) ).thenReturn( hostInterface );
        assertNotNull( commands.getAddIpHostToEtcHostsCommand( DOMAIN, Sets.newHashSet( containerHost ) ) );
    }
}