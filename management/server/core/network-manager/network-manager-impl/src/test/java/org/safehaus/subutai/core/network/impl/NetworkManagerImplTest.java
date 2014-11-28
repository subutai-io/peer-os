package org.safehaus.subutai.core.network.impl;


import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.core.network.api.ContainerInfo;
import org.safehaus.subutai.core.network.api.N2NConnection;
import org.safehaus.subutai.core.network.api.NetworkManagerException;
import org.safehaus.subutai.core.network.api.Tunnel;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.peer.api.HostNotFoundException;
import org.safehaus.subutai.core.peer.api.LocalPeer;
import org.safehaus.subutai.core.peer.api.ManagementHost;
import org.safehaus.subutai.core.peer.api.PeerException;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.peer.api.ResourceHost;

import junit.framework.TestCase;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.fail;
import static junit.framework.TestCase.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Test for NetworkManagerImpl
 */
@RunWith( MockitoJUnitRunner.class )
public class NetworkManagerImplTest
{

    private static final String SUPER_NODE_IP = "super.node.ip";
    private static final int SUPER_NODE_PORT = 1234;
    private static final String INTERFACE_NAME = "interface name";
    private static final String COMMUNITY_NAME = "community name";
    private static final String LOCAL_IP = "local.ip";
    private static final String TUNNEL_NAME = "tunnel name";
    private static final String TUNNEL_IP = "tunnel.ip";
    private static final String TUNNEL_TYPE = "tunnel type";
    private static final String GATEWAY_IP = "gateway.ip";
    private static final int VLAN_ID = 100;
    private static final int VNI = 100;
    private static final String CONTAINER_NAME = "container";
    private static final int NET_MASK = 24;
    private static final String CONTAINER_IP_OUTPUT =
            "    - check passed: container \"bar\" exists.                                                   [  INFO "
                    + "  ]\n" + "Environment IP:  192.168.3.5/24\n" + "Vlan ID:  100\n"
                    + "Environment IP and VLAN ID.                                                                   "
                    + "[   OK    ]";
    private static final String LIST_TUNNELS_OUTPUT = "List of Tunnels\n" + "--------\n" + "tunnel1-10.2.1.3";
    private static final String LIST_N2N_OUTPUT = "LocalPeerIP ServerIP Port LocalInterface Community\n"
            + "10.1.2.3    212.167.154.154 5000    com community1 ";
    private static final String PATH_TO_KEY_FILE = "/path/to/key/file";

    @Mock
    PeerManager peerManager;
    @Mock
    LocalPeer localPeer;
    @Mock
    ManagementHost managementHost;
    @Mock
    ResourceHost resourceHost;
    @Mock
    ContainerHost containerHost;
    @Mock
    CommandResult commandResult;
    @Mock
    Commands commands;
    @Mock
    RequestBuilder requestBuilder;


    private NetworkManagerImpl networkManager;


    @Before
    public void setUp() throws PeerException, CommandException
    {
        networkManager = new NetworkManagerImpl( peerManager );
        networkManager.commands = commands;
        when( peerManager.getLocalPeer() ).thenReturn( localPeer );
        when( localPeer.getManagementHost() ).thenReturn( managementHost );
        when( localPeer.getContainerHostByName( anyString() ) ).thenReturn( containerHost );
        when( localPeer.getResourceHostByName( anyString() ) ).thenReturn( resourceHost );
        when( managementHost.execute( any( RequestBuilder.class ) ) ).thenReturn( commandResult );
        when( containerHost.execute( any( RequestBuilder.class ) ) ).thenReturn( commandResult );
        when( resourceHost.execute( any( RequestBuilder.class ) ) ).thenReturn( commandResult );
        when( commandResult.hasSucceeded() ).thenReturn( true );
    }


    @Test( expected = NullPointerException.class )
    public void testConstructor() throws Exception
    {
        new NetworkManagerImpl( null );
    }


