package org.safehaus.subutai.plugin.hbase.impl.handler;

import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
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
import org.safehaus.subutai.plugin.hbase.impl.HBaseImpl;

import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ClusterOperationHandlerTest
{
    private ClusterOperationHandler clusterOperationHandler;
    private ClusterOperationHandler clusterOperationHandler2;
    private ClusterOperationHandler clusterOperationHandler3;
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

    @Before
    public void setUp() throws Exception
    {
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
        clusterOperationHandler3 = new ClusterOperationHandler(hBaseImpl,hBaseConfig, ClusterOperationType.INSTALL);
    }

    @Test
    public void testRunWithOperationTypeInstall() throws Exception
    {
        // mock setupCluster method
        when(hBaseImpl.getEnvironmentManager()).thenReturn(environmentManager);
        when(environmentManager.getEnvironmentByUUID(any(UUID.class))).thenReturn(environment);
        when(hBaseImpl.getClusterSetupStrategy(trackerOperation,hBaseConfig,environment)).thenReturn(clusterSetupStrategy);

        clusterOperationHandler.run();

        // asserts

    }

    @Test
    public void testRunWithOperationTypeUninstall() throws Exception
    {
        // mock setupCluster method
        when(hBaseImpl.getEnvironmentManager()).thenReturn(environmentManager);
        when(environmentManager.getEnvironmentByUUID(any(UUID.class))).thenReturn(environment);
        when(hBaseImpl.getClusterSetupStrategy(trackerOperation,hBaseConfig,environment)).thenReturn(clusterSetupStrategy);

        clusterOperationHandler2.run();

        // asserts

    }

    @Test
    public void testRunOperationOnContainers() throws Exception
    {

    }

    @Test
    public void testSetupCluster() throws Exception
    {

    }

    @Test
    public void testDestroyCluster() throws Exception
    {

    }

    @Test
    public void testExecuteCommand() throws Exception
    {

    }
}