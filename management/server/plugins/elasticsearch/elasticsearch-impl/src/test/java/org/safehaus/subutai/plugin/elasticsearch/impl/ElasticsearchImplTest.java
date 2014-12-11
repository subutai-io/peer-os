package org.safehaus.subutai.plugin.elasticsearch.impl;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.common.PluginDAO;
import org.safehaus.subutai.plugin.elasticsearch.api.ElasticsearchClusterConfiguration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.*;
import java.util.concurrent.ExecutorService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@Ignore
public class ElasticsearchImplTest
{
    private ElasticsearchImpl elasticsearchImpl;
    private UUID uuid;
    @Mock
    ElasticsearchClusterConfiguration clusterConfiguration;
    @Mock
    Tracker tracker;
    @Mock
    DataSource dataSource;
    @Mock
    EnvironmentManager environmentManager;
    @Mock
    Connection connection;
    @Mock
    PreparedStatement preparedStatement;
    @Mock
    TrackerOperation trackerOperation;
    @Mock
    Environment environment;
    @Mock
    ContainerHost containerHost;
    @Mock
    Iterator<ContainerHost> iterator;
    @Mock
    Set<ContainerHost> mySet;
    @Mock
    CommandResult commandResult;
    @Mock
    ClusterSetupStrategy clusterSetupStrategy;
    @Mock
    PluginDAO pluginDAO;
    @Mock
    ResultSet resultSet;
    @Mock
    ResultSetMetaData resultSetMetaData;
    @Mock
    ExecutorService executor;

