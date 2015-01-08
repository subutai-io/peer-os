package org.safehaus.subutai.plugin.elasticsearch.impl.handler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.common.PluginDAO;
import org.safehaus.subutai.plugin.common.api.NodeOperationType;
import org.safehaus.subutai.plugin.elasticsearch.api.ElasticsearchClusterConfiguration;
import org.safehaus.subutai.plugin.elasticsearch.impl.ElasticsearchImpl;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NodeOperationHandlerTest
{
    private NodeOperationHandler nodeOperationHandler;
    private NodeOperationHandler nodeOperationHandler2;
    private NodeOperationHandler nodeOperationHandler3;
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
        // mock constructor
        when(elasticsearchImpl.getTracker()).thenReturn(tracker);
        when(tracker.createTrackerOperation(anyString(), anyString())).thenReturn(trackerOperation);

        // mock run method
        Set<ContainerHost> mySet = new HashSet<>();
        mySet.add(containerHost);
        when(elasticsearchImpl.getCluster("test")).thenReturn(clusterConfiguration);
        when(elasticsearchImpl.getEnvironmentManager()).thenReturn(environmentManager);
        when(environmentManager.getEnvironmentByUUID(any(UUID.class))).thenReturn(environment);
        when(environment.getContainerHosts()).thenReturn(mySet);
        when(containerHost.getHostname()).thenReturn("test");
        when(containerHost.execute(any(RequestBuilder.class))).thenReturn(commandResult);

        nodeOperationHandler = new NodeOperationHandler(elasticsearchImpl,"test","test", NodeOperationType.START);
        nodeOperationHandler2 = new NodeOperationHandler(elasticsearchImpl,"test","test", NodeOperationType.STOP);
        nodeOperationHandler3 = new NodeOperationHandler(elasticsearchImpl,"test","test", NodeOperationType.STATUS);

    }

    @Test
    public void testRunWithOperationTypeStart() throws Exception
    {
        nodeOperationHandler.run();

        // assertions
        assertNotNull(elasticsearchImpl.getCluster("test"));
        assertNotNull(environment.getContainerHosts());
        assertEquals(commandResult,containerHost.execute(any(RequestBuilder.class)));
    }

    @Test
    public void testRunWithOperationTypeStop() throws Exception
    {
        nodeOperationHandler2.run();

        // assertions
        assertNotNull(elasticsearchImpl.getCluster("test"));
        assertNotNull(environment.getContainerHosts());
        assertEquals(commandResult,containerHost.execute(any(RequestBuilder.class)));
    }

    @Test
    public void testRunWithOperationTypeStatus() throws Exception
    {
        nodeOperationHandler3.run();

        // assertions
        assertNotNull(elasticsearchImpl.getCluster("test"));
        assertNotNull(environment.getContainerHosts());
        assertEquals(commandResult,containerHost.execute(any(RequestBuilder.class)));
    }


    @Test
    public void testLogResultsWhenElasticSearchIsNotRunning() throws Exception
    {
        when(commandResult.getExitCode()).thenReturn(768);
        nodeOperationHandler.logResults(trackerOperation, commandResult);
    }

    @Test
    public void testLogResultsWhenUnknownStatus() throws Exception
    {
        when(commandResult.getExitCode()).thenReturn(5);
        nodeOperationHandler.logResults(trackerOperation, commandResult);
    }

}