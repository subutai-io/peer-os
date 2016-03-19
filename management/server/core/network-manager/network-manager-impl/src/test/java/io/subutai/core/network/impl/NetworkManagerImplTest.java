package io.subutai.core.network.impl;


import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Sets;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.network.Vni;
import io.subutai.common.network.VniVlanMapping;
import io.subutai.common.network.Vnis;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.protocol.P2PConnection;
import io.subutai.common.protocol.Tunnel;
import io.subutai.common.settings.Common;
import io.subutai.core.network.api.ContainerInfo;
import io.subutai.core.network.api.NetworkManager;
import io.subutai.core.network.api.NetworkManagerException;
import io.subutai.core.peer.api.PeerManager;
import junit.framework.TestCase;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static junit.framework.TestCase.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Test for NetworkManagerImpl
 */
@RunWith( MockitoJUnitRunner.class )
public class NetworkManagerImplTest
{

    private static final String INTERFACE_NAME = "interface name";
    private static final String COMMUNITY_NAME = "community name";
    private static final String LOCAL_IP = "127.0.0.1";
    private static final String TUNNEL_NAME = "tunnel1";
    private static final int TUNNEL_ID = 1;
    private static final String TUNNEL_IP = "tunnel.ip";
    private static final String GATEWAY_IP = "gateway.ip";
    private static final int VLAN_ID = 100;
    private static final int VNI = 100;
    private static final String ENVIRONMENT_ID = UUID.randomUUID().toString();
    private static final String CONTAINER_NAME = "container";
    private static final int NET_MASK = 24;
    private static final String CONTAINER_IP_OUTPUT =
            "    - check passed: container \"bar\" exists.                                                   [  INFO "
                    + "  ]\n" + "Environment IP:  192.168.3.5/24\n" + "Vlan ID:  100\n"
                    + "Environment IP and VLAN ID.                                                                   "
                    + "[   OK    ]";
    private static final String LIST_TUNNELS_OUTPUT = "List of Tunnels\n" + "--------\n" + "tunnel1-10.2.1.3";
    private static final String LIST_P2P_OUTPUT = "Interface LocalPeerIP Hash\n" + "com 10.1.2.3 community1";
    private static final String SECRET_KEY = "secret";
    private static final String RESERVED_VNIS_OUTPUT = String.format( "%s,%s,%s", VNI, VLAN_ID, ENVIRONMENT_ID );
    private static final String VNI_VLAN_MAPPING_OUTPUT =
            String.format( "%s\t%s\t%s\t%s", TUNNEL_NAME, VNI, VLAN_ID, ENVIRONMENT_ID );
    private static final String SSH_KEY = "SSH-KEY";
    private static final String DOMAIN = "domain";

    @Mock
    PeerManager peerManager;
    @Mock
    LocalPeer localPeer;
    @Mock
    ResourceHost managementHost;
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
    @Mock
    SshManager sshManager;
    @Mock
    HostManager hostManager;

    private NetworkManagerImpl spyNetworkManager;

    private Set<ContainerHost> containers;

    private NetworkManagerImpl networkManager;


    @Before
    public void setUp() throws PeerException, CommandException
    {
        networkManager = new NetworkManagerImpl( peerManager );
        networkManager.commands = commands;
        when( peerManager.getLocalPeer() ).thenReturn( localPeer );
        when( localPeer.getManagementHost() ).thenReturn( managementHost );
        when( localPeer.getContainerHostByName( anyString() ) ).thenReturn( containerHost );
        when( localPeer.getResourceHostByContainerName( anyString() ) ).thenReturn( resourceHost );
        when( managementHost.execute( any( RequestBuilder.class ) ) ).thenReturn( commandResult );
        when( containerHost.execute( any( RequestBuilder.class ) ) ).thenReturn( commandResult );
        when( resourceHost.execute( any( RequestBuilder.class ) ) ).thenReturn( commandResult );
        when( commandResult.hasSucceeded() ).thenReturn( true );
        containers = Sets.newHashSet( containerHost );
        spyNetworkManager = spy( networkManager );
        doReturn( sshManager ).when( spyNetworkManager ).getSshManager( containers );
        doReturn( hostManager ).when( spyNetworkManager ).getHostManager( containers, DOMAIN );
    }


    @Test( expected = NullPointerException.class )
    public void testConstructor() throws Exception
    {
        new NetworkManagerImpl( null );
    }


    @Test
    public void testSetupP2PConnection() throws Exception
    {
        networkManager.setupP2PConnection( INTERFACE_NAME, LOCAL_IP, COMMUNITY_NAME, SECRET_KEY,
                Common.DEFAULT_P2P_SECRET_KEY_TTL_SEC );

        verify( localPeer ).getManagementHost();
        verify( commands ).getSetupP2PConnectionCommand( eq( INTERFACE_NAME ), eq( LOCAL_IP ), eq( COMMUNITY_NAME ),
                eq( SECRET_KEY ), anyLong() );
        verify( managementHost ).execute( any( RequestBuilder.class ) );
    }


    @Test
    public void testRemoveP2PConnection() throws Exception
    {
        networkManager.removeP2PConnection( COMMUNITY_NAME );

        verify( localPeer ).getManagementHost();
        verify( commands ).getRemoveP2PConnectionCommand( COMMUNITY_NAME );
        verify( managementHost ).execute( any( RequestBuilder.class ) );
    }


