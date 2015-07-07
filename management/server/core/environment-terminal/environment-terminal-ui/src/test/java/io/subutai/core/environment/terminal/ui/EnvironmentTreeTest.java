package io.subutai.core.environment.terminal.ui;


import java.util.Date;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.environment.Environment;
import org.safehaus.subutai.common.environment.EnvironmentNotFoundException;
import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.common.peer.Peer;
import io.subutai.core.env.api.EnvironmentManager;
import io.subutai.core.environment.terminal.ui.EnvironmentTree;

import com.google.common.collect.Sets;
import com.vaadin.data.util.HierarchicalContainer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class EnvironmentTreeTest
{
    private static final UUID PEER_ID = UUID.randomUUID();
    private static UUID CONTAINER_A_ID = UUID.randomUUID();
    private static UUID CONTAINER_B_ID = UUID.randomUUID();
    @Mock
    EnvironmentManager environmentManager;

    @Mock
    private Environment environmentA;
    @Mock
    private ContainerHost containerHostB;
    @Mock
    private ContainerHost containerHostA;

    @Mock
    Peer peer;

    EnvironmentTree environmentTree;


    @Before
    public void setUp()
    {

    }


    @Test
    public void testGetSelectedContainers()
    {
        when( containerHostA.getHostname() ).thenReturn( "hostA" );
        when( containerHostB.getHostname() ).thenReturn( "hostB" );

        when( environmentManager.getEnvironments() ).thenReturn( Sets.newHashSet( environmentA ) );
        when( environmentA.getContainerHosts() ).thenReturn( Sets.newHashSet( containerHostA, containerHostB ) );


        environmentTree = new EnvironmentTree( environmentManager, new Date() );

        environmentTree.environment = environmentA;


        Set<ContainerHost> hostSet = environmentTree.getSelectedContainers();


        assertNotNull( hostSet );
        assertEquals( 0, hostSet.size() );

        environmentTree.selectedContainers = Sets.newHashSet( containerHostA );

        hostSet = environmentTree.getSelectedContainers();


        assertNotNull( hostSet );
        assertEquals( 1, hostSet.size() );
    }


    @Test
    public void testGetNodeContainer()
    {
        when( containerHostA.getHostname() ).thenReturn( "hostA" );
        when( containerHostB.getHostname() ).thenReturn( "hostB" );

        when( environmentManager.getEnvironments() ).thenReturn( Sets.newHashSet( environmentA ) );
        when( environmentA.getContainerHosts() ).thenReturn( Sets.newHashSet( containerHostA, containerHostB ) );


        environmentTree = new EnvironmentTree( environmentManager, new Date() );

        environmentTree.environment = environmentA;


        HierarchicalContainer container = environmentTree.getNodeContainer();

        assertNotNull( container );
        assertEquals( 0, container.size() );
    }


    @Test
    public void testRefresh() throws EnvironmentNotFoundException
    {
        when( containerHostA.getHostname() ).thenReturn( "hostA" );
        when( containerHostA.getPeer() ).thenReturn( peer );
        when( containerHostA.getPeerId() ).thenReturn( PEER_ID.toString() );

        when( containerHostB.getHostname() ).thenReturn( "hostB" );
        when( containerHostB.getPeer() ).thenReturn( peer );
        when( containerHostB.getPeerId() ).thenReturn( PEER_ID.toString() );

        when( peer.getName() ).thenReturn( "peer name" );
        when( peer.getId() ).thenReturn( PEER_ID );


        when( environmentManager.getEnvironments() ).thenReturn( Sets.newHashSet( environmentA ) );
        when( environmentManager.findEnvironment( PEER_ID ) ).thenReturn( environmentA );

        Set<ContainerHost> containers = Sets.newHashSet( containerHostA );
        when( environmentA.getContainerHosts() ).thenReturn( containers );
        when( environmentA.getId() ).thenReturn( PEER_ID );
        when( environmentA.getName() ).thenReturn( "environmentA" );
        when( environmentA.getPeers() ).thenReturn( Sets.newHashSet( peer ) );

        environmentTree = new EnvironmentTree( environmentManager, new Date() );

        environmentTree.environment = environmentA;

        environmentTree.refreshContainers();

        assertEquals( 2, environmentTree.container.size() );
    }


    @Test
    public void testFilterContainerHostsByTag() throws EnvironmentNotFoundException
    {
        when( peer.getName() ).thenReturn( "peer name" );
        when( peer.getId() ).thenReturn( PEER_ID );

        when( containerHostA.getHostname() ).thenReturn( "hostA" );
        when( containerHostA.getId() ).thenReturn( CONTAINER_A_ID );
        when( containerHostA.getPeer() ).thenReturn( peer );
        when( containerHostA.getPeerId() ).thenReturn( PEER_ID.toString() );
        when( containerHostA.getTags() ).thenReturn( Sets.newHashSet( "tagA", "commonTag" ) );

        when( containerHostB.getHostname() ).thenReturn( "hostB" );
        when( containerHostB.getId() ).thenReturn( CONTAINER_B_ID );
        when( containerHostB.getPeer() ).thenReturn( peer );
        when( containerHostB.getPeerId() ).thenReturn( PEER_ID.toString() );
        when( containerHostB.getTags() ).thenReturn( Sets.newHashSet( "tagB", "commonTag" ) );


        when( environmentManager.getEnvironments() ).thenReturn( Sets.newHashSet( environmentA ) );
        when( environmentManager.findEnvironment( PEER_ID ) ).thenReturn( environmentA );

        Set<ContainerHost> containers = Sets.newHashSet( containerHostA, containerHostB );

        when( environmentA.getContainerHosts() ).thenReturn( containers );
        when( environmentA.getId() ).thenReturn( PEER_ID );
        when( environmentA.getName() ).thenReturn( "environmentA" );
        when( environmentA.getPeers() ).thenReturn( Sets.newHashSet( peer ) );

        environmentTree = new EnvironmentTree( environmentManager, new Date() );

        environmentTree.environment = environmentA;

        environmentTree.selectedContainers = containers;

        assertEquals( 2, environmentTree.getSelectedContainers().size() );


        environmentTree.refreshContainers();


        assertEquals( 3, environmentTree.tree.getContainerDataSource().getItemIds().size() );

        environmentTree.filterContainerHostsByTag( "tagA" );

        environmentTree.refreshContainers();

        assertEquals( 2, environmentTree.tree.getContainerDataSource().getItemIds().size() );


        environmentTree.filterContainerHostsByTag( "tagB" );

        environmentTree.refreshContainers();

        assertEquals( 2, environmentTree.tree.getContainerDataSource().getItemIds().size() );

        environmentTree.filterContainerHostsByTag( "commonTag" );

        environmentTree.refreshContainers();

        assertEquals( 3, environmentTree.tree.getContainerDataSource().getItemIds().size() );

        // clearing filter
        environmentTree.filterContainerHostsByTag( "" );

        environmentTree.refreshContainers();

        assertEquals( 3, environmentTree.tree.getContainerDataSource().getItemIds().size() );
    }
}
