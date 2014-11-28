package org.safehaus.subutai.plugin.hadoop.impl.handler;


import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.common.PluginDAO;
import org.safehaus.subutai.plugin.common.api.NodeOperationType;
import org.safehaus.subutai.plugin.common.api.NodeType;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hadoop.impl.HadoopImpl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;


public class NodeOperationHandlerTest
{
    NodeOperationHandler nodeOperationHandler;
    HadoopImpl hadoopImpl;
    Tracker tracker;
    ContainerHost containerHost;
    ContainerHost containerHost2;
    Environment environment;
    EnvironmentManager environmentManager;
    HadoopClusterConfig hadoopClusterConfig;
    CommandResult commandResult;
    ClusterOperationHandler clusterOperationHandler;
    TrackerOperation trackerOperation;
    RequestBuilder requestBuilder;


    @Before
    public void setUp()
    {
        tracker = mock( Tracker.class );
        hadoopImpl = mock( HadoopImpl.class );
        when( hadoopImpl.getTracker() ).thenReturn( tracker );
        containerHost = mock( ContainerHost.class );
        containerHost2 = mock( ContainerHost.class );
        environment = mock( Environment.class );
        environmentManager = mock( EnvironmentManager.class );
        hadoopClusterConfig = mock( HadoopClusterConfig.class );
        commandResult = mock( CommandResult.class );
        clusterOperationHandler = mock( ClusterOperationHandler.class );
        trackerOperation = mock( TrackerOperation.class );
        requestBuilder = mock( RequestBuilder.class );

        nodeOperationHandler =
                new NodeOperationHandler( hadoopImpl, "test", "test", NodeOperationType.INSTALL, NodeType.NAMENODE );

        verify( hadoopImpl ).getTracker();
        assertEquals( tracker, hadoopImpl.getTracker() );
    }


    @Test
    public void testRun()
    {
        UUID uuid = new UUID( 50, 50 );
        Set<ContainerHost> mySet = mock( Set.class );
        mySet.add( containerHost );
        mySet.add( containerHost2 );

        when( hadoopImpl.getEnvironmentManager() ).thenReturn( environmentManager );
        when( environmentManager.getEnvironmentByUUID( any( UUID.class ) ) ).thenReturn( environment );
        when( environment.getId() ).thenReturn( uuid );

        when( environment.getContainers() ).thenReturn( mySet );
        Iterator<ContainerHost> iterator = mock( Iterator.class );
        when( mySet.iterator() ).thenReturn( iterator );
        when( iterator.hasNext() ).thenReturn( true ).thenReturn( true ).thenReturn( false );
        when( iterator.next() ).thenReturn( containerHost ).thenReturn( containerHost2 );

        when( containerHost.getHostname() ).thenReturn( "test" );
        when( containerHost2.getHostname() ).thenReturn( "test" );
        when( hadoopImpl.getTracker() ).thenReturn( tracker );
        when( hadoopImpl.getCluster( anyString() ) ).thenReturn( hadoopClusterConfig );

        nodeOperationHandler =
                new NodeOperationHandler( hadoopImpl, "test", "test", NodeOperationType.INSTALL, NodeType.NAMENODE );
        nodeOperationHandler.run();

        verify( hadoopImpl ).getEnvironmentManager();
        verify( hadoopImpl ).getCluster( "test" );

        assertEquals( "test", containerHost.getHostname() );
        assertEquals( uuid, environment.getId() );
    }


    @Test()
    public void testRunCommandWithNodeOperationTypeSTART() throws CommandException
    {
        when( hadoopImpl.getTracker() ).thenReturn( tracker );
        when( tracker.createTrackerOperation( anyString(), anyString() ) ).thenReturn( trackerOperation );
        when( containerHost.execute( any( RequestBuilder.class ) ) ).thenReturn( commandResult );
        when( hadoopImpl.getCluster( anyString() ) ).thenReturn( hadoopClusterConfig );
        when( commandResult.getStdOut() ).thenReturn( "NameNode" );

        nodeOperationHandler =
                new NodeOperationHandler( hadoopImpl, "test", "test", NodeOperationType.INSTALL, NodeType.NAMENODE );

        nodeOperationHandler.runCommand( containerHost, NodeOperationType.START, NodeType.NAMENODE );
        nodeOperationHandler.runCommand( containerHost, NodeOperationType.START, NodeType.JOBTRACKER );
        nodeOperationHandler.runCommand( containerHost, NodeOperationType.START, NodeType.TASKTRACKER );
        nodeOperationHandler.runCommand( containerHost, NodeOperationType.START, NodeType.DATANODE );

        assertEquals( commandResult, containerHost.execute( any( RequestBuilder.class ) ) );
    }


