package org.safehaus.subutai.plugin.accumulo.impl.handler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.exception.ClusterConfigurationException;
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
import org.safehaus.subutai.plugin.common.api.NodeOperationType;
import org.safehaus.subutai.plugin.common.api.NodeType;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.api.Zookeeper;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NodeOperationHandlerTest
{
    private NodeOperationHandler nodeOperationHandler;
    private NodeOperationHandler nodeOperationHandler2;
    private NodeOperationHandler nodeOperationHandler3;
    private NodeOperationHandler nodeOperationHandler4;
    private NodeOperationHandler nodeOperationHandler5;
    private NodeOperationHandler nodeOperationHandler6;
    private NodeOperationHandler nodeOperationHandler7;
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
    PluginDAO pluginDAO;
    @Mock
    Hadoop hadoop;
    @Mock
    Zookeeper zookeeper;
    @Mock
    HadoopClusterConfig hadoopClusterConfig;
    @Mock
    ZookeeperClusterConfig zookeeperClusterConfig;

    @Before
    public void setUp() throws Exception
    {
        // mock constructor
        uuid = UUID.randomUUID();
        when(accumuloImpl.getCluster("testClusterName")).thenReturn(accumuloClusterConfig);
        when(accumuloImpl.getTracker()).thenReturn(tracker);
        when(tracker.createTrackerOperation(anyString(), anyString())).thenReturn(trackerOperation);
        when(trackerOperation.getId()).thenReturn(uuid);

        nodeOperationHandler = new NodeOperationHandler(accumuloImpl, hadoop, zookeeper, "testClusterName",
                "testHostName", NodeOperationType.INSTALL, NodeType.ACCUMULO_TRACER);
        nodeOperationHandler2 = new NodeOperationHandler(accumuloImpl, hadoop, zookeeper, "testClusterName",
                "testHostName", NodeOperationType.START, NodeType.ACCUMULO_TRACER);
        nodeOperationHandler3 = new NodeOperationHandler(accumuloImpl, hadoop, zookeeper, "testClusterName",
                "testHostName", NodeOperationType.STOP, NodeType.ACCUMULO_TRACER);
        nodeOperationHandler4 = new NodeOperationHandler(accumuloImpl, hadoop, zookeeper, "testClusterName",
                "testHostName", NodeOperationType.STATUS, NodeType.ACCUMULO_TRACER);
        nodeOperationHandler5 = new NodeOperationHandler(accumuloImpl, hadoop, zookeeper, "testClusterName",
                "testHostName", NodeOperationType.UNINSTALL, NodeType.ACCUMULO_TRACER);
        nodeOperationHandler6 = new NodeOperationHandler(accumuloImpl, hadoop, zookeeper, "testClusterName",
                "testHostName", NodeOperationType.INSTALL, NodeType.ACCUMULO_TABLET_SERVER);
        nodeOperationHandler7 = new NodeOperationHandler(accumuloImpl, hadoop, zookeeper, "testClusterName",
                "testHostName", NodeOperationType.UNINSTALL, NodeType.ACCUMULO_TABLET_SERVER);

        // mock run method
        Set<ContainerHost> mySet = new HashSet<>();
        mySet.add(containerHost);
        when(containerHost.getHostname()).thenReturn("testHostName");
        when(environmentManager.getEnvironmentByUUID(any(UUID.class))).thenReturn(environment);
        when(environment.getContainerHosts()).thenReturn(mySet);

        // mock installProductOnNode
        when(commandResult.hasSucceeded()).thenReturn(true);
        when(hadoop.getCluster(anyString())).thenReturn(hadoopClusterConfig);

        when(accumuloImpl.getPluginDAO()).thenReturn(pluginDAO);

        // mock clusterConfiguration and configureCluster method
        when(hadoopClusterConfig.getEnvironmentId()).thenReturn(uuid);
        when(zookeeper.getCluster(anyString())).thenReturn(zookeeperClusterConfig);

        Set<UUID> myUUID = new HashSet<>();
        myUUID.add(uuid);

        when(accumuloClusterConfig.getAllNodes()).thenReturn(myUUID);
        when(accumuloClusterConfig.getMasterNode()).thenReturn(uuid);
        when(accumuloClusterConfig.getGcNode()).thenReturn(uuid);
        when(accumuloClusterConfig.getMonitor()).thenReturn(uuid);
        when(environment.getContainerHostById(any(UUID.class))).thenReturn(containerHost);
        when(containerHost.execute(any(RequestBuilder.class))).thenReturn(commandResult);
        when(zookeeperClusterConfig.getNodes()).thenReturn(myUUID);


    }

    @Test
    public void testRunWithNodeOperationTypeInstallAndNodeTypeAccumuloTracer() throws Exception
    {
        Set<UUID> myUUID = new HashSet<>();
        when(containerHost.execute(new RequestBuilder(
                Commands.installCommand + Common.PACKAGE_PREFIX + AccumuloClusterConfig.PRODUCT_KEY.toLowerCase())
                .withTimeout(3600))).thenReturn(commandResult);
        when(accumuloImpl.getEnvironmentManager()).thenReturn(environmentManager);
        when(accumuloClusterConfig.getTracers()).thenReturn(myUUID);

        nodeOperationHandler.run();

        // assertions
        assertNotNull(accumuloImpl.getCluster("testClusterName"));
        verify(containerHost).execute(new RequestBuilder(
                Commands.installCommand + Common.PACKAGE_PREFIX + AccumuloClusterConfig.PRODUCT_KEY.toLowerCase())
                .withTimeout(3600));
        assertTrue(commandResult.hasSucceeded());
        assertEquals(pluginDAO, accumuloImpl.getPluginDAO());
        assertEquals(myUUID, accumuloClusterConfig.getTracers());
    }

    @Test
    public void testRunWithNodeOperationTypeInstallAndNodeTypeAccumuloTabletServers() throws Exception
    {
        Set<UUID> myUUID = new HashSet<>();
        when(containerHost.execute(new RequestBuilder(
                Commands.installCommand + Common.PACKAGE_PREFIX + AccumuloClusterConfig.PRODUCT_KEY.toLowerCase())
                .withTimeout(3600))).thenReturn(commandResult);
        when(accumuloImpl.getEnvironmentManager()).thenReturn(environmentManager);
        when(accumuloClusterConfig.getSlaves()).thenReturn(myUUID);

        nodeOperationHandler6.run();

        // assertions
        assertNotNull(accumuloImpl.getCluster("testClusterName"));
        verify(containerHost).execute(new RequestBuilder(
                Commands.installCommand + Common.PACKAGE_PREFIX + AccumuloClusterConfig.PRODUCT_KEY.toLowerCase())
                .withTimeout(3600));
        assertTrue(commandResult.hasSucceeded());
        assertEquals(pluginDAO, accumuloImpl.getPluginDAO());
        assertEquals(myUUID, accumuloClusterConfig.getSlaves());
    }


    @Test
    public void testRunWithNodeOperationTypeStart() throws CommandException
    {
        when(containerHost.execute(new RequestBuilder(Commands.startCommand))).thenReturn(commandResult);
        when(accumuloImpl.getEnvironmentManager()).thenReturn(environmentManager);

        nodeOperationHandler2.run();

        // assertions
        assertNotNull(accumuloImpl.getCluster("testClusterName"));
        verify(containerHost).execute(new RequestBuilder(Commands.startCommand));
    }

    @Test
    public void testRunWithNodeOperationTypeStop() throws CommandException
    {
        when(containerHost.execute(new RequestBuilder(Commands.stopCommand))).thenReturn(commandResult);
        when(accumuloImpl.getEnvironmentManager()).thenReturn(environmentManager);

        nodeOperationHandler3.run();

        // assertions
        assertNotNull(accumuloImpl.getCluster("testClusterName"));
        verify(containerHost).execute(new RequestBuilder(Commands.stopCommand));
    }


    @Test
    public void testRunWithNodeOperationTypeStatus() throws CommandException
    {
        when(containerHost.execute(new RequestBuilder(Commands.statusCommand))).thenReturn(commandResult);
        when(accumuloImpl.getEnvironmentManager()).thenReturn(environmentManager);

        nodeOperationHandler4.run();

        // assertions
        assertNotNull(accumuloImpl.getCluster("testClusterName"));
        verify(containerHost).execute(new RequestBuilder(Commands.statusCommand));
    }

    @Test
    public void testRunWithNodeOperationTypeUninstallAndNodeTypeAccumuloTracer() throws Exception
    {
        Set<UUID> myUUID = new HashSet<>();
        when(accumuloImpl.getEnvironmentManager()).thenReturn(environmentManager);
        when(accumuloClusterConfig.getTracers()).thenReturn(myUUID);

        nodeOperationHandler5.run();

        // assertions
        assertNotNull(accumuloImpl.getCluster("testClusterName"));
        verify(containerHost).execute(new RequestBuilder(
                Commands.uninstallCommand + Common.PACKAGE_PREFIX + AccumuloClusterConfig.PRODUCT_KEY
                        .toLowerCase()));
        assertTrue(commandResult.hasSucceeded());
        assertEquals(pluginDAO, accumuloImpl.getPluginDAO());
        assertEquals(myUUID, accumuloClusterConfig.getTracers());
    }

    @Test
    public void testRunWithNodeOperationTypeUninstallAndNodeTypeAccumuloTabletServers() throws Exception
    {
        Set<UUID> myUUID = new HashSet<>();
        when(accumuloImpl.getEnvironmentManager()).thenReturn(environmentManager);
        when(accumuloClusterConfig.getSlaves()).thenReturn(myUUID);

        nodeOperationHandler7.run();

        // assertions
        assertNotNull(accumuloImpl.getCluster("testClusterName"));
        verify(containerHost).execute(new RequestBuilder(
                Commands.uninstallCommand + Common.PACKAGE_PREFIX + AccumuloClusterConfig.PRODUCT_KEY
                        .toLowerCase()));
        assertTrue(commandResult.hasSucceeded());
        assertEquals(pluginDAO, accumuloImpl.getPluginDAO());
        assertEquals(myUUID, accumuloClusterConfig.getSlaves());
    }


    // exceptions
    @Test
    public void testRunWithNodeOperationTypeStartCommandException() throws CommandException
    {
        when(containerHost.execute(new RequestBuilder(Commands.startCommand))).thenThrow(CommandException.class);
        when(accumuloImpl.getEnvironmentManager()).thenReturn(environmentManager);

        nodeOperationHandler2.run();
    }

    @Test(expected = ClusterConfigurationException.class)
    public void testRunWithNodeOperationTypeInstallClusterConfigurationException() throws Exception
    {
        when(accumuloImpl.getEnvironmentManager()).thenThrow(ClusterConfigurationException.class);

        nodeOperationHandler.run();
    }


}