package org.safehaus.subutai.plugin.hbase.impl.handler;

import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.exception.ClusterException;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.common.PluginDAO;
import org.safehaus.subutai.plugin.common.api.ClusterOperationType;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hbase.api.HBaseConfig;
import org.safehaus.subutai.plugin.hbase.impl.Commands;
import org.safehaus.subutai.plugin.hbase.impl.HBaseImpl;

import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class ClusterOperationHandlerTest
{
    private ClusterOperationHandler clusterOperationHandler;
    private ClusterOperationHandler clusterOperationHandler2;
    private ClusterOperationHandler clusterOperationHandler3;
    private ClusterOperationHandler clusterOperationHandler4;
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
    private PluginDAO pluginDAO;
    private HadoopClusterConfig hadoopClusterConfig;
    private ClusterSetupStrategy clusterSetupStrategy;
    private Hadoop hadoop;
    private EnvironmentBlueprint environmentBlueprint;
    private ConfigBase configBase;
    private Commands commands;

    @Before
    public void setUp()
    {
        commands = mock(Commands.class);
        hBaseConfig = mock(HBaseConfig.class);
        hBaseImpl = mock(HBaseImpl.class);
        configBase = mock(ConfigBase.class);
        environmentBlueprint = mock(EnvironmentBlueprint.class);
        hadoop = mock(Hadoop.class);
        clusterSetupStrategy = mock(ClusterSetupStrategy.class);
        hadoopClusterConfig = mock(HadoopClusterConfig.class);
        pluginDAO = mock(PluginDAO.class);
        uuid = new UUID(50, 50);
        commandResult = mock(CommandResult.class);
        requestBuilder = mock(RequestBuilder.class);
        containerHost = mock(ContainerHost.class);
        environment = mock(Environment.class);
        environmentManager = mock(EnvironmentManager.class);
        trackerOperation = mock(TrackerOperation.class);
        tracker = mock(Tracker.class);
        when(hBaseImpl.getTracker()).thenReturn(tracker);
        when(tracker.createTrackerOperation(anyString(), anyString())).thenReturn(trackerOperation);

        clusterOperationHandler = new ClusterOperationHandler(hBaseImpl,hBaseConfig, ClusterOperationType.INSTALL);
        clusterOperationHandler2 = new ClusterOperationHandler(hBaseImpl,hBaseConfig, ClusterOperationType.UNINSTALL);
        clusterOperationHandler3 = new ClusterOperationHandler(hBaseImpl,hBaseConfig, ClusterOperationType.START_ALL);
        clusterOperationHandler4 = new ClusterOperationHandler(hBaseImpl,hBaseConfig, ClusterOperationType.STOP_ALL);
    }

    @Test
    public void testRunWithOperationTypeInstall() 
    {
        // mock setupCluster method
        when(hBaseImpl.getEnvironmentManager()).thenReturn(environmentManager);
        when(environmentManager.getEnvironmentByUUID(any(UUID.class))).thenReturn(environment);
        when(hBaseImpl.getClusterSetupStrategy(trackerOperation,hBaseConfig,environment)).thenReturn(clusterSetupStrategy);

        clusterOperationHandler.run();

        // asserts
        assertNotNull(environment);
        assertEquals(environment, hBaseImpl.getEnvironmentManager().getEnvironmentByUUID(any(UUID.class)));
        verify(hBaseImpl).getClusterSetupStrategy(trackerOperation, hBaseConfig, environment);

    }

    @Test
    public void testRunWithOperationTypeUninstall() throws CommandException, ClusterException
    {
        // mock destroyCluster method
        Set<ContainerHost> mySet = mock(Set.class);
        mySet.add(containerHost);
        when(hBaseImpl.getEnvironmentManager()).thenReturn(environmentManager);
        when(environmentManager.getEnvironmentByUUID(any(UUID.class))).thenReturn(environment);
        when(environment.getContainerHostsByIds( any( Set.class ) )).thenReturn(mySet).thenReturn(mySet);
        Iterator<ContainerHost> iterator = mock(Iterator.class);
        when(mySet.iterator()).thenReturn(iterator).thenReturn(iterator);
        when(iterator.hasNext()).thenReturn(true).thenReturn(false).thenReturn(true).thenReturn(false);
        when(iterator.next()).thenReturn(containerHost).thenReturn(containerHost);
        when(containerHost.isConnected()).thenReturn(true);
        when(hBaseImpl.getCommands()).thenReturn(commands);
        when(commands.getUninstallCommand()).thenReturn(requestBuilder);
        when(hBaseImpl.getPluginDAO()).thenReturn(pluginDAO);
        when(pluginDAO.deleteInfo(anyString(), anyString())).thenReturn(true);

        // mock executeCommand
        when(containerHost.execute(requestBuilder)).thenReturn(commandResult);
        when(commandResult.hasSucceeded()).thenReturn(true);
        clusterOperationHandler.executeCommand(containerHost, requestBuilder);

        clusterOperationHandler2.run();

        // asserts
        assertNotNull(environment);
        assertEquals(environment, hBaseImpl.getEnvironmentManager().getEnvironmentByUUID(any(UUID.class)));
        assertTrue(containerHost.isConnected());
        assertEquals(requestBuilder, hBaseImpl.getCommands().getUninstallCommand());
        assertTrue(hBaseImpl.getPluginDAO().deleteInfo(anyString(), anyString()));
    }

    @Test
    public void testRunWithOperationTypeStartAll() throws CommandException, ClusterException
    {
        // mock destroyCluster method
        Set<ContainerHost> mySet = mock(Set.class);
        mySet.add(containerHost);
        when(hBaseImpl.getEnvironmentManager()).thenReturn(environmentManager);
        when(environmentManager.getEnvironmentByUUID(any(UUID.class))).thenReturn(environment);
        when(environment.getContainerHostsByIds( any( Set.class ) )).thenReturn(mySet).thenReturn(mySet);
        Iterator<ContainerHost> iterator = mock(Iterator.class);
        when(mySet.iterator()).thenReturn(iterator).thenReturn(iterator);
        when(iterator.hasNext()).thenReturn(true).thenReturn(false).thenReturn(true).thenReturn(false);
        when(iterator.next()).thenReturn(containerHost).thenReturn(containerHost);
        when(containerHost.isConnected()).thenReturn(true);

        // mock executeCommand
        when(containerHost.execute(any(RequestBuilder.class))).thenReturn(commandResult);
        when(commandResult.hasSucceeded()).thenReturn(true);
        when(commandResult.getStdOut()).thenReturn("test");
        clusterOperationHandler.executeCommand(containerHost, requestBuilder);

        clusterOperationHandler3.run();

        // asserts
        verify(hBaseImpl).getEnvironmentManager();
        assertEquals(environment,environmentManager.getEnvironmentByUUID(any(UUID.class)));
        assertNotNull(hBaseConfig.getAllNodes());
        assertTrue(commandResult.hasSucceeded());
        assertEquals("test",commandResult.getStdOut());

    }

    @Test
    public void testRunWithOperationTypeStopAll() throws CommandException, ClusterException
    {
        Set<UUID> myUUID = mock(Set.class);
        myUUID.add(uuid);

        // mock destroyCluster method
        Set<ContainerHost> mySet = mock(Set.class);
        mySet.add(containerHost);
        when(hBaseImpl.getEnvironmentManager()).thenReturn(environmentManager);
        when(environmentManager.getEnvironmentByUUID(any(UUID.class))).thenReturn(environment);
        when(environment.getContainerHostsByIds(any(Set.class))).thenReturn(mySet).thenReturn(mySet);
        Iterator<ContainerHost> iterator = mock(Iterator.class);
        when(mySet.iterator()).thenReturn(iterator).thenReturn(iterator);
        when(iterator.hasNext()).thenReturn(true).thenReturn(false).thenReturn(true).thenReturn(false);
        when(iterator.next()).thenReturn(containerHost).thenReturn(containerHost);
        when(containerHost.isConnected()).thenReturn(true);

        // mock executeCommand
        when(containerHost.execute(any(RequestBuilder.class))).thenReturn(commandResult);
        when(commandResult.hasSucceeded()).thenReturn(true);
        when(commandResult.getStdOut()).thenReturn("test");
        clusterOperationHandler.executeCommand(containerHost, requestBuilder);

        clusterOperationHandler4.run();

        // asserts
        verify(hBaseImpl).getEnvironmentManager();
        assertEquals(environment,environmentManager.getEnvironmentByUUID(any(UUID.class)));
        assertNotNull(hBaseConfig.getAllNodes());
        assertTrue(commandResult.hasSucceeded());
        assertEquals("test",commandResult.getStdOut());
    }

    @Test
    public void testExecuteCommand() throws CommandException, ClusterException
    {
        when(containerHost.execute(requestBuilder)).thenReturn(commandResult);
        when(commandResult.hasSucceeded()).thenReturn(true);
        clusterOperationHandler.executeCommand(containerHost, requestBuilder);

        // asserts
        assertTrue(commandResult.hasSucceeded());
        assertEquals(commandResult, clusterOperationHandler.executeCommand(containerHost, requestBuilder));
        assertEquals(commandResult, containerHost.execute(requestBuilder));
        assertNotNull(clusterOperationHandler.executeCommand(containerHost, requestBuilder));

    }

}