    @Before
    public void setUp() throws Exception
    {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any(String.class))).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
        when(resultSetMetaData.getColumnCount()).thenReturn(1);

        uuid = new UUID(50, 50);
        elasticsearchImpl = new ElasticsearchImpl(dataSource);
        elasticsearchImpl.setEnvironmentManager(environmentManager);
        elasticsearchImpl.setExecutor(executor);
        elasticsearchImpl.setDataSource(dataSource);
        elasticsearchImpl.setTracker(tracker);
        elasticsearchImpl.init();
        elasticsearchImpl.setPluginDAO(pluginDAO);

        // mock clusterOperationHandler
        when(tracker.createTrackerOperation(anyString(), anyString())).thenReturn(trackerOperation);
        when(clusterConfiguration.getClusterName()).thenReturn("test");
        when(trackerOperation.getId()).thenReturn(uuid);
        elasticsearchImpl.executor = executor;


        // assertions
        assertEquals(connection, dataSource.getConnection());
        assertEquals(preparedStatement, connection.prepareStatement(any(String.class)));

    }

    @Test
    public void testGetPluginDAO() throws Exception
    {
        elasticsearchImpl.getPluginDAO();

        // assertions
        assertNotNull(elasticsearchImpl.getPluginDAO());
        assertEquals(pluginDAO, elasticsearchImpl.getPluginDAO());
    }

    @Test
    public void testSetPluginDAO() throws Exception
    {
        elasticsearchImpl.setPluginDAO(pluginDAO);

        // assertions
        assertNotNull(elasticsearchImpl.getPluginDAO());

    }

    @Test
    public void testGetEnvironmentManager() throws Exception
    {
        elasticsearchImpl.getEnvironmentManager();

        // assertions
        assertNotNull(elasticsearchImpl.getEnvironmentManager());
        assertEquals(environmentManager, elasticsearchImpl.getEnvironmentManager());

    }

    @Test
    public void testSetEnvironmentManager() throws Exception
    {
        elasticsearchImpl.setEnvironmentManager(environmentManager);

        // assertions
        assertNotNull(elasticsearchImpl.getEnvironmentManager());
        assertEquals(environmentManager, elasticsearchImpl.getEnvironmentManager());

    }

    @Test
    public void testSetExecutor() throws Exception
    {
        elasticsearchImpl.setExecutor(executor);
    }

    @Test
    public void testSetDataSource() throws Exception
    {
        elasticsearchImpl.setDataSource(dataSource);
    }

    @Test
    public void testGetTracker() throws Exception
    {
        elasticsearchImpl.getTracker();

        // assertions
        assertNotNull(elasticsearchImpl.getTracker());
        assertEquals(tracker, elasticsearchImpl.getTracker());

    }

    @Test
    public void testSetTracker() throws Exception
    {
        elasticsearchImpl.setTracker(tracker);

        // assertions
        assertNotNull(elasticsearchImpl.getTracker());
        assertEquals(tracker, elasticsearchImpl.getTracker());

    }

    @Test
    public void testInit() throws Exception
    {
        elasticsearchImpl.init();
    }

    @Test
    public void testDestroy() throws Exception
    {
        elasticsearchImpl.destroy();
    }

    @Test
    public void testInstallCluster() throws Exception
    {
        UUID id = elasticsearchImpl.installCluster(clusterConfiguration);

        // assertions
        assertEquals(uuid, id);
    }

    @Test
    public void testUninstallCluster() throws Exception
    {
        UUID id = elasticsearchImpl.uninstallCluster(clusterConfiguration);

        // assertions
        assertEquals(uuid, id);
    }

    @Test
    public void testGetClusters() throws Exception
    {
        List<ElasticsearchClusterConfiguration> myList = new ArrayList<>();
        myList.add(clusterConfiguration);
        when(pluginDAO.getInfo(ElasticsearchClusterConfiguration.PRODUCT_KEY, ElasticsearchClusterConfiguration.class )).thenReturn(myList);

        elasticsearchImpl.getClusters();

        // assertions
        assertNotNull(elasticsearchImpl.getClusters());
        assertEquals(myList,elasticsearchImpl.getClusters());
    }

    @Test
    public void testGetCluster() throws Exception
    {
        when(pluginDAO.getInfo(ElasticsearchClusterConfiguration.PRODUCT_KEY, "test",
                ElasticsearchClusterConfiguration.class)).thenReturn(clusterConfiguration);
        elasticsearchImpl.getCluster("test");

        // assertions
        assertNotNull(elasticsearchImpl.getCluster("test"));
        assertEquals(clusterConfiguration,elasticsearchImpl.getCluster("test"));
    }

    @Test
    public void testStartAllNodes() throws Exception
    {
        UUID id = elasticsearchImpl.startAllNodes(clusterConfiguration);

        // assertions
        assertEquals(uuid, id);

    }

    @Test
    public void testCheckAllNodes() throws Exception
    {
        UUID id = elasticsearchImpl.checkAllNodes(clusterConfiguration);

        // assertions
        assertEquals(uuid, id);

    }

    @Test
    public void testStopAllNodes() throws Exception
    {
        UUID id = elasticsearchImpl.stopAllNodes(clusterConfiguration);

        // assertions
        assertEquals(uuid, id);

    }

    @Test
    public void testAddNode() throws Exception
    {
        elasticsearchImpl.addNode("test", "test");
    }

    @Test
    public void testCheckNode() throws Exception
    {
        UUID id = elasticsearchImpl.checkNode("test", "test");

        // assertions
        assertEquals(uuid, id);

    }

    @Test
    public void testStartNode() throws Exception
    {
        UUID id = elasticsearchImpl.startNode("test", "test");

        // assertions
        assertEquals(uuid, id);

    }

    @Test
    public void testStopNode() throws Exception
    {
        UUID id = elasticsearchImpl.stopNode("test", "test");

        // assertions
        assertEquals(uuid, id);

    }

    @Test
    public void testDestroyNode() throws Exception
    {
        elasticsearchImpl.destroyNode("test", "test");
    }

    @Test
    public void testUninstallCluster1() throws Exception
    {
        elasticsearchImpl.uninstallCluster("test");
    }

    @Test
    public void testGetClusterSetupStrategy() throws Exception
    {
        elasticsearchImpl.getClusterSetupStrategy(environment, clusterConfiguration, trackerOperation);
    }

    @Test
    public void testGetDefaultEnvironmentBlueprint() throws Exception
    {
        elasticsearchImpl.getDefaultEnvironmentBlueprint(clusterConfiguration);
    }

    @Test
    public void testConfigureEnvironmentCluster() throws Exception
    {
        elasticsearchImpl.configureEnvironmentCluster(clusterConfiguration);
    }
}