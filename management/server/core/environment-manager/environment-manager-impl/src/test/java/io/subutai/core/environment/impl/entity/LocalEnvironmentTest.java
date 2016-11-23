package io.subutai.core.environment.impl.entity;


import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Sets;

import io.subutai.common.environment.EnvironmentStatus;
import io.subutai.common.peer.AlertHandlerPriority;
import io.subutai.common.peer.EnvironmentAlertHandler;
import io.subutai.common.peer.EnvironmentContainerHost;
import io.subutai.common.peer.Peer;
import io.subutai.core.environment.impl.EnvironmentManagerImpl;
import io.subutai.core.identity.api.IdentityManager;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class LocalEnvironmentTest
{
    private static final String ENV_NAME = "env";
    private static final String SSH_KEY = "key";
    private static final String PEER_ID = "id";
    private static final String CONTAINER_ID = "id";
    private static final String CONTAINER_HOSTNAME = "name";
    private static final Long USER_ID = 123L;

    @Mock
    EnvironmentManagerImpl environmentManager;
    @Mock
    EnvironmentContainerImpl environmentContainer;
    @Mock
    IdentityManager identityManager;
    @Mock
    EnvironmentPeerImpl environmentPeer;
    @Mock
    Peer peer;

    LocalEnvironment environment;


    @Before
    public void setUp() throws Exception
    {
        environment = new LocalEnvironment( ENV_NAME, SSH_KEY, USER_ID, PEER_ID );

        environment.setEnvironmentManager( environmentManager );

        environment.addContainers( Sets.newHashSet( environmentContainer ) );

        doReturn( CONTAINER_HOSTNAME ).when( environmentContainer ).getHostname();
        doReturn( CONTAINER_ID ).when( environmentContainer ).getId();
        doReturn( PEER_ID ).when( environmentContainer ).getPeerId();
        doReturn( identityManager ).when( environmentManager ).getIdentityManager();
        doReturn( PEER_ID ).when( environmentPeer ).getPeerId();
        doReturn( PEER_ID ).when( peer ).getId();
        doReturn( peer ).when( environmentManager ).resolvePeer( PEER_ID );
    }


    @Test
    public void testAddSshKey() throws Exception
    {
        environment.addSshKey( SSH_KEY, true );

        verify( environmentManager ).addSshKey( environment.getId(), SSH_KEY, true );
    }


    @Test
    public void testRemoveSshKey() throws Exception
    {
        environment.removeSshKey( SSH_KEY, true );

        verify( environmentManager ).removeSshKey( environment.getId(), SSH_KEY, true );
    }


    @Test
    public void testSshKeys() throws Exception
    {
        environment.addSshKey( SSH_KEY );

        assertTrue( environment.getSshKeys().contains( SSH_KEY ) );

        environment.removeSshKey( SSH_KEY );

        assertFalse( environment.getSshKeys().contains( SSH_KEY ) );
    }


    @Test
    public void getCreationTimestamp() throws Exception
    {
        assertTrue( environment.getCreationTimestamp() > 0 );
    }


    @Test
    public void testGetName() throws Exception
    {
        assertEquals( ENV_NAME, environment.getName() );
    }


    @Test
    public void testUserId() throws Exception
    {
        environment.setUserId( USER_ID );

        assertEquals( USER_ID, environment.getUserId() );
    }


    @Test
    public void testGetStatus() throws Exception
    {
        assertEquals( EnvironmentStatus.EMPTY, environment.getStatus() );
    }


    @Test
    public void testRawTopology() throws Exception
    {
        environment.setRawTopology( "RAW" );

        assertEquals( "RAW", environment.getRawTopology() );
    }


    @Test
    public void testGetPeerId() throws Exception
    {
        assertEquals( PEER_ID, environment.getPeerId() );
    }


    @Test
    public void testGetContainerHostById() throws Exception
    {
        assertNotNull( environment.getContainerHostById( CONTAINER_ID ) );
    }


    @Test
    public void testGetContainerHostByPeerId() throws Exception
    {
        assertNotNull( environment.getContainerHostsByPeerId( PEER_ID ) );
    }


    @Test
    public void testGetContainerHostByHostname() throws Exception
    {
        assertNotNull( environment.getContainerHostByHostname( CONTAINER_HOSTNAME ) );
    }


    @Test
    public void testGetContainerHostsByIds() throws Exception
    {
        Set<EnvironmentContainerHost> containers =
                environment.getContainerHostsByIds( Sets.newHashSet( CONTAINER_ID ) );

        assertTrue( containers.contains( environmentContainer ) );
    }


    @Test
    public void testEnvironmentPeer() throws Exception
    {
        environment.addEnvironmentPeer( environmentPeer );

        assertTrue( environment.getEnvironmentPeers().contains( environmentPeer ) );

        assertEquals( environmentPeer, environment.getEnvironmentPeer( PEER_ID ) );

        environment.removeEnvironmentPeer( PEER_ID );

        assertFalse( environment.getEnvironmentPeers().contains( environmentPeer ) );
    }


    @Test
    public void testGetId() throws Exception
    {
        assertNotNull( environment.getId() );
    }


    @Test
    public void testGetContainerHosts() throws Exception
    {
        assertTrue( environment.getContainerHosts().contains( environmentContainer ) );
    }


    @Test
    public void testDestroyContainer() throws Exception
    {
        environment.destroyContainer( environmentContainer, true );

        verify( environmentManager ).destroyContainer( environment.getId(), CONTAINER_ID, true );
    }


    @Test
    public void testGetPeers() throws Exception
    {
        environment.addEnvironmentPeer( environmentPeer );

        assertTrue( environment.getPeers().contains( peer ) );
    }


    @Test
    public void testRemoveContainer() throws Exception
    {
        environment.removeContainer( environmentContainer );

        assertTrue( environment.getContainerHosts().isEmpty() );
    }


    @Test
    public void testAddContainers() throws Exception
    {
        environment.removeContainer( environmentContainer );

        environment.addContainers( Sets.newHashSet( environmentContainer ) );

        assertFalse( environment.getContainerHosts().isEmpty() );
    }


    @Test
    public void testSetStatus() throws Exception
    {
        environment.setStatus( EnvironmentStatus.CANCELLED );

        assertEquals( EnvironmentStatus.CANCELLED, environment.getStatus() );
    }


    @Test
    public void testSubnetCidr() throws Exception
    {
        String CIDR = "192.168.0.1/24";

        environment.setSubnetCidr( CIDR );

        assertEquals( CIDR, environment.getSubnetCidr() );
    }


    @Test
    public void testVni() throws Exception
    {
        Long VNI = 123L;

        environment.setVni( VNI );

        assertEquals( VNI, environment.getVni() );
    }


    @Test
    public void testP2pSubnet() throws Exception
    {
        String P2P_SUBNET = "10.10.10.1";

        environment.setP2PSubnet( P2P_SUBNET );

        assertEquals( P2P_SUBNET, environment.getP2pSubnet() );
    }


    @Test
    public void testGetP2pIps() throws Exception
    {
        environment.addEnvironmentPeer( environmentPeer );

        doReturn( Sets.newHashSet( new RhP2PIpEntity( "ID", "10.10.10.1" ) ) ).when( environmentPeer ).getRhP2pIps();

        assertFalse( environment.getP2pIps().isEmpty() );
    }


    @Test
    public void testIsMember() throws Exception
    {
        environment.addEnvironmentPeer( environmentPeer );

        assertTrue( environment.isMember( peer ) );
    }


    @Test
    public void testP2pKey() throws Exception
    {
        String P2P_KEY = "key";

        environment.setP2pKey( P2P_KEY );

        assertEquals( P2P_KEY, environment.getP2pKey() );
    }


    @Test
    public void testGetP2pHash() throws Exception
    {

        assertNotNull( environment.getP2PHash() );
    }


    @Test
    public void testGetEnvironmentId() throws Exception
    {
        assertNotNull( environment.getEnvironmentId() );
    }


    @Test
    public void testAlertHandler() throws Exception
    {
        EnvironmentAlertHandler environmentAlertHandler =
                new EnvironmentAlertHandlerImpl( "ID", AlertHandlerPriority.NORMAL );

        environment.addAlertHandler( environmentAlertHandler );

        assertTrue( environment.getAlertHandlers().contains( environmentAlertHandler ) );

        environment.removeAlertHandler( environmentAlertHandler );

        assertFalse( environment.getAlertHandlers().contains( environmentAlertHandler ) );
    }
}
