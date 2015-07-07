package io.subutai.core.peer.impl.entity;


import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.naming.NamingException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import io.subutai.common.host.HostArchitecture;
import io.subutai.common.host.Interface;
import io.subutai.common.network.Gateway;
import io.subutai.common.network.Vni;
import io.subutai.common.network.VniVlanMapping;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.util.ServiceLocator;
import io.subutai.core.hostregistry.api.ResourceHostInfo;
import io.subutai.core.network.api.NetworkManager;
import io.subutai.core.network.api.NetworkManagerException;
import io.subutai.core.network.api.Tunnel;

import io.subutai.core.repository.api.RepositoryException;
import io.subutai.core.repository.api.RepositoryManager;

import com.google.common.collect.Sets;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class ManagementHostEntityTest
{
    private static final UUID PEER_ID = UUID.randomUUID();
    private static final UUID HOST_ID = UUID.randomUUID();
    private static final UUID ENV_ID = UUID.randomUUID();
    private static final String NAME = "name";
    private static final String HOSTNAME = "hostname";
    private static final HostArchitecture ARCH = HostArchitecture.AMD64;
    private static final String INTERFACE_NAME = "eth0";
    private static final String IP = "127.0.0.1";
    private static final String MAC = "mac";
    private static final int VLAN = 100;
    private static final int VNI = 10000;
    private static final int TUNNEL_ID = 123;
    @Mock
    Peer peer;
    @Mock
    ResourceHostInfo hostInfo;
    @Mock
    Interface anInterface;
    @Mock
    ExecutorService singleThreadExecutorService;
    @Mock
    ServiceLocator serviceLocator;
    @Mock
    Callable callable;
    @Mock
    Future future;
    @Mock
    RepositoryManager repositoryManager;
    @Mock
    NetworkManager networkManager;

    ManagementHostEntity managementHostEntity;


    @Before
    public void setUp() throws Exception
    {
        when( hostInfo.getId() ).thenReturn( HOST_ID );
        when( hostInfo.getHostname() ).thenReturn( HOSTNAME );
        when( hostInfo.getArch() ).thenReturn( ARCH );
        when( hostInfo.getInterfaces() ).thenReturn( Sets.newHashSet( anInterface ) );
        when( anInterface.getInterfaceName() ).thenReturn( INTERFACE_NAME );
        when( anInterface.getIp() ).thenReturn( IP );
        when( anInterface.getMac() ).thenReturn( MAC );
        managementHostEntity = new ManagementHostEntity( PEER_ID.toString(), hostInfo, "eth1" );
        managementHostEntity.singleThreadExecutorService = singleThreadExecutorService;
        managementHostEntity.serviceLocator = serviceLocator;
        managementHostEntity.init();
        when( singleThreadExecutorService.submit( any( Callable.class ) ) ).thenReturn( future );
        when( serviceLocator.getService( RepositoryManager.class ) ).thenReturn( repositoryManager );
        when( serviceLocator.getService( NetworkManager.class ) ).thenReturn( networkManager );
    }


    @Test
    public void testDispose() throws Exception
    {
        managementHostEntity.dispose();

        verify( singleThreadExecutorService ).shutdown();
    }


    @Test
    public void testQueueSequentialTask() throws Exception
    {
        managementHostEntity.queueSequentialTask( callable );

        verify( singleThreadExecutorService ).submit( callable );
    }


    @Test
    public void testGetNSetName() throws Exception
    {
        managementHostEntity.setName( NAME );

        assertEquals( NAME, managementHostEntity.getName() );
    }


    @Test( expected = PeerException.class )
    public void testAddAptSource() throws Exception
    {
        managementHostEntity.addAptSource( HOSTNAME, IP );

        verify( repositoryManager ).addAptSource( HOSTNAME, IP );

        doThrow( new RepositoryException( "" ) ).when( repositoryManager ).addAptSource( anyString(), anyString() );

        managementHostEntity.addAptSource( HOSTNAME, IP );
    }


    @Test( expected = PeerException.class )
    public void testRemoveAptSource() throws Exception
    {
        managementHostEntity.removeAptSource( HOSTNAME, IP );

        verify( repositoryManager ).removeAptSource( IP );

        doThrow( new RepositoryException( "" ) ).when( repositoryManager ).removeAptSource( anyString() );

        managementHostEntity.removeAptSource( HOSTNAME, IP );
    }


    @Test
    public void testCreateGateway() throws Exception
    {
        managementHostEntity.createGateway( IP, VLAN );

        verify( future ).get();

        doThrow( new InterruptedException() ).when( future ).get();

        try
        {
            managementHostEntity.createGateway( IP, VLAN );
            fail( "Expected PeerException" );
        }
        catch ( PeerException e )
        {
        }

        doThrow( new ExecutionException( null ) ).when( future ).get();

        try
        {
            managementHostEntity.createGateway( IP, VLAN );
            fail( "Expected PeerException" );
        }
        catch ( PeerException e )
        {
        }

        doThrow( new ExecutionException( new PeerException( "" ) ) ).when( future ).get();

        try
        {
            managementHostEntity.createGateway( IP, VLAN );
            fail( "Expected PeerException" );
        }
        catch ( PeerException e )
        {
        }
    }


    @Test
    public void testReserveVni() throws Exception
    {
        Vni vni = mock( Vni.class );
        when( future.get() ).thenReturn( new Integer( VLAN ) );

        managementHostEntity.reserveVni( vni );

        verify( future ).get();

        doThrow( new InterruptedException() ).when( future ).get();

        try
        {
            managementHostEntity.reserveVni( vni );
            fail( "Expected PeerException" );
        }
        catch ( PeerException e )
        {
        }

        doThrow( new ExecutionException( null ) ).when( future ).get();

        try
        {
            managementHostEntity.reserveVni( vni );
            fail( "Expected PeerException" );
        }
        catch ( PeerException e )
        {
        }

        doThrow( new ExecutionException( new PeerException( "" ) ) ).when( future ).get();

        try
        {
            managementHostEntity.reserveVni( vni );
            fail( "Expected PeerException" );
        }
        catch ( PeerException e )
        {
        }
    }


    @Test
    public void testSetupTunnels() throws Exception
    {
        when( future.get() ).thenReturn( new Integer( VLAN ) );

        managementHostEntity.setupTunnels( Sets.newHashSet( IP ), ENV_ID );

        verify( future ).get();

        doThrow( new InterruptedException() ).when( future ).get();

        try
        {
            managementHostEntity.setupTunnels( Sets.newHashSet( IP ), ENV_ID );
            fail( "Expected PeerException" );
        }
        catch ( PeerException e )
        {
        }

        doThrow( new ExecutionException( null ) ).when( future ).get();

        try
        {
            managementHostEntity.setupTunnels( Sets.newHashSet( IP ), ENV_ID );
            fail( "Expected PeerException" );
        }
        catch ( PeerException e )
        {
        }

        doThrow( new ExecutionException( new PeerException( "" ) ) ).when( future ).get();

        try
        {
            managementHostEntity.setupTunnels( Sets.newHashSet( IP ), ENV_ID );
            fail( "Expected PeerException" );
        }
        catch ( PeerException e )
        {
        }
    }


    @Test( expected = PeerException.class )
    public void testRemoveGateway() throws Exception
    {
        managementHostEntity.removeGateway( VLAN );

        verify( networkManager ).removeGateway( VLAN );

        doThrow( new NetworkManagerException( "" ) ).when( networkManager ).removeGateway( VLAN );

        managementHostEntity.removeGateway( VLAN );
    }


    @Test( expected = PeerException.class )
    public void testCleanupEnvironmentNetworkSettings() throws Exception
    {
        managementHostEntity.cleanupEnvironmentNetworkSettings( ENV_ID );

        verify( networkManager ).cleanupEnvironmentNetworkSettings( ENV_ID );

        doThrow( new NetworkManagerException( "" ) ).when( networkManager ).cleanupEnvironmentNetworkSettings( ENV_ID );

        managementHostEntity.cleanupEnvironmentNetworkSettings( ENV_ID );
    }


    @Test( expected = PeerException.class )
    public void testListTunnels() throws Exception
    {
        managementHostEntity.listTunnels();

        verify( networkManager ).listTunnels();

        doThrow( new NetworkManagerException( "" ) ).when( networkManager ).listTunnels();

        managementHostEntity.listTunnels();
    }


    @Test( expected = PeerException.class )
    public void testRemoveTunnel() throws Exception
    {
        Tunnel tunnel = mock( Tunnel.class );
        when( networkManager.listTunnels() ).thenReturn( Sets.newHashSet( tunnel ) );
        when( tunnel.getTunnelIp() ).thenReturn( IP );

        managementHostEntity.removeTunnel( IP );

        verify( networkManager ).removeTunnel( anyInt() );

        doThrow( new NetworkManagerException( "" ) ).when( networkManager ).removeTunnel( anyInt() );

        managementHostEntity.removeTunnel( IP );
    }


    @Test( expected = PeerException.class )
    public void testGetReservedVnis() throws Exception
    {
        managementHostEntity.getReservedVnis();

        verify( networkManager ).getReservedVnis();

        doThrow( new NetworkManagerException( "" ) ).when( networkManager ).getReservedVnis();

        managementHostEntity.getReservedVnis();
    }


    @Test
    public void testGetGateways() throws Exception
    {
        HostInterface hostInterface = mock( HostInterface.class );
        when( hostInterface.getInterfaceName() ).thenReturn( "br-100" );

        managementHostEntity.addInterface( hostInterface );

        Set<Gateway> gateways = managementHostEntity.getGateways();

        assertFalse( gateways.isEmpty() );
    }


    @Test
    public void testFindVniByEnvironmentById() throws Exception
    {

        assertNull( managementHostEntity.findVniByEnvironmentId( ENV_ID ) );

        Vni vni = mock( Vni.class );
        when( vni.getEnvironmentId() ).thenReturn( ENV_ID );
        when( networkManager.getReservedVnis() ).thenReturn( Sets.newHashSet( vni ) );

        Vni vni2 = managementHostEntity.findVniByEnvironmentId( ENV_ID );

        assertEquals( vni, vni2 );
    }


    @Test( expected = PeerException.class )
    public void testSetupVniVlanMapping() throws Exception
    {
        VniVlanMapping vniVlanMapping = mock( VniVlanMapping.class );
        when( networkManager.getVniVlanMappings() ).thenReturn( Sets.newHashSet( vniVlanMapping ) );
        when( vniVlanMapping.getTunnelId() ).thenReturn( TUNNEL_ID );
        when( vniVlanMapping.getEnvironmentId() ).thenReturn( ENV_ID );

        managementHostEntity.setupVniVlanMapping( TUNNEL_ID, VNI, VLAN, ENV_ID );

        verify( networkManager, never() ).setupVniVLanMapping( TUNNEL_ID, VNI, VLAN, ENV_ID );

        when( vniVlanMapping.getTunnelId() ).thenReturn( TUNNEL_ID + 1 );

        managementHostEntity.setupVniVlanMapping( TUNNEL_ID, VNI, VLAN, ENV_ID );

        verify( networkManager ).setupVniVLanMapping( TUNNEL_ID, VNI, VLAN, ENV_ID );

        doThrow( new NetworkManagerException( "" ) ).when( networkManager )
                                                    .setupVniVLanMapping( TUNNEL_ID, VNI, VLAN, ENV_ID );

        managementHostEntity.setupVniVlanMapping( TUNNEL_ID, VNI, VLAN, ENV_ID );
    }


    @Test
    public void testFindTunnel() throws Exception
    {
        Tunnel tunnel = mock( Tunnel.class );
        when( tunnel.getTunnelIp() ).thenReturn( IP );
        when( tunnel.getTunnelId() ).thenReturn( TUNNEL_ID );

        int tunnelId = managementHostEntity.findTunnel( IP, Sets.newHashSet( tunnel ) );

        assertEquals( TUNNEL_ID, tunnelId );

        when( tunnel.getTunnelIp() ).thenReturn( "" );

        tunnelId = managementHostEntity.findTunnel( IP, Sets.newHashSet( tunnel ) );

        assertThat( TUNNEL_ID, not( tunnelId ) );
    }


    @Test
    public void testCalculateNextTunnelId() throws Exception
    {
        Tunnel tunnel = mock( Tunnel.class );
        when( tunnel.getTunnelId() ).thenReturn( TUNNEL_ID );

        int newTunnelId = managementHostEntity.calculateNextTunnelId( Sets.newHashSet( tunnel ) );

        assertEquals( ( TUNNEL_ID + 1 ), newTunnelId );
    }


    @Test
    public void testFindAvailableVlanId() throws Exception
    {
        Vni vni = mock( Vni.class );
        when( vni.getVlan() ).thenReturn( VLAN );
        when( networkManager.getReservedVnis() ).thenReturn( Sets.newHashSet( vni ) );

        int newVlanId = managementHostEntity.findAvailableVlanId();

        assertEquals( ( VLAN + 1 ), newVlanId );
    }


    @Test( expected = PeerException.class )
    public void testGetNetworkManager() throws Exception
    {
        assertEquals( networkManager, managementHostEntity.getNetworkManager() );

        doThrow( new NamingException() ).when( serviceLocator ).getService( any( Class.class ) );

        managementHostEntity.getNetworkManager();
    }


    @Test( expected = PeerException.class )
    public void testGetRepositoryManager() throws Exception
    {
        assertEquals( repositoryManager, managementHostEntity.getRepositoryManager() );

        doThrow( new NamingException() ).when( serviceLocator ).getService( any( Class.class ) );

        managementHostEntity.getRepositoryManager();
    }
}


