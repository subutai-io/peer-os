package org.safehaus.subutai.plugin.shark.impl.handler;

import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.exception.ClusterException;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.common.PluginDAO;
import org.safehaus.subutai.plugin.common.api.OperationType;
import org.safehaus.subutai.plugin.shark.api.SharkClusterConfig;
import org.safehaus.subutai.plugin.shark.impl.Commands;
import org.safehaus.subutai.plugin.shark.impl.SharkImpl;
import org.safehaus.subutai.plugin.spark.api.Spark;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NodeOperationHandlerTest {
    NodeOperationHandler nodeOperationHandler;
    NodeOperationHandler nodeOperationHandler2;
    private SharkImpl sharkImpl;
    private SharkClusterConfig sharkClusterConfig;
    private Tracker tracker;
    private TrackerOperation trackerOperation;
    private EnvironmentManager environmentManager;
    private Environment environment;
    private ContainerHost containerHost;
    private RequestBuilder requestBuilder;
    private CommandResult commandResult;
    private UUID uuid;
    private Spark spark;
    private SparkClusterConfig sparkClusterConfig;
    private Commands commands;
    private PluginDAO pluginDAO;

    @Before
    public void setUp() {
        pluginDAO = mock(PluginDAO.class);
        commands = mock(Commands.class);
        sparkClusterConfig = mock(SparkClusterConfig.class);
        spark = mock(Spark.class);
        uuid = new UUID(50, 50);
        commandResult = mock(CommandResult.class);
        requestBuilder = mock(RequestBuilder.class);
        containerHost = mock(ContainerHost.class);
        environment = mock(Environment.class);
        environmentManager = mock(EnvironmentManager.class);
        trackerOperation = mock(TrackerOperation.class);
        tracker = mock(Tracker.class);
        sharkImpl = mock(SharkImpl.class);
        sharkClusterConfig = mock(SharkClusterConfig.class);
        when(sharkImpl.getTracker()).thenReturn(tracker);
        when(tracker.createTrackerOperation(anyString(), anyString())).thenReturn(trackerOperation);
        nodeOperationHandler = new NodeOperationHandler(sharkImpl, sharkClusterConfig, "test", OperationType.INCLUDE);
        nodeOperationHandler2 = new NodeOperationHandler(sharkImpl, sharkClusterConfig, "test", OperationType.EXCLUDE);
    }


    @Test
    public void testRunWithOperationTypeInclude() throws CommandException, ClusterException {
        // mock run method
        when(sharkImpl.getCluster(any(String.class))).thenReturn(sharkClusterConfig);
        when(sharkImpl.getEnvironmentManager()).thenReturn(environmentManager);
        when(environmentManager.getEnvironmentByUUID(any(UUID.class))).thenReturn(environment);
        when(environment.getContainerHostByHostname("test")).thenReturn(containerHost);
        when(containerHost.isConnected()).thenReturn(true);

        // mock addNode method
        List<UUID> myList = mock(ArrayList.class);
        myList.add(uuid);
        when(containerHost.getId()).thenReturn(uuid);
        when(sharkImpl.getSparkManager()).thenReturn(spark);
        when(spark.getCluster(any(String.class))).thenReturn(sparkClusterConfig);
        when(environment.getContainerHostByUUID(any(UUID.class))).thenReturn(containerHost);
        when(sparkClusterConfig.getAllNodesIds()).thenReturn(myList);
        when(sparkClusterConfig.getAllNodesIds().contains(uuid)).thenReturn(true);

        when(commands.getCheckInstalledCommand()).thenReturn(requestBuilder);
        when(commandResult.getStdOut()).thenReturn("test");
        when(commands.getInstallCommand()).thenReturn(requestBuilder);
        when(commands.getSetMasterIPCommand(containerHost)).thenReturn(requestBuilder);
        when(sharkImpl.getPluginDao()).thenReturn(pluginDAO);
        when(pluginDAO.saveInfo(anyString(),anyString(),any())).thenReturn(true);

        // mock executeCommand method
        when(sharkImpl.getCommands()).thenReturn(commands);
        when(containerHost.execute(requestBuilder)).thenReturn(commandResult);
        when(commandResult.hasSucceeded()).thenReturn(true);
        nodeOperationHandler.executeCommand(containerHost, requestBuilder);

        nodeOperationHandler.run();

        assertNotNull(environment);
        assertNotNull(containerHost);
        assertNotNull(sparkClusterConfig);
        assertNotNull(commandResult);
        assertEquals(commandResult, nodeOperationHandler.executeCommand(containerHost, sharkImpl.getCommands().getInstallCommand()));
        assertEquals(uuid, containerHost.getId());
        assertTrue(containerHost.isConnected());
        assertTrue(pluginDAO.saveInfo(anyString(),anyString(),any()));
    }

    @Test
    public void testRunWithOperationTypeExclude() throws CommandException, ClusterException {
        // mock run method
        when(sharkImpl.getCluster(any(String.class))).thenReturn(sharkClusterConfig);
        when(sharkImpl.getEnvironmentManager()).thenReturn(environmentManager);
        when(environmentManager.getEnvironmentByUUID(any(UUID.class))).thenReturn(environment);
        when(environment.getContainerHostByHostname("test")).thenReturn(containerHost);
        when(containerHost.isConnected()).thenReturn(true);

        // mock removeNode method
        Set<UUID> mySet = mock(Set.class);
        mySet.add(uuid);
        when(containerHost.getId()).thenReturn(uuid);
        when(sharkClusterConfig.getNodeIds()).thenReturn(mySet);
        when(sharkClusterConfig.getNodeIds().contains(any())).thenReturn(true);

        when(sharkImpl.getPluginDao()).thenReturn(pluginDAO);
        when(pluginDAO.saveInfo(anyString(), anyString(), any())).thenReturn(true);


        // mock executeCommand method
        when(sharkImpl.getCommands()).thenReturn(commands);
        when(commands.getUninstallCommand()).thenReturn(requestBuilder);

        when(containerHost.execute(requestBuilder)).thenReturn(commandResult);
        when(commandResult.hasSucceeded()).thenReturn(true);
        nodeOperationHandler.executeCommand(containerHost, requestBuilder);


        nodeOperationHandler2.run();

        assertNotNull(environment);
        assertNotNull(containerHost);
        assertNotNull(sparkClusterConfig);
        assertNotNull(commandResult);
        assertEquals(commandResult, nodeOperationHandler.executeCommand(containerHost, sharkImpl.getCommands().getUninstallCommand()));
        assertEquals(uuid, containerHost.getId());
        assertTrue(containerHost.isConnected());
        assertTrue(pluginDAO.saveInfo(anyString(),anyString(),any()));
    }


    @Test
    public void testExecuteCommand() throws CommandException, ClusterException {
        when(containerHost.execute(requestBuilder)).thenReturn(commandResult);
        when(commandResult.hasSucceeded()).thenReturn(true);
        nodeOperationHandler.executeCommand(containerHost, requestBuilder);

        assertTrue(commandResult.hasSucceeded());
        assertEquals(commandResult, nodeOperationHandler.executeCommand(containerHost, requestBuilder));
        assertEquals(commandResult, containerHost.execute(requestBuilder));
        assertNotNull(nodeOperationHandler.executeCommand(containerHost, requestBuilder));

    }

    @Test(expected = IllegalArgumentException.class)
    public void testRunWhenClusterNameIsNull() {
        when(sharkImpl.getTracker()).thenReturn(tracker);
        nodeOperationHandler = new NodeOperationHandler(sharkImpl, sharkClusterConfig, null, OperationType.INSTALL);
    }

    @Test(expected = ClusterException.class)
    public void shouldThrowsClusterExceptionInExecuteCommand() throws CommandException, ClusterException {
        when(containerHost.execute(requestBuilder)).thenReturn(commandResult);
        nodeOperationHandler.executeCommand(containerHost, requestBuilder);
    }
}