    @Test
    public void testSetupN2NConnection() throws Exception
    {
        networkManager.setupN2NConnection( SUPER_NODE_IP, SUPER_NODE_PORT, INTERFACE_NAME, COMMUNITY_NAME, LOCAL_IP,
                PATH_TO_KEY_FILE );

        verify( localPeer ).getManagementHost();
        verify( commands )
                .getSetupN2NConnectionCommand( SUPER_NODE_IP, SUPER_NODE_PORT, INTERFACE_NAME, COMMUNITY_NAME, LOCAL_IP,
                        PATH_TO_KEY_FILE );
        verify( managementHost ).execute( any( RequestBuilder.class ) );
    }


    @Test
    public void testRemoveN2NConnection() throws Exception
    {
        networkManager.removeN2NConnection( INTERFACE_NAME, COMMUNITY_NAME );

        verify( localPeer ).getManagementHost();
        verify( commands ).getRemoveN2NConnectionCommand( INTERFACE_NAME, COMMUNITY_NAME );
        verify( managementHost ).execute( any( RequestBuilder.class ) );
    }


    @Test
    public void testSetupTunnel() throws Exception
    {
        networkManager.setupTunnel( TUNNEL_NAME, TUNNEL_IP, TUNNEL_TYPE );

        verify( localPeer ).getManagementHost();
        verify( commands ).getSetupTunnelCommand( TUNNEL_NAME, TUNNEL_IP, TUNNEL_TYPE );
        verify( managementHost ).execute( any( RequestBuilder.class ) );
    }


    @Test
    public void testRemoveTunnel() throws Exception
    {
        networkManager.removeTunnel( TUNNEL_NAME );

        verify( localPeer ).getManagementHost();
        verify( commands ).getRemoveTunnelCommand( TUNNEL_NAME );
        verify( managementHost ).execute( any( RequestBuilder.class ) );
    }


    @Test
    public void testSetupGateway() throws Exception
    {
        networkManager.setupGateway( GATEWAY_IP, VLAN_ID );


        verify( localPeer ).getManagementHost();
        verify( commands ).getSetupGatewayCommand( GATEWAY_IP, VLAN_ID );
        verify( managementHost ).execute( any( RequestBuilder.class ) );
    }


    @Test
    public void testRemoveGateway() throws Exception
    {
        networkManager.removeGateway( VLAN_ID );


        verify( localPeer ).getManagementHost();
        verify( commands ).getRemoveGatewayCommand( VLAN_ID );
        verify( managementHost ).execute( any( RequestBuilder.class ) );
    }


    @Test
    public void testSetupGatewayOnContainer() throws Exception
    {
        networkManager.setupGatewayOnContainer( CONTAINER_NAME, GATEWAY_IP, INTERFACE_NAME );


        verify( localPeer ).getContainerHostByName( CONTAINER_NAME );
        verify( commands ).getSetupGatewayOnContainerCommand( GATEWAY_IP, INTERFACE_NAME );
        verify( containerHost ).execute( any( RequestBuilder.class ) );
    }


    @Test
    public void testRemoveGatewayOnContainer() throws Exception
    {
        networkManager.removeGatewayOnContainer( CONTAINER_NAME );


        verify( localPeer ).getContainerHostByName( CONTAINER_NAME );
        verify( commands ).getRemoveGatewayOnContainerCommand();
        verify( containerHost ).execute( any( RequestBuilder.class ) );
    }


    @Test
    public void testSetContainerIp() throws Exception
    {
        networkManager.setContainerIp( CONTAINER_NAME, LOCAL_IP, NET_MASK, VLAN_ID );

        verify( localPeer ).getContainerHostByName( CONTAINER_NAME );
        verify( localPeer ).getResourceHostByName( anyString() );
        verify( commands ).getSetContainerIpCommand( CONTAINER_NAME, LOCAL_IP, NET_MASK, VLAN_ID );
        verify( resourceHost ).execute( any( RequestBuilder.class ) );
    }


