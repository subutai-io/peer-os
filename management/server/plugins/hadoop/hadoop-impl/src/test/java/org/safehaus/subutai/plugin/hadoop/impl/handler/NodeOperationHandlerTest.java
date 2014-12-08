package org.safehaus.subutai.plugin.hadoop.impl.handler;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
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

import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class NodeOperationHandlerTest
{
    private NodeOperationHandler nodeOperationHandler;
    private UUID uuid;
    @Mock
    HadoopImpl hadoopImpl;
    @Mock
    Tracker tracker;
    @Mock
    ContainerHost containerHost;
    @Mock
    ContainerHost containerHost2;
    @Mock
    Environment environment;
    @Mock
    EnvironmentManager environmentManager;
    @Mock
    HadoopClusterConfig hadoopClusterConfig;
    @Mock
    CommandResult commandResult;
    @Mock
    ClusterOperationHandler clusterOperationHandler;
    @Mock
    TrackerOperation trackerOperation;
    @Mock
    RequestBuilder requestBuilder;
    @Mock
    PluginDAO pluginDAO;


    @Before
    public void setUp() throws CommandException
    {
        when(commandResult.getStdOut()).thenReturn("NameNode");
        Set<ContainerHost> mySet = mock(Set.class);
        mySet.add(containerHost);
        mySet.add(containerHost2);
        when(hadoopImpl.getEnvironmentManager()).thenReturn(environmentManager);
        when(environmentManager.getEnvironmentByUUID(any(UUID.class))).thenReturn(environment);
        when(environment.getId()).thenReturn(uuid);
        when(environment.getContainerHosts()).thenReturn(mySet);
        Iterator<ContainerHost> iterator = mock(Iterator.class);
        when(mySet.iterator()).thenReturn(iterator);
        when(iterator.hasNext()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(iterator.next()).thenReturn(containerHost).thenReturn(containerHost2);
        when(containerHost.getHostname()).thenReturn("test");
        when(containerHost2.getHostname()).thenReturn("test");
        when(hadoopImpl.getTracker()).thenReturn(tracker);
        when(tracker.createTrackerOperation(anyString(), anyString())).thenReturn(trackerOperation);
        when(containerHost.execute(any(RequestBuilder.class))).thenReturn(commandResult);
        when(hadoopImpl.getCluster(anyString())).thenReturn(hadoopClusterConfig);
        when(hadoopClusterConfig.getNameNode()).thenReturn(UUID.randomUUID());
        when(environment.getContainerHostById(any(UUID.class))).thenReturn(containerHost);
        when(hadoopImpl.getPluginDAO()).thenReturn(pluginDAO);

        nodeOperationHandler =
                new NodeOperationHandler(hadoopImpl, "test", "test", NodeOperationType.INSTALL, NodeType.NAMENODE);
        uuid = UUID.randomUUID();

        // assertions
        verify(hadoopImpl).getTracker();
        assertEquals(tracker, hadoopImpl.getTracker());
    }


    @Test
    public void testRun()
    {
        Set<ContainerHost> mySet = mock(Set.class);
        mySet.add(containerHost);
        mySet.add(containerHost2);

        when(hadoopImpl.getEnvironmentManager()).thenReturn(environmentManager);
        when(environmentManager.getEnvironmentByUUID(any(UUID.class))).thenReturn(environment);
        when(environment.getId()).thenReturn(uuid);

        when(environment.getContainerHosts()).thenReturn(mySet);
        Iterator<ContainerHost> iterator = mock(Iterator.class);
        when(mySet.iterator()).thenReturn(iterator);
        when(iterator.hasNext()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(iterator.next()).thenReturn(containerHost).thenReturn(containerHost2);

        when(containerHost.getHostname()).thenReturn("test");
        when(containerHost2.getHostname()).thenReturn("test");
        when(hadoopImpl.getTracker()).thenReturn(tracker);
        when(hadoopImpl.getCluster(anyString())).thenReturn(hadoopClusterConfig);

        nodeOperationHandler =
                new NodeOperationHandler(hadoopImpl, "test", "test", NodeOperationType.INSTALL, NodeType.NAMENODE);
        nodeOperationHandler.run();

        // assertions
        verify(hadoopImpl).getEnvironmentManager();
        verify(hadoopImpl).getCluster("test");
        assertEquals("test", containerHost.getHostname());
        assertEquals(uuid, environment.getId());
    }


    @Test()
    public void testRunCommandWithNodeOperationTypeSTART() throws CommandException
    {
        nodeOperationHandler =
                new NodeOperationHandler(hadoopImpl, "test", "test", NodeOperationType.INSTALL, NodeType.NAMENODE);

        nodeOperationHandler.runCommand(containerHost, NodeOperationType.START, NodeType.NAMENODE);
        nodeOperationHandler.runCommand(containerHost, NodeOperationType.START, NodeType.JOBTRACKER);
        nodeOperationHandler.runCommand(containerHost, NodeOperationType.START, NodeType.TASKTRACKER);
        nodeOperationHandler.runCommand(containerHost, NodeOperationType.START, NodeType.DATANODE);

        // assertions
        assertEquals(commandResult, containerHost.execute(any(RequestBuilder.class)));
    }


    @Test()
    public void testRunCommandWithNodeOperationTypeSTOP() throws CommandException
    {
        nodeOperationHandler =
                new NodeOperationHandler(hadoopImpl, "test", "test", NodeOperationType.INSTALL, NodeType.NAMENODE);
        nodeOperationHandler.runCommand(containerHost, NodeOperationType.STOP, NodeType.NAMENODE);
        nodeOperationHandler.runCommand(containerHost, NodeOperationType.STOP, NodeType.JOBTRACKER);
        nodeOperationHandler.runCommand(containerHost, NodeOperationType.STOP, NodeType.TASKTRACKER);
        nodeOperationHandler.runCommand(containerHost, NodeOperationType.STOP, NodeType.DATANODE);

        // assertions
        assertEquals(commandResult, containerHost.execute(any(RequestBuilder.class)));
    }


    @Test()
    public void testRunCommandWithNodeOperationTypeSTATUS() throws CommandException
    {
        nodeOperationHandler =
                new NodeOperationHandler(hadoopImpl, "test", "test", NodeOperationType.INSTALL, NodeType.NAMENODE);
        nodeOperationHandler.runCommand(containerHost, NodeOperationType.STATUS, NodeType.NAMENODE);
        nodeOperationHandler.runCommand(containerHost, NodeOperationType.STATUS, NodeType.JOBTRACKER);
        nodeOperationHandler.runCommand(containerHost, NodeOperationType.STATUS, NodeType.TASKTRACKER);
        nodeOperationHandler.runCommand(containerHost, NodeOperationType.STATUS, NodeType.DATANODE);
        nodeOperationHandler.runCommand(containerHost, NodeOperationType.STATUS, NodeType.SECONDARY_NAMENODE);

        // assertions
        assertEquals(commandResult, containerHost.execute(any(RequestBuilder.class)));
    }


    @Test
    public void testFindNodeInCluster() throws CommandException
    {
        nodeOperationHandler =
                new NodeOperationHandler(hadoopImpl, "test", "test", NodeOperationType.INSTALL, NodeType.NAMENODE);
        containerHost2 = nodeOperationHandler.findNodeInCluster("test");

        // assertions
        assertNotNull(nodeOperationHandler.findNodeInCluster("tes"));
        assertEquals(containerHost, containerHost2);
    }

    @Test
    public void testExcludeNode() throws CommandException
    {
        nodeOperationHandler =
                new NodeOperationHandler(hadoopImpl, "test", "test", NodeOperationType.INSTALL, NodeType.NAMENODE);
        nodeOperationHandler.excludeNode();

        // assertions
        verify(hadoopImpl).getPluginDAO();
        verify(trackerOperation).addLogDone("Cluster info saved to DB");
    }

    @Test
    public void testIncludeNode() throws CommandException
    {
        nodeOperationHandler =
                new NodeOperationHandler(hadoopImpl, "test", "test", NodeOperationType.INSTALL, NodeType.NAMENODE);
        nodeOperationHandler.includeNode();

        // assertions
        verify(hadoopImpl).getPluginDAO();
        verify(trackerOperation).addLogDone("Cluster info saved to DB");

    }
}