    @Test()
    public void testRunCommandWithNodeOperationTypeSTOP() throws CommandException
    {
        when( hadoopImpl.getTracker() ).thenReturn( tracker );
        when( tracker.createTrackerOperation( anyString(), anyString() ) ).thenReturn( trackerOperation );
        when( containerHost.execute( any( RequestBuilder.class ) ) ).thenReturn( commandResult );
        when( hadoopImpl.getCluster( anyString() ) ).thenReturn( hadoopClusterConfig );
        when( commandResult.getStdOut() ).thenReturn( "NameNode" );

        nodeOperationHandler =
                new NodeOperationHandler( hadoopImpl, "test", "test", NodeOperationType.INSTALL, NodeType.NAMENODE );
        nodeOperationHandler.runCommand( containerHost, NodeOperationType.STOP, NodeType.NAMENODE );
        nodeOperationHandler.runCommand( containerHost, NodeOperationType.STOP, NodeType.JOBTRACKER );
        nodeOperationHandler.runCommand( containerHost, NodeOperationType.STOP, NodeType.TASKTRACKER );
        nodeOperationHandler.runCommand( containerHost, NodeOperationType.STOP, NodeType.DATANODE );

        assertEquals( commandResult, containerHost.execute( any( RequestBuilder.class ) ) );
    }


    @Test()
    public void testRunCommandWithNodeOperationTypeSTATUS() throws CommandException
    {
        when( hadoopImpl.getTracker() ).thenReturn( tracker );
        when( tracker.createTrackerOperation( anyString(), anyString() ) ).thenReturn( trackerOperation );
        when( containerHost.execute( any( RequestBuilder.class ) ) ).thenReturn( commandResult );
        when( hadoopImpl.getCluster( anyString() ) ).thenReturn( hadoopClusterConfig );
        when( commandResult.getStdOut() ).thenReturn( "NameNode" );

        nodeOperationHandler =
                new NodeOperationHandler( hadoopImpl, "test", "test", NodeOperationType.INSTALL, NodeType.NAMENODE );
        nodeOperationHandler.runCommand( containerHost, NodeOperationType.STATUS, NodeType.NAMENODE );
        nodeOperationHandler.runCommand( containerHost, NodeOperationType.STATUS, NodeType.JOBTRACKER );
        nodeOperationHandler.runCommand( containerHost, NodeOperationType.STATUS, NodeType.TASKTRACKER );
        nodeOperationHandler.runCommand( containerHost, NodeOperationType.STATUS, NodeType.DATANODE );
        nodeOperationHandler.runCommand( containerHost, NodeOperationType.STATUS, NodeType.SECONDARY_NAMENODE );

        assertEquals( commandResult, containerHost.execute( any( RequestBuilder.class ) ) );
    }


    @Test
    public void testFindNodeInCluster() throws CommandException
    {
        UUID uuid = new UUID( 50, 50 );
        Set<ContainerHost> mySet = mock( Set.class );
        mySet.add( containerHost );
        mySet.add( containerHost2 );

        when( hadoopImpl.getEnvironmentManager() ).thenReturn( environmentManager );
        when( environmentManager.getEnvironmentByUUID( any( UUID.class ) ) ).thenReturn( environment );
        when( environment.getId() ).thenReturn( uuid );

        when( environment.getContainers() ).thenReturn( mySet );
        Iterator<ContainerHost> iterator = mock( Iterator.class );
        when( mySet.iterator() ).thenReturn( iterator );
        when( iterator.hasNext() ).thenReturn( true ).thenReturn( true ).thenReturn( false );
        when( iterator.next() ).thenReturn( containerHost ).thenReturn( containerHost2 );
        when( containerHost.getHostname() ).thenReturn( "test" );
        when( containerHost2.getHostname() ).thenReturn( "test" );


        when( hadoopImpl.getTracker() ).thenReturn( tracker );
        when( tracker.createTrackerOperation( anyString(), anyString() ) ).thenReturn( trackerOperation );
        when( containerHost.execute( any( RequestBuilder.class ) ) ).thenReturn( commandResult );
        when( hadoopImpl.getCluster( anyString() ) ).thenReturn( hadoopClusterConfig );
        when( hadoopClusterConfig.getNameNode() ).thenReturn( UUID.randomUUID() );


        nodeOperationHandler =
                new NodeOperationHandler( hadoopImpl, "test", "test", NodeOperationType.INSTALL, NodeType.NAMENODE );
        containerHost2 = nodeOperationHandler.findNodeInCluster( "test" );

        assertNotNull( nodeOperationHandler.findNodeInCluster( "tes" ) );
        assertEquals( containerHost, containerHost2 );
    }


