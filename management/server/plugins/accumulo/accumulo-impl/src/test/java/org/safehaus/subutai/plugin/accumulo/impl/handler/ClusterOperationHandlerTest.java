package org.safehaus.subutai.plugin.accumulo.impl.handler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.accumulo.api.AccumuloClusterConfig;
import org.safehaus.subutai.plugin.accumulo.impl.AccumuloImpl;
import org.safehaus.subutai.plugin.accumulo.impl.Commands;
import org.safehaus.subutai.plugin.common.PluginDAO;
import org.safehaus.subutai.plugin.common.api.ClusterOperationType;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.api.Zookeeper;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;

import java.util.*;

import static junit.framework.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ClusterOperationHandlerTest
{
    private ClusterOperationHandler clusterOperationHandler;
    private ClusterOperationHandler clusterOperationHandler2;
    private ClusterOperationHandler clusterOperationHandler3;
    private ClusterOperationHandler clusterOperationHandler4;
    private ClusterOperationHandler clusterOperationHandler5;
    private UUID uuid;
    @Mock
    AccumuloImpl accumuloImpl;
    @Mock
    AccumuloClusterConfig accumuloClusterConfig;
    @Mock
    Tracker tracker;
    @Mock
    EnvironmentManager environmentManager;
    @Mock
    TrackerOperation trackerOperation;
    @Mock
    Environment environment;
    @Mock
    ContainerHost containerHost;
    @Mock
    CommandResult commandResult;
    @Mock
    ClusterSetupStrategy clusterSetupStrategy;
    @Mock
    HadoopClusterConfig hadoopClusterConfig;
    @Mock
    ZookeeperClusterConfig zookeeperClusterConfig;
    @Mock
    Hadoop hadoop;
    @Mock
    Zookeeper zookeeper;
    @Mock
    PluginDAO pluginDAO;

    @Before
    public void setUp() throws CommandException
    {
        // mock constructor
        uuid = UUID.randomUUID();
        when(accumuloImpl.getCluster(anyString())).thenReturn(accumuloClusterConfig);
        when(accumuloImpl.getTracker()).thenReturn(tracker);
        when(tracker.createTrackerOperation(anyString(), anyString())).thenReturn(trackerOperation);
        when(trackerOperation.getId()).thenReturn(uuid);

        // mock runOperationOnContainers method
        when(accumuloImpl.getEnvironmentManager()).thenReturn(environmentManager);
        when(environmentManager.getEnvironmentByUUID(any(UUID.class))).thenReturn(environment);
        when(environment.getContainerHostById(any(UUID.class))).thenReturn(containerHost);
        when(containerHost.execute(any(RequestBuilder.class))).thenReturn(commandResult);

        clusterOperationHandler = new ClusterOperationHandler(accumuloImpl, accumuloClusterConfig,
                hadoopClusterConfig, zookeeperClusterConfig, ClusterOperationType.INSTALL);
        clusterOperationHandler2 = new ClusterOperationHandler(accumuloImpl, accumuloClusterConfig,
                hadoopClusterConfig, zookeeperClusterConfig, ClusterOperationType.UNINSTALL);
        clusterOperationHandler3 = new ClusterOperationHandler(accumuloImpl, accumuloClusterConfig,
                hadoopClusterConfig, zookeeperClusterConfig, ClusterOperationType.START_ALL);
        clusterOperationHandler4 = new ClusterOperationHandler(accumuloImpl, accumuloClusterConfig,
                hadoopClusterConfig, zookeeperClusterConfig, ClusterOperationType.STOP_ALL);
        clusterOperationHandler5 = new ClusterOperationHandler(accumuloImpl, accumuloClusterConfig,
                hadoopClusterConfig, zookeeperClusterConfig, ClusterOperationType.STATUS_ALL);

    }

    @Test
    public void testRunWithClusterOperationTypeInstall() throws CommandException
    {
        // mock setup method
        when(accumuloClusterConfig.getMasterNode()).thenReturn(UUID.randomUUID());
        when(accumuloClusterConfig.getGcNode()).thenReturn(UUID.randomUUID());
        when(accumuloClusterConfig.getMonitor()).thenReturn(UUID.randomUUID());
        when(accumuloClusterConfig.getClusterName()).thenReturn("test-cluster");
        when(accumuloClusterConfig.getInstanceName()).thenReturn("test-instance");
        when(accumuloClusterConfig.getPassword()).thenReturn("test-password");
        when(accumuloImpl.getHadoopManager()).thenReturn(hadoop);

        List<UUID> myList = new ArrayList<>();
        myList.add(uuid);

        Set<UUID> myUUID = new HashSet<>();
        myUUID.add(uuid);
        when(accumuloClusterConfig.getTracers()).thenReturn(myUUID);
        when(accumuloClusterConfig.getSlaves()).thenReturn(myUUID);

        when(hadoop.getCluster(anyString())).thenReturn(hadoopClusterConfig);
        when(accumuloImpl.getZkManager()).thenReturn(zookeeper);
        when(zookeeper.getCluster(anyString())).thenReturn(zookeeperClusterConfig);
        when(zookeeperClusterConfig.getNodes()).thenReturn(myUUID);
        when(zookeeperClusterConfig.getClusterName()).thenReturn("testClusterName");
        when(containerHost.getHostname()).thenReturn("testHostName");
        when(zookeeper.startNode(anyString(), anyString())).thenReturn(uuid);

        when(hadoopClusterConfig.getAllNodes()).thenReturn(myList);
        when(accumuloClusterConfig.getAllNodes()).thenReturn(myUUID);
        when(commandResult.hasSucceeded()).thenReturn(true);
        when(commandResult.getStdOut()).thenReturn("Hadoop");


        // mock clusterConfiguration
        when(accumuloImpl.getEnvironmentManager()).thenReturn(environmentManager);
        when(zookeeperClusterConfig.getEnvironmentId()).thenReturn(uuid);
        when(environmentManager.getEnvironmentByUUID(uuid)).thenReturn(environment);
        when(accumuloImpl.getPluginDAO()).thenReturn(pluginDAO);
        when(accumuloImpl.getCluster(anyString())).thenReturn(null);

        clusterOperationHandler.run();

        // assertions
        assertEquals(environment, accumuloImpl.getEnvironmentManager().getEnvironmentByUUID(any(UUID.class)));
        verify(trackerOperation).addLogDone("Accumulo cluster data saved into database");
    }

    @Test
    public void testRunWithClusterOperationTypeUninstall() throws CommandException
    {
        Set<UUID> mySet = new HashSet<>();
        mySet.add(uuid);
        when(accumuloImpl.getCluster(anyString())).thenReturn(accumuloClusterConfig);
        when(accumuloClusterConfig.getAllNodes()).thenReturn(mySet);
        when(commandResult.hasSucceeded()).thenReturn(true);
        when(accumuloImpl.getPluginDAO()).thenReturn(pluginDAO);

        clusterOperationHandler2.run();

        // assertions
        assertNotNull(accumuloImpl.getCluster(anyString()));
        verify(containerHost).execute(new RequestBuilder(
                Commands.uninstallCommand + Common.PACKAGE_PREFIX + AccumuloClusterConfig.PRODUCT_KEY
                        .toLowerCase()));
        assertTrue(commandResult.hasSucceeded());
        verify(trackerOperation).addLog(
                AccumuloClusterConfig.PRODUCT_KEY + " is uninstalled from node " + containerHost.getHostname()
                        + " successfully.");
        verify(trackerOperation).addLog(AccumuloClusterConfig.PRODUCT_KEY + " cluster info removed from HDFS.");
        verify(accumuloImpl).getPluginDAO();
    }

    @Test
    public void testRunWithClusterOperationTypeStartAll() throws CommandException
    {

        when(commandResult.hasSucceeded()).thenReturn(true);
        clusterOperationHandler3.run();

        // assertions
        assertEquals(environment, accumuloImpl.getEnvironmentManager().getEnvironmentByUUID(any(UUID.class)));
        assertTrue(commandResult.hasSucceeded());
    }

    @Test
    public void testRunWithClusterOperationTypeStopAll()
    {

        when(commandResult.hasSucceeded()).thenReturn(true);
        clusterOperationHandler4.run();

        // assertions
        assertEquals(environment, accumuloImpl.getEnvironmentManager().getEnvironmentByUUID(any(UUID.class)));
        assertTrue(commandResult.hasSucceeded());
    }

    @Test
    public void testRunWithClusterOperationTypeStatusAll()
    {
        Set<ContainerHost> mySet = new HashSet<>();
        mySet.add(containerHost);
        when(environment.getContainerHosts()).thenReturn(mySet);
        when(commandResult.hasSucceeded()).thenReturn(true);
        clusterOperationHandler5.run();

        // assertions
        assertEquals(environment, accumuloImpl.getEnvironmentManager().getEnvironmentByUUID(any(UUID.class)));
        assertTrue(commandResult.hasSucceeded());
    }

    @Test
    public void testRunShouldThowsClusterSetupException() throws CommandException
    {
        // mock setup method
        when(accumuloClusterConfig.getMasterNode()).thenReturn(UUID.randomUUID());
        when(accumuloClusterConfig.getGcNode()).thenReturn(UUID.randomUUID());
        when(accumuloClusterConfig.getMonitor()).thenReturn(UUID.randomUUID());
        when(accumuloClusterConfig.getClusterName()).thenReturn("test-cluster");
        when(accumuloClusterConfig.getInstanceName()).thenReturn("test-instance");
        when(accumuloClusterConfig.getPassword()).thenReturn("test-password");
        when(accumuloImpl.getHadoopManager()).thenReturn(hadoop);

        List<UUID> myList = new ArrayList<>();
        myList.add(uuid);

        Set<UUID> myUUID = new HashSet<>();
        myUUID.add(uuid);
        when(accumuloClusterConfig.getTracers()).thenReturn(myUUID);
        when(accumuloClusterConfig.getSlaves()).thenReturn(myUUID);

        when(hadoop.getCluster(anyString())).thenThrow(ClusterSetupException.class);
        when(accumuloImpl.getCluster(anyString())).thenReturn(null);

        clusterOperationHandler.run();
    }

    @Test
    public void testRunWhenCommandResultHasNotSucceeded() throws CommandException
    {
        Set<UUID> mySet = new HashSet<>();
        mySet.add(uuid);
        when(accumuloImpl.getCluster(anyString())).thenReturn(accumuloClusterConfig);
        when(accumuloClusterConfig.getAllNodes()).thenReturn(mySet);
        when(commandResult.hasSucceeded()).thenReturn(false);
        when(accumuloImpl.getPluginDAO()).thenReturn(pluginDAO);

        clusterOperationHandler2.run();

        // assertions
        assertNotNull(accumuloImpl.getCluster(anyString()));
        verify(containerHost).execute(new RequestBuilder(
                Commands.uninstallCommand + Common.PACKAGE_PREFIX + AccumuloClusterConfig.PRODUCT_KEY
                        .toLowerCase()));
        assertFalse(commandResult.hasSucceeded());
        verify(trackerOperation).addLogFailed(
                "Could not uninstall " + AccumuloClusterConfig.PRODUCT_KEY + " from node " + containerHost
                        .getHostname());

    }

    @Test
    public void testRunShouldThrowsCommandException() throws CommandException
    {
        Set<UUID> mySet = new HashSet<>();
        mySet.add(uuid);
        when(accumuloImpl.getCluster(anyString())).thenReturn(accumuloClusterConfig);
        when(accumuloClusterConfig.getAllNodes()).thenReturn(mySet);
        when(containerHost.execute(new RequestBuilder(
                Commands.uninstallCommand + Common.PACKAGE_PREFIX + AccumuloClusterConfig.PRODUCT_KEY
                        .toLowerCase() ))).thenThrow(CommandException.class);
        when(commandResult.hasSucceeded()).thenReturn(false);
        when(accumuloImpl.getPluginDAO()).thenReturn(pluginDAO);

        clusterOperationHandler2.run();

        // assertions
        assertNotNull(accumuloImpl.getCluster(anyString()));
    }

/*
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    HadoopClusterConfig hadoopClusterConfig;
    AccumuloClusterConfig accumuloClusterConfig;
    AccumuloImpl accumuloMock;
    ZookeeperClusterConfig zookeeperClusterConfig;


    @Before
    public void setUp()
    {
        hadoopClusterConfig = mock( HadoopClusterConfig.class );
        when( hadoopClusterConfig.getEnvironmentId() ).thenReturn( UUID.randomUUID() );

        Environment environmentMock = mock( Environment.class );
        when( environmentMock.getId() ).thenReturn( UUID.randomUUID() );

        accumuloClusterConfig = mock( AccumuloClusterConfig.class );
        when( accumuloClusterConfig.getMasterNode() ).thenReturn( UUID.randomUUID() );
        when( accumuloClusterConfig.getGcNode() ).thenReturn( UUID.randomUUID() );
        when( accumuloClusterConfig.getMonitor() ).thenReturn( UUID.randomUUID() );
        when( accumuloClusterConfig.getClusterName() ).thenReturn( "test-cluster" );
        when( accumuloClusterConfig.getInstanceName() ).thenReturn( "test-instance" );
        when( accumuloClusterConfig.getPassword() ).thenReturn( "test-password" );

        accumuloMock = mock( AccumuloImpl.class );
        when( accumuloMock.getTracker() ).thenReturn( new TrackerMock() );
        when( accumuloMock.getEnvironmentManager() ).thenReturn( mock( EnvironmentManager.class ) );
        when( accumuloMock.getEnvironmentManager().getEnvironmentByUUID( hadoopClusterConfig.getEnvironmentId() ) )
                .thenReturn( environmentMock );

        Set<UUID> set = new HashSet<>();
        set.add( UUID.randomUUID() );
        set.add( UUID.randomUUID() );
        when( accumuloClusterConfig.getTracers() ).thenReturn( set );
        when( accumuloClusterConfig.getSlaves() ).thenReturn( set );

        zookeeperClusterConfig = mock( ZookeeperClusterConfig.class );
    }


    @Test
    public void testSetupClusterWithExistingClusterName()
    {
        when( accumuloMock.getCluster( anyString() ) ).thenReturn( accumuloClusterConfig );
        AbstractOperationHandler operationHandler =
                new ClusterOperationHandler( accumuloMock, accumuloClusterConfig, hadoopClusterConfig,
                        zookeeperClusterConfig, ClusterOperationType.INSTALL );

//        operationHandler.run();

//        assertTrue( operationHandler.getTrackerOperation().getLog().contains( "already a cluster" ) );
//        assertEquals( operationHandler.getTrackerOperation().getState(), OperationState.FAILED );
    }


    @Test
    public void testDestroyCluster()
    {
        AbstractOperationHandler operationHandler =
                new ClusterOperationHandler( accumuloMock, accumuloClusterConfig, hadoopClusterConfig,
                        zookeeperClusterConfig, ClusterOperationType.UNINSTALL );
//        operationHandler.run();
//        assertTrue( operationHandler.getTrackerOperation().getLog().contains( "not exist" ) );
//        assertEquals( operationHandler.getTrackerOperation().getState(), OperationState.FAILED );
    }
*/
}
