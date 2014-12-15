package org.safehaus.subutai.plugin.accumulo.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.accumulo.api.AccumuloClusterConfig;
import org.safehaus.subutai.plugin.common.PluginDAO;
import org.safehaus.subutai.plugin.common.api.NodeType;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.api.Zookeeper;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AccumuloImplTest
{
    private AccumuloImpl accumuloImpl;
    private UUID uuid;
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
    DataSource dataSource;
    @Mock
    PluginDAO pluginDAO;
    @Mock
    ExecutorService executor;
    @Mock
    Commands commands;
    @Mock
    Hadoop hadoop;
    @Mock
    Zookeeper zookeeper;
    @Mock
    Connection connection;
    @Mock
    PreparedStatement preparedStatement;
    @Mock
    ResultSet resultSet;
    @Mock
    ResultSetMetaData resultSetMetaData;
    @Mock
    HadoopClusterConfig hadoopClusterConfig;
    @Mock
    ZookeeperClusterConfig zookeeperClusterConfig;

    @Before
    public void setUp() throws Exception
    {
        // mock init
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any(String.class))).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
        when(resultSetMetaData.getColumnCount()).thenReturn(1);

        uuid = new UUID(50, 50);
        accumuloImpl = new AccumuloImpl(dataSource);
//        accumuloImpl.init();
        accumuloImpl.setExecutor(executor);
        accumuloImpl.setEnvironmentManager(environmentManager);
        accumuloImpl.setHadoopManager(hadoop);
        accumuloImpl.setTracker(tracker);
        accumuloImpl.setZkManager(zookeeper);
        accumuloImpl.setPluginDAO(pluginDAO);

        // mock InstallClusterHandler
        when(tracker.createTrackerOperation(anyString(), anyString())).thenReturn(trackerOperation);
        when(trackerOperation.getId()).thenReturn(uuid);

        when(hadoop.getCluster(anyString())).thenReturn(hadoopClusterConfig);
        when(zookeeper.getCluster(anyString())).thenReturn(zookeeperClusterConfig);
        when(pluginDAO.getInfo(AccumuloClusterConfig.PRODUCT_KEY, "test", AccumuloClusterConfig.class)).thenReturn
                (accumuloClusterConfig);

        // asserts
        assertEquals(connection, dataSource.getConnection());
        assertEquals(preparedStatement, connection.prepareStatement(any(String.class)));
        assertEquals(resultSet, preparedStatement.executeQuery());
        assertEquals(resultSetMetaData, resultSet.getMetaData());
        assertNotNull(resultSetMetaData.getColumnCount());

    }

    @Test
    public void testGetPluginDAO() throws Exception
    {
        accumuloImpl.getPluginDAO();

        // assertions
        assertNotNull(accumuloImpl.getPluginDAO());

    }

    @Test
    public void testGetExecutor() throws Exception
    {
        accumuloImpl.getExecutor();

        // assertions
        assertNotNull(accumuloImpl.getExecutor());
        assertEquals(executor, accumuloImpl.getExecutor());

    }

    @Test
    public void testGetEnvironmentManager() throws Exception
    {
        accumuloImpl.getEnvironmentManager();

        // assertions
        assertNotNull(accumuloImpl.getEnvironmentManager());
        assertEquals(environmentManager, accumuloImpl.getEnvironmentManager());

    }

    @Test
    public void testGetCommands() throws Exception
    {
        accumuloImpl.getCommands();

    }

    @Test
    public void testGetTracker() throws Exception
    {
        accumuloImpl.getTracker();

        // assertions
        assertNotNull(accumuloImpl.getTracker());
        assertEquals(tracker, accumuloImpl.getTracker());

    }

    @Test
    public void testGetHadoopManager() throws Exception
    {
        accumuloImpl.getHadoopManager();

        // assertions
        assertNotNull(accumuloImpl.getHadoopManager());
        assertEquals(hadoop, accumuloImpl.getHadoopManager());

    }


    @Test
    public void testInit() throws Exception
    {
//        accumuloImpl.init();
    }

    @Test
    public void testDestroy() throws Exception
    {
        accumuloImpl.destroy();
    }

    @Test
    public void testInstallCluster() throws Exception
    {
        UUID id = accumuloImpl.installCluster(accumuloClusterConfig);

        // assertions
        assertNotNull(accumuloImpl.installCluster(accumuloClusterConfig));
        assertEquals(uuid, id);
    }

    @Test
    public void testUninstallCluster() throws Exception
    {
        UUID id = accumuloImpl.uninstallCluster("test");

        // assertions
        assertNotNull(accumuloImpl.uninstallCluster("test"));
        assertEquals(uuid, id);

    }

    @Test
    public void testGetClusters() throws Exception
    {
        List<AccumuloClusterConfig> myList = new ArrayList<>();
        myList.add(accumuloClusterConfig);
        when(pluginDAO.getInfo(AccumuloClusterConfig.PRODUCT_KEY, AccumuloClusterConfig.class)).thenReturn(myList);


        accumuloImpl.getClusters();

        // assertions
        assertNotNull(accumuloImpl.getClusters());
        assertEquals(myList, accumuloImpl.getClusters());

    }

    @Test
    public void testGetCluster() throws Exception
    {
        accumuloImpl.getCluster("test");

        // assertions
        assertNotNull(accumuloImpl.getCluster("test"));
        assertEquals(accumuloClusterConfig, accumuloImpl.getCluster("test"));
    }

    @Test
    public void testAddNode() throws Exception
    {
        UUID id = accumuloImpl.addNode("test", "test");
    }

    @Test
    public void testStartCluster() throws Exception
    {
        UUID id = accumuloImpl.startCluster("test");

        // assertions
        assertNotNull(accumuloImpl.startCluster("test"));
        assertEquals(uuid, id);

    }

    @Test
    public void testStopCluster() throws Exception
    {
        UUID id = accumuloImpl.stopCluster("test");

        // assertions
        assertNotNull(accumuloImpl.stopCluster("test"));
        assertEquals(uuid, id);

    }

    @Test
    public void testCheckNode() throws Exception
    {
        UUID id = accumuloImpl.checkNode("test", "test");

        // assertions
        assertEquals(uuid, id);
    }

    @Test
    public void testAddNode1() throws Exception
    {
        UUID id = accumuloImpl.addNode("test", "test", NodeType.MASTER_NODE);

        // assertions
        verify(executor).execute(isA(AbstractOperationHandler.class));
        assertEquals(uuid, id);

    }

    @Test
    public void testDestroyNode() throws Exception
    {
        UUID id = accumuloImpl.destroyNode("test", "test", NodeType.MASTER_NODE);

        // assertions
        verify(executor).execute(isA(AbstractOperationHandler.class));
        assertEquals(uuid, id);

    }

    @Test
    public void testAddProperty() throws Exception
    {
        UUID id = accumuloImpl.addProperty("test", "test", "test");

        // assertions
        verify(executor).execute(isA(AbstractOperationHandler.class));
        assertEquals(uuid, id);

    }

    @Test
    public void testRemoveProperty() throws Exception
    {
        UUID id = accumuloImpl.removeProperty("test", "test");

        // assertions
        verify(executor).execute(isA(AbstractOperationHandler.class));
        assertEquals(uuid, id);

    }

    @Test
    public void testGetDefaultEnvironmentBlueprint() throws Exception
    {
        accumuloImpl.getDefaultEnvironmentBlueprint(accumuloClusterConfig);

        // assertions
        assertNotNull(accumuloImpl.getDefaultEnvironmentBlueprint(accumuloClusterConfig));

    }
}