    @Test
    public void testSetupTunnel() throws Exception
    {
        networkManager.setupTunnel( TUNNEL_ID, TUNNEL_IP );

        verify( localPeer ).getManagementHost();
        verify( commands ).getSetupTunnelCommand( TUNNEL_NAME, TUNNEL_IP, NetworkManager.TUNNEL_TYPE );
        verify( managementHost ).execute( any( RequestBuilder.class ) );
    }


    @Test
    public void testRemoveTunnel() throws Exception
    {
        networkManager.removeTunnel( TUNNEL_ID );

        verify( localPeer ).getManagementHost();
        verify( commands ).getRemoveTunnelCommand( TUNNEL_NAME );
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
        verify( localPeer ).getResourceHostByContainerName( anyString() );
        verify( commands ).getSetContainerIpCommand( CONTAINER_NAME, LOCAL_IP, NET_MASK, VLAN_ID );
        verify( resourceHost ).execute( any( RequestBuilder.class ) );
    }


    @Test
    public void testRemoveContainerIp() throws Exception
    {
        networkManager.removeContainerIp( CONTAINER_NAME );

        verify( localPeer ).getContainerHostByName( CONTAINER_NAME );
        verify( localPeer ).getResourceHostByContainerName( anyString() );
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
        verify( localPeer ).getResourceHostByContainerName( anyString() );
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
    public void testListP2PConnections() throws Exception
    {
        when( commandResult.getStdOut() ).thenReturn( LIST_P2P_OUTPUT );


        Set<P2PConnection> connections = networkManager.listP2PConnections();

        TestCase.assertFalse( connections.isEmpty() );
    }


    @Test
    public void testSetupVniVLanMapping() throws Exception
    {

        networkManager.setupVniVLanMapping( TUNNEL_ID, VNI, VLAN_ID, ENVIRONMENT_ID );

        verify( localPeer ).getManagementHost();
        verify( commands ).getSetupVniVlanMappingCommand( TUNNEL_NAME, VNI, VLAN_ID, ENVIRONMENT_ID );
        verify( managementHost ).execute( any( RequestBuilder.class ) );
    }


    @Test
    public void testRemoveVniVLanMapping() throws Exception
    {

        networkManager.removeVniVLanMapping( TUNNEL_ID, VNI, VLAN_ID );

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

        doThrow( new HostNotFoundException( "" ) ).when( localPeer ).getResourceHostByContainerName( anyString() );

        networkManager.getResourceHost( CONTAINER_NAME );
    }


    @Test( expected = NetworkManagerException.class )
    public void testGetContainerHost() throws Exception
    {
        doThrow( new HostNotFoundException( "" ) ).when( localPeer ).getContainerHostByName( anyString() );

        networkManager.getContainerHost( CONTAINER_NAME );
    }


    @Test
    public void testReserveVni() throws Exception
    {
        networkManager.reserveVni( new Vni( VNI, VLAN_ID, ENVIRONMENT_ID ) );

        verify( managementHost ).execute( any( RequestBuilder.class ) );
    }


    @Test
    public void testGetReservedVnis() throws Exception
    {
        when( commandResult.getStdOut() ).thenReturn( RESERVED_VNIS_OUTPUT );

        Vnis vnis = networkManager.getReservedVnis();

        assertNotNull( vnis.findVlanByVni( VNI ) );
        assertNotNull( vnis.findVniByEnvironmentId( ENVIRONMENT_ID ) );
    }


    @Test
    public void testGetVniVlanMappings() throws Exception
    {
        when( commandResult.getStdOut() ).thenReturn( VNI_VLAN_MAPPING_OUTPUT );


        Set<VniVlanMapping> mappings = networkManager.getVniVlanMappings();

        assertTrue( mappings.contains( new VniVlanMapping( TUNNEL_ID, VNI, VLAN_ID, ENVIRONMENT_ID ) ) );
    }


    @Test
    public void testExchangeSshKeys() throws Exception
    {
        spyNetworkManager.exchangeSshKeys( containers, Sets.<String>newHashSet() );

        verify( sshManager ).execute( Sets.<String>newHashSet(), false );
    }


    @Test
    public void testAddSshKeyToAuthorizedKeys() throws Exception
    {

        spyNetworkManager.addSshKeyToAuthorizedKeys( containers, SSH_KEY );

        verify( sshManager ).appendSshKey( SSH_KEY );
    }


    @Test
    public void testReplaceSshKeyInAuthorizedKeys() throws Exception
    {
        spyNetworkManager.replaceSshKeyInAuthorizedKeys( containers, SSH_KEY, SSH_KEY );

        verify( sshManager ).replaceSshKey( SSH_KEY, SSH_KEY );
    }


    @Test
    public void testRemoveSshKeyFromAuthorizedKeys() throws Exception
    {
        spyNetworkManager.removeSshKeyFromAuthorizedKeys( containers, SSH_KEY );

        verify( sshManager ).removeSshKey( SSH_KEY );
    }


    @Test
    public void testRegisterHosts() throws Exception
    {

        spyNetworkManager.registerHosts( containers, DOMAIN );

        verify( hostManager ).execute();
    }


    @Test
    public void testGetHostManager() throws Exception
    {
        assertNotNull( networkManager.getHostManager( containers, DOMAIN ) );
    }


    @Test
    public void testGetSshManager() throws Exception
    {

        assertNotNull( networkManager.getSshManager( containers ) );
    }
}