    @Test
    public void testRemoveContainerIp() throws Exception
    {
        networkManager.removeContainerIp( CONTAINER_NAME );

        verify( localPeer ).getContainerHostByName( CONTAINER_NAME );
        verify( localPeer ).getResourceHostByName( anyString() );
        verify( commands ).getRemoveContainerIpCommand( CONTAINER_NAME );
        verify( resourceHost ).execute( any( RequestBuilder.class ) );
    }


    @Test
    public void testGetContainerIp() throws Exception
    {
        when( commandResult.getStdOut() ).thenReturn( CONTAINER_IP_OUTPUT );

        ContainerInfo containerInfo = networkManager.getContainerIp( CONTAINER_NAME );

        assertNotNull( containerInfo );
        verify( localPeer ).getContainerHostByName( CONTAINER_NAME );
        verify( localPeer ).getResourceHostByName( anyString() );
        verify( commands ).getShowContainerIpCommand( CONTAINER_NAME );


        when( commandResult.getStdOut() ).thenReturn( "" );

        try
        {
            networkManager.getContainerIp( CONTAINER_NAME );
            fail( "Expected NetworkManagerException" );
        }
        catch ( NetworkManagerException e )
        {
        }
    }


    @Test
    public void testListTunnels() throws Exception
    {
        when( commandResult.getStdOut() ).thenReturn( LIST_TUNNELS_OUTPUT );

        Set<Tunnel> tunnels = networkManager.listTunnels();

        assertFalse( tunnels.isEmpty() );
    }


    @Test
    public void testListN2NConnections() throws Exception
    {
        when( commandResult.getStdOut() ).thenReturn( LIST_N2N_OUTPUT );


        Set<N2NConnection> connections = networkManager.listN2NConnections();

        TestCase.assertFalse( connections.isEmpty() );
    }


    @Test
    public void testSetupVniVLanMapping() throws Exception
    {

        networkManager.setupVniVLanMapping( TUNNEL_NAME, VNI, VLAN_ID );

        verify( localPeer ).getManagementHost();
        verify( commands ).getSetupVniVlanMappingCommand( TUNNEL_NAME, VNI, VLAN_ID );
        verify( managementHost ).execute( any( RequestBuilder.class ) );
    }


    @Test
    public void testRemoveVniVLanMapping() throws Exception
    {

        networkManager.removeVniVLanMapping( TUNNEL_NAME, VNI, VLAN_ID );

        verify( localPeer ).getManagementHost();
        verify( commands ).getRemoveVniVlanMappingCommand( TUNNEL_NAME, VNI, VLAN_ID );
        verify( managementHost ).execute( any( RequestBuilder.class ) );
    }


    @Test( expected = NetworkManagerException.class )
    public void testExecute() throws Exception
    {
        when( commandResult.hasSucceeded() ).thenReturn( false );
        networkManager.execute( containerHost, requestBuilder );
    }


    @Test( expected = NetworkManagerException.class )
    public void testExecute2() throws Exception
    {
        doThrow( new CommandException( "" ) ).when( containerHost ).execute( requestBuilder );
        networkManager.execute( containerHost, requestBuilder );
    }


    @Test( expected = NetworkManagerException.class )
    public void testGetManagementHost() throws Exception
    {
        doThrow( new HostNotFoundException( "" ) ).when( localPeer ).getManagementHost();

        networkManager.getManagementHost();
    }


    @Test( expected = NetworkManagerException.class )
    public void testGetResourceHost() throws Exception
    {

        doThrow( new HostNotFoundException( "" ) ).when( localPeer ).getResourceHostByName( anyString() );

        networkManager.getResourceHost( CONTAINER_NAME );
    }


    @Test( expected = NetworkManagerException.class )
    public void testGetContainerHost() throws Exception
    {
        doThrow( new HostNotFoundException( "" ) ).when( localPeer ).getContainerHostByName( anyString() );

        networkManager.getContainerHost( CONTAINER_NAME );
    }
}
