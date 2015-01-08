package org.safehaus.subutai.plugin.hadoop.impl;


import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.common.PluginDAO;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;

import javax.sql.DataSource;
import java.sql.*;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HadoopImplTest
{
    private HadoopImpl hadoopImpl;
    private UUID uuid;
    @Mock
    DataSource dataSource;
    @Mock
    ExecutorService executorService;
    @Mock
    TrackerOperation trackerOperation;
    @Mock
    Tracker tracker;
    @Mock
    HadoopClusterConfig hadoopClusterConfig;
    @Mock
    Commands commands;
    @Mock
    PreparedStatement preparedStatement;
    @Mock
    Connection connection;
    @Mock
    EnvironmentManager environmentManager;
    @Mock
    Environment environment;
    @Mock
    ResultSetMetaData resultSetMetaData;
    @Mock
    ResultSet resultSet;
    @Mock
    PluginDAO pluginDAO;


    @Before
    public void setUp() throws SQLException
    {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any(String.class))).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
        when(resultSetMetaData.getColumnCount()).thenReturn(1);
        when(preparedStatement.executeUpdate()).thenReturn(5);


        hadoopImpl = new HadoopImpl(dataSource);
//        hadoopImpl.init();
        hadoopImpl.setExecutor(executorService);
        hadoopImpl.setTracker(tracker);
        hadoopImpl.setPluginDAO(pluginDAO);
        hadoopImpl.setEnvironmentManager(environmentManager);
        uuid = new UUID(50, 50);

        // mock ClusterOperationHandler
        when(trackerOperation.getId()).thenReturn(uuid);
        when(tracker.createTrackerOperation(anyString(), anyString())).thenReturn(trackerOperation);
        when(hadoopClusterConfig.getClusterName()).thenReturn("test");

        // assertions
        assertEquals(connection, dataSource.getConnection());
        assertEquals(5, preparedStatement.executeUpdate());
    }

    @Test
    public void testInit() throws SQLException
    {
        //hadoopImpl.init();
    }


    @Test
    public void testDestroy()
    {
        hadoopImpl.destroy();
    }


    @Test
    public void testGetTracker()
    {
        hadoopImpl.getTracker();

        // assertions
        assertEquals(tracker, hadoopImpl.getTracker());
        assertNotNull(hadoopImpl.getTracker());
    }


    @Test
    public void testSetTracker()
    {
        hadoopImpl.setTracker(tracker);

        // assertions
        assertEquals(tracker, hadoopImpl.getTracker());
    }

    @Test
    public void testGetExecutor()
    {
        hadoopImpl.getExecutor();

        // assertions
        assertEquals(executorService, hadoopImpl.getExecutor());
        assertNotNull(hadoopImpl.getExecutor());
    }


    @Test
    public void testSetExecutor()
    {
        hadoopImpl.setExecutor(executorService);
        hadoopImpl.getExecutor();

        // assertions
        assertEquals(executorService, hadoopImpl.getExecutor());
    }


    @Test
    public void testGetEnvironmentManager()
    {
        hadoopImpl.getEnvironmentManager();

        // assertions
        assertEquals(environmentManager, hadoopImpl.getEnvironmentManager());
        assertNotNull(hadoopImpl.getEnvironmentManager());

    }


    @Test
    public void testSetEnvironmentManager()
    {
        hadoopImpl.setEnvironmentManager(environmentManager);
        hadoopImpl.getEnvironmentManager();

        // assertions
        assertEquals(environmentManager, hadoopImpl.getEnvironmentManager());
    }


    @Test
    public void testGetPluginDAO()
    {
        hadoopImpl.getPluginDAO();

        // assertions
        assertNotNull(hadoopImpl.getPluginDAO());
    }


    @Test
    public void testInstallCluster()
    {
        hadoopImpl.installCluster(hadoopClusterConfig);

        assertNotNull(hadoopImpl.installCluster(hadoopClusterConfig));
        assertEquals(uuid, hadoopImpl.installCluster(hadoopClusterConfig));
    }


    @Test
    public void testUninstallCluster()
    {
        hadoopImpl.uninstallCluster(hadoopClusterConfig);

        assertNotNull(hadoopImpl.uninstallCluster(hadoopClusterConfig));
        assertEquals(uuid, hadoopImpl.uninstallCluster(hadoopClusterConfig));
    }


    @Test
    public void testStartNameNode()
    {
        hadoopImpl.startNameNode(hadoopClusterConfig);

        assertNotNull(hadoopImpl.startNameNode(hadoopClusterConfig));
        assertEquals(uuid, hadoopImpl.startNameNode(hadoopClusterConfig));
    }


    @Test
    public void testStopNameNode()
    {
        hadoopImpl.stopNameNode(hadoopClusterConfig);

        assertNotNull(hadoopImpl.stopNameNode(hadoopClusterConfig));
        assertEquals(uuid, hadoopImpl.stopNameNode(hadoopClusterConfig));
    }


    @Test
    public void testStatusNameNode()
    {
        hadoopImpl.statusNameNode(hadoopClusterConfig);

        assertNotNull(hadoopImpl.statusNameNode(hadoopClusterConfig));
        assertEquals(uuid, hadoopImpl.statusNameNode(hadoopClusterConfig));
    }


    @Test
    public void testStatusSecondaryNameNode()
    {
        hadoopImpl.statusSecondaryNameNode(hadoopClusterConfig);

        assertNotNull(hadoopImpl.statusSecondaryNameNode(hadoopClusterConfig));
        assertEquals(uuid, hadoopImpl.statusSecondaryNameNode(hadoopClusterConfig));
    }


    @Test
    public void testStartDataNode()
    {
        String hostname = "test";
        hadoopImpl.startDataNode(hadoopClusterConfig, hostname);

        assertNotNull(hadoopImpl.startDataNode(hadoopClusterConfig, hostname));
        assertEquals(uuid, hadoopImpl.startDataNode(hadoopClusterConfig, hostname));
    }


    @Test
    public void testStopDataNode()
    {
        String hostname = "test";
        hadoopImpl.stopDataNode(hadoopClusterConfig, hostname);

        assertNotNull(hadoopImpl.stopDataNode(hadoopClusterConfig, hostname));
        assertEquals(uuid, hadoopImpl.stopDataNode(hadoopClusterConfig, hostname));
    }


    @Test
    public void testStatusDataNode()
    {
        String hostname = "test";
        hadoopImpl.statusDataNode(hadoopClusterConfig, hostname);

        assertNotNull(hadoopImpl.statusDataNode(hadoopClusterConfig, hostname));
        assertEquals(uuid, hadoopImpl.statusDataNode(hadoopClusterConfig, hostname));
    }


    @Test
    public void testStartJobTracker()
    {
        hadoopImpl.startJobTracker(hadoopClusterConfig);

        assertNotNull(hadoopImpl.startJobTracker(hadoopClusterConfig));
        assertEquals(uuid, hadoopImpl.startJobTracker(hadoopClusterConfig));
    }


    @Test
    public void testStopJobTracker()
    {
        hadoopImpl.stopJobTracker(hadoopClusterConfig);

        assertNotNull(hadoopImpl.stopJobTracker(hadoopClusterConfig));
        assertEquals(uuid, hadoopImpl.stopJobTracker(hadoopClusterConfig));
    }


    @Test
    public void testStatusJobTracker()
    {
        hadoopImpl.statusJobTracker(hadoopClusterConfig);

        assertNotNull(hadoopImpl.statusJobTracker(hadoopClusterConfig));
        assertEquals(uuid, hadoopImpl.statusJobTracker(hadoopClusterConfig));
    }


    @Test
    public void testStartTaskTracker()
    {
        String hostname = "test";
        hadoopImpl.startTaskTracker(hadoopClusterConfig, hostname);

        assertNotNull(hadoopImpl.startTaskTracker(hadoopClusterConfig, hostname));
        assertEquals(uuid, hadoopImpl.startTaskTracker(hadoopClusterConfig, hostname));
    }


    @Test
    public void testStopTaskTracker()
    {
        String hostname = "test";
        hadoopImpl.stopTaskTracker(hadoopClusterConfig, hostname);

        assertNotNull(hadoopImpl.stopTaskTracker(hadoopClusterConfig, hostname));
        assertEquals(uuid, hadoopImpl.stopTaskTracker(hadoopClusterConfig, hostname));
    }


    @Test
    public void testStatusTaskTracker()
    {
        String hostname = "test";
        hadoopImpl.statusTaskTracker(hadoopClusterConfig, hostname);

        assertNotNull(hadoopImpl.statusTaskTracker(hadoopClusterConfig, hostname));
        assertEquals(uuid, hadoopImpl.statusTaskTracker(hadoopClusterConfig, hostname));
    }


    @Ignore
    @Test
    public void testAddNode1()
    {
        String clusterName = "test";
        hadoopImpl.addNode(clusterName, 5);

        assertNotNull(hadoopImpl.addNode(clusterName, 5));
        assertEquals(uuid, hadoopImpl.addNode(clusterName, 5));
    }


    @Ignore
    @Test
    public void testDestroyNode()
    {
        String hostname = "test";
        hadoopImpl.destroyNode(hadoopClusterConfig, hostname);

        assertNotNull(hadoopImpl.destroyNode(hadoopClusterConfig, hostname));
        assertEquals(uuid, hadoopImpl.destroyNode(hadoopClusterConfig, hostname));
    }


    @Test
    public void testCheckDecomissionStatus()
    {
        hadoopImpl.checkDecomissionStatus(hadoopClusterConfig);

        assertNotNull(hadoopImpl.checkDecomissionStatus(hadoopClusterConfig));
        assertEquals(uuid, hadoopImpl.checkDecomissionStatus(hadoopClusterConfig));
    }


    @Test
    public void testExcludeNode()
    {
        String hostname = "test";
        hadoopImpl.excludeNode(hadoopClusterConfig, hostname);

        assertNotNull(hadoopImpl.excludeNode(hadoopClusterConfig, hostname));
        assertEquals(uuid, hadoopImpl.excludeNode(hadoopClusterConfig, hostname));
    }


    @Test
    public void testIncludeNode()
    {
        String hostname = "test";
        hadoopImpl.includeNode(hadoopClusterConfig, hostname);

        assertNotNull(hadoopImpl.includeNode(hadoopClusterConfig, hostname));
        assertEquals(uuid, hadoopImpl.includeNode(hadoopClusterConfig, hostname));
    }


    @Test
    public void testGetDefaultEnvironmentBlueprint() throws ClusterSetupException
    {
        hadoopImpl.getDefaultEnvironmentBlueprint(hadoopClusterConfig);

        assertNotNull(hadoopImpl.getDefaultEnvironmentBlueprint(hadoopClusterConfig));
    }

    @Test
    public void testGetClusterSetupStrategy()
    {
        ClusterSetupStrategy clusterStrategy = hadoopImpl.getClusterSetupStrategy(environment, hadoopClusterConfig,
                trackerOperation);

        // assertions
        assertNotNull(clusterStrategy);

    }

    @Test
    public void testGetClusters()
    {
        hadoopImpl.getClusters();

        // assertions
        assertNotNull(hadoopImpl.getClusters());
    }

    @Test
    public void testGetCluster()
    {
        hadoopImpl.getCluster("test");
    }

    @Test
    public void testAddNode()
    {
        hadoopImpl.addNode("test","test");
    }

    @Ignore
    @Test
    public void testUninstallCluster1()
    {
        hadoopImpl.uninstallCluster("test");
    }


}