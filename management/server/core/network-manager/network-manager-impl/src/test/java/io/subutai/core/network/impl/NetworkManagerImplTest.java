package io.subutai.core.network.impl;


import java.util.Date;
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
import io.subutai.common.network.JournalCtlLevel;
import io.subutai.common.network.P2pLogs;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.protocol.P2PConnections;
import io.subutai.common.protocol.Tunnels;
import io.subutai.common.settings.Common;
import io.subutai.common.settings.SystemSettings;
import io.subutai.core.network.api.NetworkManagerException;
import io.subutai.core.peer.api.PeerManager;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNull;
import static org.mockito.Matchers.any;
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

    private static final String P2P_HASH = "interface name";
    private static final String COMMUNITY_NAME = "community name";
    private static final String LOCAL_IP = "127.0.0.1";
    private static final String TUNNEL_NAME = "tunnel1";
    private static final int TUNNEL_ID = 1;
    private static final String TUNNEL_IP = "10.10.10.10";
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
    private static final String LIST_TUNNELS_OUTPUT = "List of Tunnels\n" + "--------\n" + "tunnel1 10.2.1.3 123 321";
    private static final String LIST_P2P_OUTPUT = "Interface LocalPeerIP Hash\n" + "com 10.1.2.3 community1";
    private static final String SECRET_KEY = "secret";
    private static final String RESERVED_VNIS_OUTPUT = String.format( "%s,%s,%s", VNI, VLAN_ID, ENVIRONMENT_ID );
    private static final String VNI_VLAN_MAPPING_OUTPUT =
            String.format( "%s\t%s\t%s\t%s", TUNNEL_NAME, VNI, VLAN_ID, ENVIRONMENT_ID );
    private static final String SSH_KEY = "SSH-KEY";
    private static final String DOMAIN = "domain";
    private static final String P2P_LOG_OUTPUT = "[INFO] bla\n[WARNING] bla\n[ERROR] bla\ntest";

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
    SystemSettings systemSettings2;


    private NetworkManagerImpl spyNetworkManager;

    private Set<ContainerHost> containers;

    private NetworkManagerImpl networkManager;


    class NetworkManagerImplForTest extends NetworkManagerImpl
    {

        public NetworkManagerImplForTest( final PeerManager peerManager )
        {
            super( peerManager );
        }


        protected SystemSettings getSystemSettings()
        {
            return systemSettings2;
        }
    }


    @Before
    public void setUp() throws PeerException, CommandException
    {
        networkManager = spy( new NetworkManagerImplForTest( peerManager ) );
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
    }


    @Test( expected = NullPointerException.class )
    public void testConstructor() throws Exception
    {
        new NetworkManagerImpl( null );
    }


    @Test( expected = NetworkManagerException.class )
    public void testExecute() throws Exception
    {
        when( commandResult.hasSucceeded() ).thenReturn( false );

        networkManager.execute( containerHost, requestBuilder );
    }


    @Test()
    public void testExecuteOK() throws Exception
    {
        when( commandResult.hasSucceeded() ).thenReturn( true );

        CommandResult result = networkManager.execute( containerHost, requestBuilder );

        assertEquals( commandResult, result );
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


    @Test
    public void testJoinP2PSwarm() throws Exception
    {
        networkManager.joinP2PSwarm( resourceHost, P2P_HASH, LOCAL_IP, P2P_HASH, SECRET_KEY,
                Common.DEFAULT_P2P_SECRET_KEY_TTL_SEC );

        verify( networkManager ).execute( eq( resourceHost ), any( RequestBuilder.class ) );
    }


    @Test
    public void testResetSwarmSecretKey() throws Exception
    {
        networkManager.resetSwarmSecretKey( resourceHost, P2P_HASH, SECRET_KEY, Common.DEFAULT_P2P_SECRET_KEY_TTL_SEC );

        verify( networkManager ).execute( eq( resourceHost ), any( RequestBuilder.class ) );
    }


    @Test
    public void testGetP2PConnections() throws Exception
    {
        doReturn( LIST_P2P_OUTPUT ).when( commandResult ).getStdOut();

        P2PConnections p2PConnections = networkManager.getP2PConnections( resourceHost );

        assertFalse( p2PConnections.getConnections().isEmpty() );
    }


    @Test
    public void testGetP2pVersion() throws Exception
    {
        networkManager.getP2pVersion( resourceHost );

        verify( commandResult ).getStdOut();
    }


    @Test( expected = NetworkManagerException.class )
    public void testGetP2pLogs() throws Exception
    {
        doReturn( P2P_LOG_OUTPUT ).when( commandResult ).getStdOut();

        P2pLogs p2pLogs = networkManager.getP2pLogs( resourceHost, JournalCtlLevel.ALL, new Date(), new Date() );

        assertFalse( p2pLogs.isEmpty() );

        p2pLogs = networkManager.getP2pLogs( resourceHost, JournalCtlLevel.ERROR, new Date(), new Date() );

        assertFalse( p2pLogs.isEmpty() );

        doThrow( new CommandException( "" ) ).when( resourceHost ).execute( any( RequestBuilder.class ) );

        networkManager.getP2pLogs( resourceHost, JournalCtlLevel.ERROR, new Date(), new Date() );
    }


    @Test
    public void testCreateTunnel() throws Exception
    {
        networkManager.createTunnel( resourceHost, TUNNEL_NAME, TUNNEL_IP, VLAN_ID, VNI );

        verify( networkManager ).execute( eq( resourceHost ), any( RequestBuilder.class ) );
    }


    @Test
    public void testGetTunnels() throws Exception
    {
        doReturn( LIST_TUNNELS_OUTPUT ).when( commandResult ).getStdOut();

        Tunnels tunnels = networkManager.getTunnels( resourceHost );

        assertFalse( tunnels.isEmpty() );
    }


    @Test( expected = NetworkManagerException.class )
    public void testGetVlanDomain() throws Exception
    {
        networkManager.getVlanDomain( VLAN_ID );

        verify( commandResult ).getStdOut();


        doReturn( false ).when( commandResult ).hasSucceeded();

        assertNull( networkManager.getVlanDomain( VLAN_ID ) );

        doThrow( new CommandException( "" ) ).when( managementHost ).execute( any( RequestBuilder.class ) );

        networkManager.getVlanDomain( VLAN_ID );
    }
}
