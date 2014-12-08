package org.safehaus.subutai.plugin.shark.impl;

import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.exception.ClusterException;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.shark.api.SharkClusterConfig;
import org.safehaus.subutai.plugin.spark.api.Spark;

import javax.sql.DataSource;
import java.sql.*;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class SharkImplTest
{
    private SharkImpl sharkImpl;
    private SharkClusterConfig sharkClusterConfig;
    private Tracker tracker;
    private TrackerOperation trackerOperation;
    private EnvironmentManager environmentManager;
    private Environment environment;
    private ExecutorService executor;
    private UUID uuid;
    private Spark spark;
    private Hadoop hadoop;
    private PreparedStatement preparedStatement;
    private Connection connection;
    private ResultSetMetaData resultSetMetaData;
    private DataSource dataSource;
    private ResultSet resultSet;
    @Before
    public void setUp() throws Exception
    {
        resultSet = mock(ResultSet.class);
        dataSource = mock(DataSource.class);
        executor = mock(ExecutorService.class);
        resultSetMetaData = mock(ResultSetMetaData.class);
        preparedStatement = mock(PreparedStatement.class);
        connection = mock(Connection.class);
        spark = mock(Spark.class);
        environment = mock(Environment.class);
        environmentManager = mock(EnvironmentManager.class);
        trackerOperation = mock(TrackerOperation.class);
        tracker = mock(Tracker.class);
        sharkClusterConfig = mock(SharkClusterConfig.class);
        hadoop = mock(Hadoop.class);
        dataSource = mock(DataSource.class);

        // mock init
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any(String.class))).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
        when(resultSetMetaData.getColumnCount()).thenReturn(1);


        uuid = new UUID(50, 50);
        sharkImpl = new SharkImpl(tracker, environmentManager, hadoop, spark, dataSource);
        sharkImpl.init();

        // mock InstallClusterHandler
        when(tracker.createTrackerOperation(anyString(), anyString())).thenReturn(trackerOperation);
        when(trackerOperation.getId()).thenReturn(uuid);


        // asserts
        assertEquals(connection, dataSource.getConnection());
        assertEquals(preparedStatement, connection.prepareStatement(any(String.class)));
        assertEquals(resultSet,preparedStatement.executeQuery());
        assertEquals(resultSetMetaData,resultSet.getMetaData());
        assertNotNull(resultSetMetaData.getColumnCount());

    }

    @Test
    public void testGetSparkManager()
    {
        sharkImpl.getSparkManager();

        assertNotNull(sharkImpl.getSparkManager());
        assertEquals(spark, sharkImpl.getSparkManager());
    }

    @Test
    public void testGetHadoopManager()
    {
        sharkImpl.getHadoopManager();

        assertNotNull(sharkImpl.getHadoopManager());
        assertEquals(hadoop, sharkImpl.getHadoopManager());
    }

    @Test
    public void testGetEnvironmentManager()
    {
        sharkImpl.getEnvironmentManager();

        assertNotNull(sharkImpl.getEnvironmentManager());
        assertEquals(environmentManager, sharkImpl.getEnvironmentManager());
    }

    @Test
    public void testGetPluginDao() throws SQLException
    {
        sharkImpl.getPluginDao();

        assertNotNull(sharkImpl.getPluginDao());
    }
    @Test
    public void testInit() throws SQLException
    {
        sharkImpl.init();
    }

    @Test
    public void testGetTracker()
    {
        sharkImpl.getTracker();

        assertEquals(tracker, sharkImpl.getTracker());
        assertNotNull(sharkImpl.getTracker());
    }

    @Test
    public void testGetCommands() throws SQLException
    {
        sharkImpl.getCommands();

        assertNotNull(sharkImpl.getCommands());
    }

    @Test
    public void testDestroy() throws SQLException
    {
        sharkImpl.destroy();
    }

    @Test
    public void testInstallCluster() throws SQLException, CommandException, ClusterException, ClusterSetupException
    {
        sharkImpl.executor = executor;

        UUID id = sharkImpl.installCluster(sharkClusterConfig);

        // asserts
        verify(executor).execute(isA(AbstractOperationHandler.class));
        assertEquals(uuid, id);
    }

    @Test
    public void testUninstallCluster()
    {
//        sharkImpl.executor = executor;
//
//        UUID id = sharkImpl.uninstallCluster("test");
//
//        // asserts
//        verify(executor).execute(isA(AbstractOperationHandler.class));
//        assertEquals(uuid, id);

    }


    @Test
    public void testGetClusters() throws SQLException
    {
        sharkImpl.getClusters();
    }

    @Test
    public void testGetCluster() throws SQLException
    {
        sharkImpl.getCluster("test");
    }

    @Test
    public void testGetClusterSetupStrategy()
    {
        sharkImpl.getClusterSetupStrategy(trackerOperation, sharkClusterConfig, environment);

        assertNotNull(sharkImpl.getClusterSetupStrategy(trackerOperation, sharkClusterConfig, environment));
    }
}