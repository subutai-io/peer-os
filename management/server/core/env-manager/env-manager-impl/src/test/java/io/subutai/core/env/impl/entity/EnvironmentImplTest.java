package io.subutai.core.env.impl.entity;


import java.io.PrintStream;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import io.subutai.common.environment.ContainerHostNotFoundException;
import io.subutai.common.environment.EnvironmentModificationException;
import io.subutai.common.environment.EnvironmentNotFoundException;
import io.subutai.common.environment.EnvironmentStatus;
import io.subutai.common.environment.Topology;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.Peer;
import io.subutai.core.env.api.EnvironmentManager;
import io.subutai.core.env.impl.TestUtil;
import io.subutai.core.env.impl.dao.EnvironmentDataService;

import org.apache.commons.net.util.SubnetUtils;

import com.google.common.collect.Sets;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class EnvironmentImplTest
{
    @Mock
    EnvironmentManager environmentManager;

    @Mock
    EnvironmentDataService environmentDataService;
    @Mock
    EnvironmentNotFoundException environmentNotFoundException;
    @Mock
    EnvironmentContainerImpl environmentContainer;
    @Mock
    Topology topology;
    @Mock
    Peer peer;
    @Mock
    ContainerHostNotFoundException containerHostNotFoundException;

    EnvironmentImpl environment;


    @Before
    public void setUp() throws Exception
    {
        environment = new EnvironmentImpl( TestUtil.ENV_NAME, TestUtil.SUBNET, TestUtil.SSH_KEY, TestUtil.USER_ID );
        environment.setEnvironmentManager( environmentManager );
        environment.setDataService( environmentDataService );
        when( environmentContainer.getId() ).thenReturn( TestUtil.CONTAINER_ID );
        when( environmentContainer.getHostname() ).thenReturn( TestUtil.HOSTNAME );
        when( environmentContainer.getPeer() ).thenReturn( peer );
    }


    @Test
    public void testGetSshKey() throws Exception
    {
        assertEquals( TestUtil.SSH_KEY, environment.getSshKey() );
    }


    @Test( expected = EnvironmentModificationException.class )
    public void testSetSshKey() throws Exception
    {
        environment.setSshKey( TestUtil.SSH_KEY, false );

        verify( environmentManager ).setSshKey( any( String.class ), eq( TestUtil.SSH_KEY ), anyBoolean() );

        doThrow( environmentNotFoundException ).when( environmentManager )
                                               .setSshKey( any( String.class ), eq( TestUtil.SSH_KEY ), anyBoolean() );

        environment.setSshKey( TestUtil.SSH_KEY, false );
    }


    @Test
    public void testSaveSshKey() throws Exception
    {
        environment.saveSshKey( TestUtil.SSH_KEY );

        verify( environmentDataService ).update( environment );
    }


    @Test
    public void testGetCreationTimestamp() throws Exception
    {
        assertTrue( environment.getCreationTimestamp() > 0 );
    }


    @Test
    public void testGetName() throws Exception
    {
        assertEquals( TestUtil.ENV_NAME, environment.getName() );
    }


    @Test
    public void testGetUserId() throws Exception
    {

        assertEquals( TestUtil.USER_ID, environment.getUserId() );
    }


    @Test
    public void testGetStatus() throws Exception
    {
        assertEquals( EnvironmentStatus.EMPTY, environment.getStatus() );
    }


    @Test
    public void testAddContainers() throws Exception
    {

        environment.addContainers( Sets.newHashSet( environmentContainer ) );

        verify( environmentDataService ).update( environment );
        verify( environmentContainer ).setEnvironment( environment );
    }


    @Test
    public void testGetContainerHostById() throws Exception
    {
        try
        {
            environment.getContainerHostById( TestUtil.CONTAINER_ID );
            fail( "Expected ContainerHostNotFoundException" );
        }
        catch ( ContainerHostNotFoundException e )
        {
        }

        environment.addContainers( Sets.newHashSet( environmentContainer ) );

        ContainerHost containerHost = environment.getContainerHostById( TestUtil.CONTAINER_ID );

        assertEquals( environmentContainer, containerHost );
    }


    @Test
    public void testGetContainerHostByHostname() throws Exception
    {
        try
        {
            environment.getContainerHostByHostname( TestUtil.HOSTNAME );
            fail( "Expected ContainerHostNotFoundException" );
        }
        catch ( ContainerHostNotFoundException e )
        {
        }

        environment.addContainers( Sets.newHashSet( environmentContainer ) );

        ContainerHost containerHost = environment.getContainerHostByHostname( TestUtil.HOSTNAME );

        assertEquals( environmentContainer, containerHost );
    }


    @Test
    public void testGetContainerHostsByIds() throws Exception
    {
        environment.addContainers( Sets.newHashSet( environmentContainer ) );

        Set<ContainerHost> hosts = environment.getContainerHostsByIds( Sets.newHashSet( TestUtil.CONTAINER_ID ) );

        assertTrue( hosts.contains( environmentContainer ) );
    }


    @Test
    public void testGetId() throws Exception
    {
        assertNotNull( environment.getId() );
    }


    @Test
    public void testGetContainerHosts() throws Exception
    {
        environment.addContainers( Sets.newHashSet( environmentContainer ) );

        Set<ContainerHost> hosts = environment.getContainerHosts();

        assertTrue( hosts.contains( environmentContainer ) );
    }


    @Test
    public void testDestroyContainer() throws Exception
    {
        environment.destroyContainer( environmentContainer, false );

        verify( environmentManager ).destroyContainer( environment.getId(), environmentContainer.getId(), false, false );
    }


    @Test( expected = EnvironmentModificationException.class )
    public void testGrowEnvironment() throws Exception
    {

        environment.growEnvironment( topology, false );

        verify( environmentManager ).growEnvironment( environment.getId(), topology, false );

        doThrow( environmentNotFoundException ).when( environmentManager )
                                               .growEnvironment( environment.getId(), topology, false );

        environment.growEnvironment( topology, false );
    }


    @Test
    public void testGetPeers() throws Exception
    {
        environment.addContainers( Sets.newHashSet( environmentContainer ) );

        Set<Peer> peers = environment.getPeers();

        assertTrue( peers.contains( peer ) );
    }


    @Test
    public void testRemoveContainer() throws Exception
    {

        environment.addContainers( Sets.newHashSet( environmentContainer ) );

        environment.removeContainer( TestUtil.CONTAINER_ID );

        verify( environmentDataService, times( 2 ) ).update( environment );

        EnvironmentImpl environmentSpy = spy( environment );

        doThrow( containerHostNotFoundException ).when( environmentSpy ).getContainerHostById( any( String.class ) );

        environmentSpy.removeContainer( TestUtil.CONTAINER_ID );

        verify( containerHostNotFoundException ).printStackTrace( any( PrintStream.class ) );
    }


    @Test
    public void testSetStatus() throws Exception
    {
        environment.setStatus( EnvironmentStatus.HEALTHY );

        verify( environmentDataService ).update( environment );
    }


    @Test
    public void testEquals() throws Exception
    {
        EnvironmentImpl environment2 = new EnvironmentImpl();
        environment2.setEnvironmentId( environment.getId() );

        assertEquals( environment2, environment );
    }


    @Test
    public void testHashCode() throws Exception
    {
        EnvironmentImpl environment2 = new EnvironmentImpl();
        environment2.setEnvironmentId( environment.getId() );

        assertEquals( environment2.hashCode(), environment.hashCode() );
    }


    @Test
    public void testGetSubnetSidr() throws Exception
    {
        String subnetCidr = environment.getSubnetCidr();

        SubnetUtils cidr = new SubnetUtils( TestUtil.SUBNET );

        assertEquals( cidr.getInfo().getCidrSignature(), subnetCidr );
    }


    @Test
    public void testGetNSetVni() throws Exception
    {
        environment.setVni( TestUtil.VNI );

        verify( environmentDataService ).update( environment );

        assertEquals( TestUtil.VNI, environment.getVni() );
    }


    @Test
    public void testGetNSetLastUsedIpIdx() throws Exception
    {
        environment.setLastUsedIpIndex( TestUtil.LAST_USED_IP_IDX );

        verify( environmentDataService ).update( environment );

        assertEquals( TestUtil.LAST_USED_IP_IDX, environment.getLastUsedIpIndex() );
    }


    @Test
    public void testToString() throws Exception
    {
        String toString = environment.toString();

        assertThat( toString, containsString( TestUtil.ENV_NAME ) );
    }
}
