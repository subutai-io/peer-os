package org.safehaus.subutai.plugin.shark.impl;

import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.common.util.DbUtil;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.common.PluginDAO;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.shark.api.SharkClusterConfig;
import org.safehaus.subutai.plugin.spark.api.Spark;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class SharkImplTest {
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
    private DataSource dataSource;
    private AbstractOperationHandler abstractOperationHandler;
    private DbUtil dbUtil;
    private ResultSet resultSet;
    @Before
    public void setUp() throws Exception {
        resultSet = mock(ResultSet.class);
        dbUtil = mock(DbUtil.class);
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
        sharkClusterConfig = mock(SharkClusterConfig.class);
        hadoop = mock(Hadoop.class);
        dataSource = mock(DataSource.class);
        abstractOperationHandler = mock(AbstractOperationHandler.class);

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

//    @Test
//    public void testGetPluginDao() throws Exception {
//        PreparedStatement preparedStatement = mock(PreparedStatement.class);
//        Connection connection = mock(Connection.class);
//        when(dataSource.getConnection()).thenReturn(connection);
//        when(connection.prepareStatement(any(String.class))).thenReturn(preparedStatement);
//        sharkImpl.init();
//        sharkImpl.getPluginDao();
//
//        assertNotNull(sharkImpl.getPluginDao());
//    }

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

//    @Test
//    public void testGetCommands() throws Exception {
//        PreparedStatement preparedStatement = mock(PreparedStatement.class);
//        Connection connection = mock(Connection.class);
//        when(dataSource.getConnection()).thenReturn(connection);
//        when(connection.prepareStatement(any(String.class))).thenReturn(preparedStatement);
//        sharkImpl.init();
//        sharkImpl.getCommands();
//
//        assertNotNull(sharkImpl.getCommands());
//    }

//    @Test
//    public void testDestroy() throws Exception {
//        PreparedStatement preparedStatement = mock(PreparedStatement.class);
//        Connection connection = mock(Connection.class);
//        when(dataSource.getConnection()).thenReturn(connection);
//        when(connection.prepareStatement(any(String.class))).thenReturn(preparedStatement);
//        sharkImpl.init();
//
//        sharkImpl.destroy();
//    }

    @Test
    public void testInstallCluster() throws Exception {
//        PreparedStatement preparedStatement = mock(PreparedStatement.class);
//        Connection connection = mock(Connection.class);
//        when(dataSource.getConnection()).thenReturn(connection);
//        when(connection.prepareStatement(any(String.class))).thenReturn(preparedStatement);
////        when(tracker.createTrackerOperation())
//        sharkImpl.init();
////        when(abstractOperationHandler.getTrackerId()).thenReturn()
//        when(trackerOperation.getId()) .thenReturn(uuid);
//
////        sharkImpl.installCluster(sharkClusterConfig);

    }

    @Test
    public void testUninstallCluster() throws Exception {

    }

    @Test
    public void testGetClusters() throws Exception {
//        PreparedStatement preparedStatement = mock(PreparedStatement.class);
//        Connection connection = mock(Connection.class);
//        when(dataSource.getConnection()).thenReturn(connection);
//        when(connection.prepareStatement(any(String.class))).thenReturn(preparedStatement);
//        sharkImpl.init();
//        sharkImpl.getCluster("test");

    }

    @Test
    public void testGetCluster() throws Exception {
        when(dbUtil.select(anyString(),anyString(),anyString())).thenReturn(resultSet);

        //sharkImpl.getCluster("test");


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
        sharkImpl.getClusterSetupStrategy(trackerOperation,sharkClusterConfig,environment);

        assertNotNull(sharkImpl.getClusterSetupStrategy(trackerOperation,sharkClusterConfig,environment));
    }
}