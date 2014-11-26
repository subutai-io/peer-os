package org.safehaus.subutai.plugin.shark.impl.handler;

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
import org.safehaus.subutai.plugin.shark.api.SetupType;
import org.safehaus.subutai.plugin.shark.api.SharkClusterConfig;
import org.safehaus.subutai.plugin.shark.impl.Commands;
import org.safehaus.subutai.plugin.shark.impl.SharkImpl;
import org.safehaus.subutai.plugin.spark.api.Spark;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;

import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class ClusterOperationHandlerTest {
    private ClusterOperationHandler clusterOperationHandler;
    private ClusterOperationHandler clusterOperationHandler2;
    private ClusterOperationHandler clusterOperationHandler3;
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
    private HadoopClusterConfig hadoopClusterConfig;
    private ClusterSetupStrategy clusterSetupStrategy;
    private Hadoop hadoop;
    private EnvironmentBlueprint environmentBlueprint;
    private ConfigBase configBase;

    @Before
    public void setUp() throws Exception {
        configBase = mock(ConfigBase.class);
        environmentBlueprint = mock(EnvironmentBlueprint.class);
        hadoop = mock(Hadoop.class);
        clusterSetupStrategy = mock(ClusterSetupStrategy.class);
        hadoopClusterConfig = mock(HadoopClusterConfig.class);
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
        clusterOperationHandler = new ClusterOperationHandler(sharkImpl, sharkClusterConfig, ClusterOperationType.INSTALL, hadoopClusterConfig);
        clusterOperationHandler2 = new ClusterOperationHandler(sharkImpl, sharkClusterConfig, ClusterOperationType.UNINSTALL, hadoopClusterConfig);
        clusterOperationHandler3 = new ClusterOperationHandler(sharkImpl, sharkClusterConfig, ClusterOperationType.CUSTOM, hadoopClusterConfig);
    }

    @Test
    public void testRunOperationOnContainersWithOperationTypeInstallWithoutHadoopInstalation() throws Exception {

        // mock setupCluster method
        // mock setup Shark cluster
        when(sharkImpl.getSparkManager()).thenReturn(spark);
        when(spark.getCluster(any(String.class))).thenReturn(sparkClusterConfig);
        when(sharkImpl.getEnvironmentManager()).thenReturn(environmentManager);
        when(environmentManager.getEnvironmentByUUID(any(UUID.class))).thenReturn(environment);
        when(sharkImpl.getClusterSetupStrategy(trackerOperation,sharkClusterConfig,environment)).thenReturn(clusterSetupStrategy);

        clusterOperationHandler.runOperationOnContainers(ClusterOperationType.INSTALL);

        // asserts
        assertNotEquals(SetupType.WITH_HADOOP_SPARK,sharkClusterConfig.getSetupType());
        assertEquals(spark,sharkImpl.getSparkManager());
        assertNotNull(sparkClusterConfig);
        assertEquals(sparkClusterConfig, sharkImpl.getSparkManager().getCluster(anyString()));
        assertNotNull(environment);
        assertEquals(environment,sharkImpl.getEnvironmentManager().getEnvironmentByUUID(any(UUID.class)));
        verify(sharkImpl).getClusterSetupStrategy(trackerOperation,sharkClusterConfig,environment);
    }

    @Test
    public void testRunWithOperationTypeInstallWithHadoopInstalation() throws Exception {
        // mock setupCluster method
        // mock setupCluster with Hadoop
        when(sharkClusterConfig.getSetupType()).thenReturn(SetupType.WITH_HADOOP_SPARK);
        when(sharkImpl.getHadoopManager()).thenReturn(hadoop);
        when(hadoop.getDefaultEnvironmentBlueprint(hadoopClusterConfig)).thenReturn(environmentBlueprint);
        when(sharkImpl.getEnvironmentManager()).thenReturn(environmentManager);
        when(environmentManager.buildEnvironment(environmentBlueprint)).thenReturn(environment);
        when(sharkImpl.getHadoopManager()).thenReturn(hadoop);
        when(hadoop.getClusterSetupStrategy(environment, hadoopClusterConfig, trackerOperation)).thenReturn(clusterSetupStrategy);
        when(clusterSetupStrategy.setup()).thenReturn(configBase);
        when(sharkClusterConfig.getSparkClusterName()).thenReturn("test");
        when(sharkImpl.getSparkManager()).thenReturn(spark);
        when(spark.getClusterSetupStrategy(any(TrackerOperation.class), any(SparkClusterConfig.class), any(Environment.class))).thenReturn(clusterSetupStrategy);
        when(clusterSetupStrategy.setup()).thenReturn(configBase);

        // mock setup Shark cluster
        when(sharkImpl.getSparkManager()).thenReturn(spark);
        when(spark.getCluster(any(String.class))).thenReturn(sparkClusterConfig);
        when(sharkImpl.getEnvironmentManager()).thenReturn(environmentManager);
        when(environmentManager.getEnvironmentByUUID(any(UUID.class))).thenReturn(environment);
        when(sharkImpl.getClusterSetupStrategy(trackerOperation, sharkClusterConfig, environment)).thenReturn(clusterSetupStrategy);


        clusterOperationHandler.run();

        // asserts
        assertEquals(SetupType.WITH_HADOOP_SPARK,sharkClusterConfig.getSetupType());
        assertEquals(environmentBlueprint,sharkImpl.getHadoopManager().getDefaultEnvironmentBlueprint(hadoopClusterConfig));
        assertEquals(environment,sharkImpl.getEnvironmentManager().buildEnvironment(environmentBlueprint));
        assertEquals(spark, sharkImpl.getSparkManager());
        assertNotNull(sparkClusterConfig);
        assertEquals(sparkClusterConfig, sharkImpl.getSparkManager().getCluster(anyString()));
        assertNotNull(environment);
        assertEquals(environment, sharkImpl.getEnvironmentManager().getEnvironmentByUUID(any(UUID.class)));
        verify(sharkImpl).getClusterSetupStrategy(trackerOperation, sharkClusterConfig, environment);
    }

    @Test
    public void testRunWithOperationTypeUninstall() throws CommandException, ClusterException {
        // mock destroyCluster method
        Set<ContainerHost> mySet = mock(Set.class);
        mySet.add(containerHost);
        when(sharkImpl.getEnvironmentManager()).thenReturn(environmentManager);
        when(environmentManager.getEnvironmentByUUID(any(UUID.class))).thenReturn(environment);
        when(environment.getHostsByIds(any(Set.class))).thenReturn(mySet).thenReturn(mySet);
        Iterator<ContainerHost> iterator = mock(Iterator.class);
        when(mySet.iterator()).thenReturn(iterator).thenReturn(iterator);
        when(iterator.hasNext()).thenReturn(true).thenReturn(false).thenReturn(true).thenReturn(false);
        when(iterator.next()).thenReturn(containerHost).thenReturn(containerHost);
        when(containerHost.isConnected()).thenReturn(true);
        when(mySet.size()).thenReturn(1);
        when(sharkImpl.getCommands()).thenReturn(commands);
        when(commands.getUninstallCommand()).thenReturn(requestBuilder);
        when(sharkImpl.getPluginDao()).thenReturn(pluginDAO);
        when(pluginDAO.deleteInfo(anyString(),anyString())).thenReturn(true);

        // mock executeCommand
        when(containerHost.execute(requestBuilder)).thenReturn(commandResult);
        when(commandResult.hasSucceeded()).thenReturn(true);
        clusterOperationHandler.executeCommand(containerHost, requestBuilder);

        clusterOperationHandler2.run();


        // asserts
        assertNotNull(environment);
        assertEquals(environment, sharkImpl.getEnvironmentManager().getEnvironmentByUUID(any(UUID.class)));
        assertTrue(containerHost.isConnected());
        assertEquals(requestBuilder,sharkImpl.getCommands().getUninstallCommand());
        assertTrue(sharkImpl.getPluginDao().deleteInfo(anyString(),anyString()));
    }

    @Test
    public void testRunWithOperationTypeCustom() throws CommandException, ClusterException {
        // mock actualizeMasterIP method
        when(sharkImpl.getSparkManager()).thenReturn(spark);
        when(spark.getCluster(anyString())).thenReturn(sparkClusterConfig);
        when(sharkImpl.getEnvironmentManager()).thenReturn(environmentManager);
        when(environmentManager.getEnvironmentByUUID(any(UUID.class))).thenReturn(environment);
        Set<ContainerHost> mySet = mock(Set.class);
        mySet.add(containerHost);
        when(environment.getHostsByIds(any(Set.class))).thenReturn(mySet).thenReturn(mySet);
        Iterator<ContainerHost> iterator = mock(Iterator.class);
        when(mySet.iterator()).thenReturn(iterator).thenReturn(iterator);
        when(iterator.hasNext()).thenReturn(true).thenReturn(false).thenReturn(true).thenReturn(false);
        when(iterator.next()).thenReturn(containerHost).thenReturn(containerHost);
        when(containerHost.isConnected()).thenReturn(true);
        when(mySet.size()).thenReturn(1);
        when(environment.getContainerHostByUUID(any(UUID.class))).thenReturn(containerHost);
        when(sharkImpl.getCommands()).thenReturn(commands);
        when(commands.getSetMasterIPCommand(containerHost)).thenReturn(requestBuilder);

        //mock executeCommand method
        when(containerHost.execute(requestBuilder)).thenReturn(commandResult);
        when(commandResult.hasSucceeded()).thenReturn(true);
        clusterOperationHandler.executeCommand(containerHost, requestBuilder);

        clusterOperationHandler3.run();

        // asserts
        assertEquals(sparkClusterConfig,sharkImpl.getSparkManager().getCluster(anyString()));
        assertNotNull(sparkClusterConfig);
        assertNotNull(environment);
        assertEquals(environment, sharkImpl.getEnvironmentManager().getEnvironmentByUUID(any(UUID.class)));
        assertTrue(containerHost.isConnected());
        assertNotNull(containerHost);
        assertEquals(containerHost,environment.getContainerHostByUUID(any(UUID.class)));
        assertEquals(commandResult,clusterOperationHandler3.executeCommand(containerHost,requestBuilder));

    }

    @Test
    public void testExecuteCommand() throws Exception {
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