package org.safehaus.subutai.plugin.hbase.impl;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.common.PluginDAO;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hbase.api.HBaseConfig;
import org.safehaus.subutai.plugin.hbase.api.SetupType;

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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@Ignore
public class HBaseImplTest
{
    private HBaseImpl hBaseImpl;
    private UUID uuid;
    @Mock
    HBaseConfig hBaseConfig;
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
    RequestBuilder requestBuilder;
    @Mock
    CommandResult commandResult;
    @Mock
    Commands commands;
    @Mock
    HadoopClusterConfig hadoopClusterConfig;
    @Mock
    ClusterSetupStrategy clusterSetupStrategy;
    @Mock
    EnvironmentBlueprint environmentBlueprint;
    @Mock
    DataSource dataSource;
    @Mock
    AbstractOperationHandler abstractOperationHandler;
    @Mock
    ResultSet resultSet;
    @Mock
    PreparedStatement preparedStatement;
    @Mock
    Connection connection;
    @Mock
    ResultSetMetaData resultSetMetaData;
    @Mock
    PluginDAO pluginDAO;
    @Mock
    ExecutorService executorService;
    @Mock
    Hadoop hadoop;
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

        uuid = UUID.randomUUID();
        hBaseImpl = new HBaseImpl(dataSource);
        hBaseImpl.init();
        hBaseImpl.setPluginDAO(pluginDAO);
        hBaseImpl.setEnvironmentManager(environmentManager);
        hBaseImpl.setTracker(tracker);
        hBaseImpl.setExecutor(executorService);
        hBaseImpl.setHadoopManager(hadoop);
        hBaseImpl.setPluginDAO(pluginDAO);


        hBaseImpl.executor = executor;
        when(tracker.createTrackerOperation(anyString(), anyString())).thenReturn(trackerOperation);
        when(trackerOperation.getId()).thenReturn(uuid);

        when(pluginDAO.getInfo(HBaseConfig.PRODUCT_KEY, "test",
                HBaseConfig.class)).thenReturn(hBaseConfig);

        // assertions
        assertEquals(connection, dataSource.getConnection());
        verify(connection).prepareStatement(any(String.class));
        assertEquals(preparedStatement, connection.prepareStatement(any(String.class)));
    }

    @Test
    public void testGetPluginDAO() throws Exception
    {
        PluginDAO plug = hBaseImpl.getPluginDAO();

        // assertions
        assertNotNull(hBaseImpl.getPluginDAO());
        assertEquals(pluginDAO, plug);
    }

    @Test
    public void testSetPluginDAO() throws Exception
    {
        hBaseImpl.setPluginDAO(pluginDAO);
        hBaseImpl.getPluginDAO();

        // assertions
        assertEquals(pluginDAO, hBaseImpl.getPluginDAO());
    }

    @Test
    public void testGetTracker() throws Exception
    {
        Tracker track = hBaseImpl.getTracker();

        // assertions
        assertNotNull(hBaseImpl.getTracker());
        assertEquals(tracker, track);
    }

    @Test
    public void testSetTracker() throws Exception
    {
        hBaseImpl.setTracker(tracker);
        hBaseImpl.getTracker();

        // assertions
        assertEquals(tracker, hBaseImpl.getTracker());
    }

    @Test
    public void testGetExecutor() throws Exception
    {
        hBaseImpl.getExecutor();

        // assertions
        assertNotNull(hBaseImpl.getExecutor());
        assertEquals(executor, hBaseImpl.getExecutor());
    }

    @Test
    public void testSetExecutor() throws Exception
    {
        hBaseImpl.setExecutor(executorService);
        hBaseImpl.getExecutor();

        // assertions
        assertEquals(executorService, hBaseImpl.getExecutor());
    }

    @Test
    public void testGetEnvironmentManager() throws Exception
    {
        hBaseImpl.getEnvironmentManager();

        // assertions
        assertNotNull(hBaseImpl.getEnvironmentManager());
        assertEquals(environmentManager, hBaseImpl.getEnvironmentManager());

    }

    @Test
    public void testSetEnvironmentManager() throws Exception
    {
        hBaseImpl.setEnvironmentManager(environmentManager);
        hBaseImpl.getEnvironmentManager();

        // assertions
        assertEquals(environmentManager, hBaseImpl.getEnvironmentManager());

    }

    @Test
    public void testInit() throws Exception
    {
        hBaseImpl.init();
    }

    @Test
    public void testDestroy() throws Exception
    {
        hBaseImpl.destroy();
    }

    @Test
    public void testGetCommands() throws Exception
    {
        hBaseImpl.getCommands();

        // assertions
        assertNotNull(hBaseImpl.getCommands());
    }

    @Test
    public void testGetHadoopManager() throws Exception
    {
        hBaseImpl.getHadoopManager();

        // assertions
        assertNotNull(hBaseImpl.getHadoopManager());
        assertEquals(hadoop, hBaseImpl.getHadoopManager());
    }

    @Test
    public void testSetHadoopManager() throws Exception
    {
        hBaseImpl.setHadoopManager(hadoop);
        hBaseImpl.getHadoopManager();

        // assertions
        assertEquals(hadoop, hBaseImpl.getHadoopManager());
    }

    @Test
    public void testInstallCluster() throws Exception
    {
        UUID id = hBaseImpl.installCluster(hBaseConfig);

        // asserts
        verify(executor).execute(isA(AbstractOperationHandler.class));
        assertEquals(uuid, id);

    }

    @Test
    public void testDestroyNode() throws Exception
    {
        UUID id = hBaseImpl.destroyNode("test", "test");

        // asserts
        verify(executor).execute(isA(AbstractOperationHandler.class));
        assertEquals(uuid, id);

    }

    @Test
    public void testGetClusterSetupStrategy() throws Exception
    {
        // without hadoop
        hBaseImpl.getClusterSetupStrategy(trackerOperation, hBaseConfig, environment);
        // with hadoop
        when(hBaseConfig.getSetupType()).thenReturn(SetupType.OVER_HADOOP);
        hBaseImpl.getClusterSetupStrategy(trackerOperation, hBaseConfig, environment);
    }

    @Test
    public void testStopCluster() throws Exception
    {
        UUID id = hBaseImpl.stopCluster("test");

        // asserts
        assertEquals(uuid, id);
    }

    @Test
    public void testStartCluster() throws Exception
    {
        UUID id = hBaseImpl.startCluster("test");

        // asserts
        assertEquals(uuid, id);

    }

    @Test
    public void testUninstallCluster() throws Exception
    {
        UUID id = hBaseImpl.uninstallCluster("test");

        // asserts
        assertEquals(uuid, id);

    }

    @Test
    public void testGetClusters() throws Exception
    {
        List<HBaseConfig> myList = new ArrayList<>();
        myList.add(hBaseConfig);
        when(pluginDAO.getInfo(HBaseConfig.PRODUCT_KEY, HBaseConfig.class )).thenReturn(myList);


        hBaseImpl.getClusters();

        // assertions
        assertNotNull(hBaseImpl.getClusters());
        assertEquals(myList,hBaseImpl.getClusters());

    }

    @Test
    public void testGetCluster() throws Exception
    {
        hBaseImpl.getCluster("test");

        // assertions
        assertNotNull(hBaseImpl.getCluster("test"));
        assertEquals(hBaseConfig,hBaseImpl.getCluster("test"));

    }

    @Test
    public void testAddNode() throws Exception
    {
        UUID id = hBaseImpl.addNode("test", "test");

        // asserts
        assertEquals(uuid, id);

    }
}