    @Test
    public void testExcludeNode() throws CommandException
    {
        UUID uuid = new UUID( 50, 50 );
        Set<ContainerHost> mySet = mock( Set.class );
        mySet.add( containerHost );
        mySet.add( containerHost2 );

        when( environment.getContainers() ).thenReturn( mySet );
        Iterator<ContainerHost> iterator = mock( Iterator.class );
        when( mySet.iterator() ).thenReturn( iterator );
        when( iterator.hasNext() ).thenReturn( true ).thenReturn( true ).thenReturn( false );
        when( iterator.next() ).thenReturn( containerHost ).thenReturn( containerHost2 );
        when( containerHost.getHostname() ).thenReturn( "test" );
        when( containerHost2.getHostname() ).thenReturn( "test" );


        when( hadoopImpl.getTracker() ).thenReturn( tracker );
        when( tracker.createTrackerOperation( anyString(), anyString() ) ).thenReturn( trackerOperation );
        when( containerHost.execute( any( RequestBuilder.class ) ) ).thenReturn( commandResult );
        when( hadoopImpl.getCluster( "test" ) ).thenReturn( hadoopClusterConfig );
        when( hadoopClusterConfig.getNameNode() ).thenReturn( UUID.randomUUID() );

        List<String> mylist = mock( ArrayList.class );
        when( containerHost.getAgent() ).thenReturn( agent );
        when( agent.getListIP() ).thenReturn( mylist );
        when( containerHost.execute( any( RequestBuilder.class ) ) ).thenReturn( commandResult );

        when( hadoopImpl.getEnvironmentManager() ).thenReturn( environmentManager );
        when( environmentManager.getEnvironmentByUUID( any( UUID.class ) ) ).thenReturn( environment );
        when( environment.getId() ).thenReturn( uuid );
        when( environment.getContainerHostByUUID( any( UUID.class ) ) ).thenReturn( containerHost );
        when( containerHost.getEnvironmentId() ).thenReturn( uuid );

        PluginDAO pluginDAO = mock( PluginDAO.class );
        when( hadoopImpl.getPluginDAO() ).thenReturn( pluginDAO );

        nodeOperationHandler =
                new NodeOperationHandler( hadoopImpl, "test", "test", NodeOperationType.INSTALL, NodeType.NAMENODE );
        nodeOperationHandler.excludeNode();
        UUID uuid1 = containerHost.getEnvironmentId();

        assertEquals( uuid, uuid1 );
        assertEquals( "test", containerHost.getHostname() );
        assertEquals( hadoopClusterConfig, hadoopImpl.getCluster( "test" ) );
        verify( hadoopClusterConfig ).getBlockedAgents();
    }


    @Test
    public void testIncludeNode() throws CommandException
    {
        UUID uuid = new UUID( 50, 50 );
        Set<ContainerHost> mySet = mock( Set.class );
        mySet.add( containerHost );
        mySet.add( containerHost2 );

        when( environment.getContainers() ).thenReturn( mySet );
        Iterator<ContainerHost> iterator = mock( Iterator.class );
        when( mySet.iterator() ).thenReturn( iterator );
        when( iterator.hasNext() ).thenReturn( true ).thenReturn( true ).thenReturn( false );
        when( iterator.next() ).thenReturn( containerHost ).thenReturn( containerHost2 );
        when( containerHost.getHostname() ).thenReturn( "test" );
        when( containerHost2.getHostname() ).thenReturn( "test" );


        when( hadoopImpl.getTracker() ).thenReturn( tracker );
        when( tracker.createTrackerOperation( anyString(), anyString() ) ).thenReturn( trackerOperation );
        when( containerHost.execute( any( RequestBuilder.class ) ) ).thenReturn( commandResult );
        when( hadoopImpl.getCluster( "test" ) ).thenReturn( hadoopClusterConfig );
        when( hadoopClusterConfig.getNameNode() ).thenReturn( UUID.randomUUID() );

        List<String> mylist = mock( ArrayList.class );
        when( containerHost.getAgent() ).thenReturn( agent );
        when( agent.getListIP() ).thenReturn( mylist );
        when( containerHost.execute( any( RequestBuilder.class ) ) ).thenReturn( commandResult );

        when( hadoopImpl.getEnvironmentManager() ).thenReturn( environmentManager );
        when( environmentManager.getEnvironmentByUUID( any( UUID.class ) ) ).thenReturn( environment );
        when( environment.getId() ).thenReturn( uuid );
        when( environment.getContainerHostByUUID( any( UUID.class ) ) ).thenReturn( containerHost );
        when( containerHost.getEnvironmentId() ).thenReturn( uuid );

        PluginDAO pluginDAO = mock( PluginDAO.class );
        when( hadoopImpl.getPluginDAO() ).thenReturn( pluginDAO );

        nodeOperationHandler =
                new NodeOperationHandler( hadoopImpl, "test", "test", NodeOperationType.INSTALL, NodeType.NAMENODE );
        nodeOperationHandler.includeNode();
        UUID uuid1 = containerHost.getEnvironmentId();

        assertEquals( uuid, uuid1 );
        assertEquals( "test", containerHost.getHostname() );
        assertEquals( hadoopClusterConfig, hadoopImpl.getCluster( "test" ) );
        verify( hadoopClusterConfig ).getBlockedAgents();
    }
}