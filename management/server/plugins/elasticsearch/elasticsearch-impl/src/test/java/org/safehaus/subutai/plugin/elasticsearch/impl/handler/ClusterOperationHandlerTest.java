package org.safehaus.subutai.plugin.elasticsearch.impl.handler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentDestroyException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.common.PluginDAO;
import org.safehaus.subutai.plugin.common.api.ClusterOperationType;
import org.safehaus.subutai.plugin.elasticsearch.api.ElasticsearchClusterConfiguration;
import org.safehaus.subutai.plugin.elasticsearch.impl.ElasticsearchImpl;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
    private UUID uuid;
    @Mock
    ElasticsearchImpl elasticsearchImpl;
    @Mock
    ElasticsearchClusterConfiguration clusterConfiguration;
    @Mock
    Tracker tracker;
    @Mock
    TrackerOperation trackerOperation;
    @Mock
    EnvironmentManager environmentManager;
    @Mock
    Environment environment;
    @Mock
    ContainerHost containerHost;
    @Mock
    CommandResult commandResult;
    @Mock
    PluginDAO pluginDAO;
    @Mock
    ClusterSetupStrategy clusterSetupStrategy;

    @Before
    public void setUp() throws Exception
    {
        when(elasticsearchImpl.getTracker()).thenReturn(tracker);
        when(tracker.createTrackerOperation(anyString(), anyString())).thenReturn(trackerOperation);
        when(clusterConfiguration.getClusterName()).thenReturn("test");
        when(elasticsearchImpl.getEnvironmentManager()).thenReturn(environmentManager);
        when(environmentManager.getEnvironmentByUUID(any(UUID.class))).thenReturn(environment);
        when(containerHost.execute(any(RequestBuilder.class))).thenReturn(commandResult);


        clusterOperationHandler = new ClusterOperationHandler(elasticsearchImpl, clusterConfiguration,
                ClusterOperationType.INSTALL);
        clusterOperationHandler2 = new ClusterOperationHandler(elasticsearchImpl, clusterConfiguration,
                ClusterOperationType.UNINSTALL);
        clusterOperationHandler3 = new ClusterOperationHandler(elasticsearchImpl, clusterConfiguration,
                ClusterOperationType.STATUS_ALL);

    }

    @Test
    public void testRunWhenOperationTypeInstall() throws Exception
    {
        when(environmentManager.buildEnvironment(any(EnvironmentBlueprint.class))).thenReturn(environment);
        when(elasticsearchImpl.getClusterSetupStrategy(environment, clusterConfiguration, trackerOperation))
                .thenReturn(clusterSetupStrategy);

        clusterOperationHandler.run();

        // assertions
        assertNotNull(elasticsearchImpl.getClusterSetupStrategy(environment, clusterConfiguration, trackerOperation));
    }

    @Test
    public void testRunWhenOperationTypeUninstall() throws EnvironmentDestroyException
    {
        when(elasticsearchImpl.getCluster("test")).thenReturn(clusterConfiguration);
        when(environmentManager.destroyEnvironment(any(UUID.class))).thenReturn(true);
        when(elasticsearchImpl.getPluginDAO()).thenReturn(pluginDAO);

        clusterOperationHandler2.run();

        // assertions
        assertNotNull(elasticsearchImpl.getCluster("test"));
        verify(trackerOperation).addLog("Destroying environment...");
        assertEquals(pluginDAO, elasticsearchImpl.getPluginDAO());
        verify(trackerOperation).addLogDone("Cluster destroyed");
    }


    @Test
    public void testRunWhenOperationTypeStatusAll() throws CommandException
    {
        Set<ContainerHost> mySet = new HashSet<>();
        mySet.add(containerHost);
        when(environment.getContainerHosts()).thenReturn(mySet);

        clusterOperationHandler3.run();

        // assertions
        assertNotNull(environment.getContainerHosts());
    }

    @Test
    public void testRunOperationOnContainersWithClusterOperationTypeStartAll() throws Exception
    {
        Set<ContainerHost> mySet = new HashSet<>();
        mySet.add(containerHost);
        when(environment.getContainerHosts()).thenReturn(mySet);

        clusterOperationHandler.runOperationOnContainers(ClusterOperationType.START_ALL);

        // assertions
        assertNotNull(environment.getContainerHosts());
    }

    @Test
    public void testRunOperationOnContainersWithClusterOperationTypeStopAll() throws Exception
    {
        Set<ContainerHost> mySet = new HashSet<>();
        mySet.add(containerHost);
        when(environment.getContainerHosts()).thenReturn(mySet);

        clusterOperationHandler.runOperationOnContainers(ClusterOperationType.STOP_ALL);

        // assertions
        assertNotNull(environment.getContainerHosts());
    }


}