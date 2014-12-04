package org.safehaus.subutai.plugin.hbase.impl.handler;

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
import org.safehaus.subutai.plugin.hbase.api.HBaseConfig;
import org.safehaus.subutai.plugin.hbase.impl.Commands;
import org.safehaus.subutai.plugin.hbase.impl.HBaseImpl;

import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class NodeOperationHandlerTest
{
    private NodeOperationHandler nodeOperationHandler;
    private NodeOperationHandler nodeOperationHandler2;
    private HBaseImpl hBaseImpl;
    private HBaseConfig hBaseConfig;
    private Tracker tracker;
    private TrackerOperation trackerOperation;
    private EnvironmentManager environmentManager;
    private Environment environment;
    private ContainerHost containerHost;
    private RequestBuilder requestBuilder;
    private CommandResult commandResult;
    private UUID uuid;
    private Commands commands;
    private PluginDAO pluginDAO;

    @Before
    public void setUp() throws Exception
    {
        pluginDAO = mock(PluginDAO.class);
        commands = mock(Commands.class);
        uuid = new UUID(50, 50);
        commandResult = mock(CommandResult.class);
        requestBuilder = mock(RequestBuilder.class);
        containerHost = mock(ContainerHost.class);
        environment = mock(Environment.class);
        environmentManager = mock(EnvironmentManager.class);
        trackerOperation = mock(TrackerOperation.class);
        tracker = mock(Tracker.class);

        hBaseImpl = mock(HBaseImpl.class);
        hBaseConfig = mock(HBaseConfig.class);
        tracker = mock(Tracker.class);
        trackerOperation = mock(TrackerOperation.class);

        nodeOperationHandler = new NodeOperationHandler(hBaseImpl, hBaseConfig);
        nodeOperationHandler = new NodeOperationHandler(hBaseImpl, hBaseConfig, uuid, OperationType.INSTALL);
        when(hBaseImpl.getTracker()).thenReturn(tracker);
        when(tracker.createTrackerOperation(anyString(), anyString())).thenReturn(trackerOperation);

        nodeOperationHandler = new NodeOperationHandler(hBaseImpl, hBaseConfig, "test", OperationType.EXCLUDE);
        nodeOperationHandler2 = new NodeOperationHandler(hBaseImpl, hBaseConfig, "test", OperationType.STATUS);
    }

    @Test
    public void testRunWithOperationTypeExclude() throws CommandException, ClusterException
    {
        // mock run method
        when(hBaseImpl.getCluster(any(String.class))).thenReturn(hBaseConfig);
        when(hBaseImpl.getEnvironmentManager()).thenReturn(environmentManager);
        when(environmentManager.getEnvironmentByUUID(any(UUID.class))).thenReturn(environment);
        when(environment.getContainerHostByHostname("test")).thenReturn(containerHost);
        when(containerHost.isConnected()).thenReturn(true);

        // mock removeNode method
        Set<UUID> mySet = mock(Set.class);
        mySet.add(uuid);
        when(containerHost.getId()).thenReturn(uuid);
        when(hBaseConfig.getAllNodes()).thenReturn(mySet);
        when(hBaseConfig.getAllNodes().contains(any())).thenReturn(true);
        when(hBaseConfig.getAllNodes().size()).thenReturn(3);
        when(hBaseConfig.getAllNodes()).thenReturn(mySet);
        when(containerHost.getId()).thenReturn(uuid);
        when(hBaseConfig.getAllNodes().remove(any(UUID.class))).thenReturn(true);
        when(hBaseImpl.getPluginDAO()).thenReturn(pluginDAO);
        when(pluginDAO.saveInfo(anyString(), anyString(), any())).thenReturn(true);


        // mock executeCommand method
        when(hBaseImpl.getCommands()).thenReturn(commands);
        when(commands.getUninstallCommand()).thenReturn(requestBuilder);

        when(containerHost.execute(requestBuilder)).thenReturn(commandResult);
        when(commandResult.hasSucceeded()).thenReturn(true);
        nodeOperationHandler.executeCommand(containerHost, requestBuilder);


        nodeOperationHandler.run();

        // assertions
        assertNotNull(environment);
        assertNotNull(containerHost);
        assertNotNull(hBaseConfig);
        assertNotNull(commandResult);
        assertEquals(commandResult, nodeOperationHandler.executeCommand(containerHost, hBaseImpl.getCommands()
                .getUninstallCommand()));
        assertEquals(uuid, containerHost.getId());
        assertTrue(containerHost.isConnected());
        assertTrue(pluginDAO.saveInfo(anyString(), anyString(), any()));
        assertTrue(hBaseConfig.getAllNodes().remove(any(UUID.class)));
    }

    @Test
    public void testRunWithOperationTypeStatus() throws CommandException
    {
        // mock run method
        when(hBaseImpl.getCluster(any(String.class))).thenReturn(hBaseConfig);
        when(hBaseImpl.getEnvironmentManager()).thenReturn(environmentManager);
        when(environmentManager.getEnvironmentByUUID(any(UUID.class))).thenReturn(environment);
        when(environment.getContainerHostByHostname("test")).thenReturn(containerHost);
        when(containerHost.isConnected()).thenReturn(true);

        // mock checkServiceStatus method
        when(environment.getContainerHostById(any(UUID.class))).thenReturn(containerHost);
        when(containerHost.execute(any(RequestBuilder.class))).thenReturn(commandResult);
        when(commandResult.hasSucceeded()).thenReturn(true);

        nodeOperationHandler2.run();

        // assertions
        verify(environment).getContainerHostById(any(UUID.class));
        assertNotNull(environment);
        assertNotNull(containerHost);
        assertTrue(containerHost.isConnected());
        assertTrue(commandResult.hasSucceeded());
    }

    @Test(expected = ClusterException.class)
    public void shouldThrowsClusterExceptionInExecuteCommand() throws CommandException, ClusterException
    {
        when(containerHost.execute(requestBuilder)).thenReturn(commandResult);
        nodeOperationHandler.executeCommand(containerHost, requestBuilder);
    }

}