package org.safehaus.subutai.plugin.shark.impl;

import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.common.PluginDAO;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.spark.api.Spark;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class SharkImplTest {
    SharkImpl sharkImpl;
    Tracker tracker;
    EnvironmentManager environmentManager;
    Hadoop hadoop;
    Spark spark;
    DataSource dataSource;
    PluginDAO pluginDAO;

    @Before
    public void setUp() throws Exception {
        tracker = mock(Tracker.class);
        environmentManager = mock(EnvironmentManager.class);
        hadoop = mock(Hadoop.class);
        spark = mock(Spark.class);
        dataSource = mock(DataSource.class);
        sharkImpl = new SharkImpl(tracker, environmentManager, hadoop, spark, dataSource);
    }

    @Test
    public void testGetSparkManager() throws Exception {
        sharkImpl.getSparkManager();

        assertNotNull(sharkImpl.getSparkManager());
        assertEquals(spark, sharkImpl.getSparkManager());
    }

    @Test
    public void testGetHadoopManager() throws Exception {
        sharkImpl.getHadoopManager();

        assertNotNull(sharkImpl.getHadoopManager());
        assertEquals(hadoop, sharkImpl.getHadoopManager());
    }

    @Test
    public void testGetEnvironmentManager() throws Exception {
        sharkImpl.getEnvironmentManager();

        assertNotNull(sharkImpl.getEnvironmentManager());
        assertEquals(environmentManager, sharkImpl.getEnvironmentManager());
    }

    @Test
    public void testGetPluginDao() throws Exception {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        Connection connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any(String.class))).thenReturn(preparedStatement);
        sharkImpl.init();
        sharkImpl.getPluginDao();

        assertNotNull(sharkImpl.getPluginDao());
    }

    @Test
    public void testInit() throws Exception {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        Connection connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any(String.class))).thenReturn(preparedStatement);
        sharkImpl.init();

        assertEquals(connection, dataSource.getConnection());
        verify(connection).prepareStatement(any(String.class));
    }

    @Test
    public void testGetTracker() throws Exception {
        sharkImpl.getTracker();

        assertEquals(tracker, sharkImpl.getTracker());
        assertNotNull(sharkImpl.getTracker());
    }

    @Test
    public void testGetCommands() throws Exception {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        Connection connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any(String.class))).thenReturn(preparedStatement);
        sharkImpl.init();
        sharkImpl.getCommands();

        assertNotNull(sharkImpl.getCommands());
    }

    @Test
    public void testDestroy() throws Exception {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        Connection connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any(String.class))).thenReturn(preparedStatement);
        sharkImpl.init();

        sharkImpl.destroy();
    }

    @Test
    public void testInstallCluster() throws Exception {

    }

    @Test
    public void testUninstallCluster() throws Exception {

    }

    @Test
    public void testGetClusters() throws Exception {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        Connection connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any(String.class))).thenReturn(preparedStatement);
        sharkImpl.init();
        sharkImpl.getCluster("test");

    }

    @Test
    public void testGetCluster() throws Exception {

    }

    @Test
    public void testInstallCluster1() throws Exception {

    }

    @Test
    public void testAddNode() throws Exception {

    }

    @Test
    public void testDestroyNode() throws Exception {

    }

    @Test
    public void testActualizeMasterIP() throws Exception {

    }

    @Test
    public void testGetClusterSetupStrategy() throws Exception {

    }
}