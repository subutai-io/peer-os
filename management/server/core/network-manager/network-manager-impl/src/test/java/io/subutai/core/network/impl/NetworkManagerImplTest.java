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
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.ResourceHost;
import io.subutai.core.network.api.NetworkManagerException;
import io.subutai.core.peer.api.PeerManager;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
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
}
