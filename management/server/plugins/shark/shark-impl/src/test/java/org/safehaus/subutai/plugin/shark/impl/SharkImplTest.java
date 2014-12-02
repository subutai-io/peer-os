package org.safehaus.subutai.plugin.shark.impl;

import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.exception.ClusterException;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.shark.api.SharkClusterConfig;
import org.safehaus.subutai.plugin.spark.api.Spark;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class SharkImplTest
{
    private SharkImpl sharkImpl;
    private SharkClusterConfig sharkClusterConfig;
    private SharkClusterConfig sharkClusterConfig2;
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
    private HadoopClusterConfig hadoopClusterConfig;
    private ClusterSetupStrategy clusterSetupStrategy;
    private Hadoop hadoop;
    private EnvironmentBlueprint environmentBlueprint;
    private DataSource dataSource;
    private AbstractOperationHandler abstractOperationHandler;
    private ResultSet resultSet;
    private SetupStrategyOverSpark setupStrategyOverSpark;
    private PreparedStatement preparedStatement;
    private Connection connection;
    private ResultSetMetaData resultSetMetaData;


    @Before
    public void setUp() throws Exception
    {
        clusterSetupStrategy = mock(ClusterSetupStrategy.class);
        environmentBlueprint = mock(EnvironmentBlueprint.class);
        resultSetMetaData = mock(ResultSetMetaData.class);
        preparedStatement = mock(PreparedStatement.class);
        connection = mock(Connection.class);
        hadoopClusterConfig = mock(HadoopClusterConfig.class);
        setupStrategyOverSpark = mock(SetupStrategyOverSpark.class);
        resultSet = mock(ResultSet.class);
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
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any(String.class))).thenReturn(preparedStatement);
        sharkImpl.init();

        sharkImpl.getPluginDao();

        assertNotNull(sharkImpl.getPluginDao());
        assertEquals(connection, dataSource.getConnection());
        assertEquals(preparedStatement, connection.prepareStatement(any(String.class)));
    }
    @Test
    public void testInit() throws SQLException
    {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any(String.class))).thenReturn(preparedStatement);

        sharkImpl.init();

        assertEquals(connection, dataSource.getConnection());
        verify(connection).prepareStatement(any(String.class));
        assertEquals(preparedStatement, connection.prepareStatement(any(String.class)));
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
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any(String.class))).thenReturn(preparedStatement);
        sharkImpl.init();
        sharkImpl.getCommands();

        assertNotNull(sharkImpl.getCommands());
        assertEquals(connection, dataSource.getConnection());
        assertEquals(preparedStatement, connection.prepareStatement(any(String.class)));
    }

    @Test
    public void testDestroy() throws SQLException
    {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any(String.class))).thenReturn(preparedStatement);
        sharkImpl.init();

        sharkImpl.destroy();
        assertEquals(connection, dataSource.getConnection());
        assertEquals(preparedStatement, connection.prepareStatement(any(String.class)));
    }

    @Test
    public void testInstallCluster() throws SQLException, CommandException, ClusterException, ClusterSetupException
    {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any(String.class))).thenReturn(preparedStatement);
        when(tracker.createTrackerOperation(anyString(), anyString())).thenReturn(trackerOperation);
        sharkImpl.init();
        when(abstractOperationHandler.getTrackerId()).thenReturn(uuid);
        when(trackerOperation.getId()).thenReturn(uuid);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
        when(resultSetMetaData.getColumnCount()).thenReturn(5);
        when(resultSetMetaData.getColumnName(anyInt())).thenReturn("test");

        // mock setup method
        sharkImpl.getSparkManager();
        when(spark.getCluster(anyString())).thenReturn(sparkClusterConfig);
        sharkImpl.getEnvironmentManager();
        when(environmentManager.getEnvironmentByUUID(any(UUID.class))).thenReturn(environment);
        sharkImpl.getSparkManager();
        Set<ContainerHost> mySet = mock(Set.class);
        ContainerHost[] arr = new ContainerHost[1];
        arr[0] = containerHost;
        when(environment.getContainerHostsByIds( any( Set.class ) )).thenReturn(mySet);
        Iterator<ContainerHost> iterator = mock(Iterator.class);
        when(mySet.iterator()).thenReturn(iterator);
        when(iterator.hasNext()).thenReturn(true).thenReturn(false);
        when(iterator.next()).thenReturn(containerHost);
        when(mySet.size()).thenReturn(1);
        when(containerHost.isConnected()).thenReturn(true);
        when(environment.getContainerHostById( any( UUID.class ) )).thenReturn(containerHost);
        when(mySet.toArray()).thenReturn(arr);
        sharkImpl.getCommands();
        when(commands.getCheckInstalledCommand()).thenReturn(requestBuilder);
        when(containerHost.execute(requestBuilder)).thenReturn(commandResult);
        when(commandResult.hasSucceeded()).thenReturn(true);
        setupStrategyOverSpark.executeCommand(containerHost, requestBuilder);
        when(commandResult.getStdOut()).thenReturn("test");
        sharkImpl.getCommands();
        when(commands.getCheckInstalledCommand()).thenReturn(requestBuilder);
        when(containerHost.execute(any(RequestBuilder.class))).thenReturn(commandResult);
        when(commandResult.hasSucceeded()).thenReturn(true);

        setupStrategyOverSpark.executeCommand(containerHost, requestBuilder);
        when(commandResult.getStdOut()).thenReturn("test");
        when(commands.getSetMasterIPCommand(containerHost)).thenReturn(requestBuilder);
        when(preparedStatement.executeUpdate()).thenReturn(2);
        when(environment.getId()).thenReturn(uuid);
        sharkClusterConfig2 = new SharkClusterConfig();
        sharkClusterConfig2.setClusterName("test");
        sharkClusterConfig2.setEnvironmentId(uuid);
        sharkClusterConfig2.setSparkClusterName("test");

        sharkImpl.installCluster(sharkClusterConfig2);

        assertEquals(trackerOperation, tracker.createTrackerOperation(anyString(), anyString()));
        assertNotNull(sharkImpl.getSparkManager());
        assertNotNull(sharkImpl.getEnvironmentManager());
        assertNotNull(sharkImpl.getClusterSetupStrategy(trackerOperation,sharkClusterConfig2,environment));
    }

    @Test
    public void testGetClusters() throws SQLException
    {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any(String.class))).thenReturn(preparedStatement);
        sharkImpl.init();
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
        when(resultSetMetaData.getColumnCount()).thenReturn(1);

        sharkImpl.getClusters();

        assertEquals(connection, dataSource.getConnection());
        assertEquals(preparedStatement, connection.prepareStatement(any(String.class)));
        assertEquals(resultSet,preparedStatement.executeQuery());
        assertEquals(resultSetMetaData,resultSet.getMetaData());
        assertNotNull(resultSetMetaData.getColumnCount());
    }

    @Test
    public void testGetCluster() throws SQLException
    {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any(String.class))).thenReturn(preparedStatement);
        sharkImpl.init();
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
        when(resultSetMetaData.getColumnCount()).thenReturn(1);

        sharkImpl.getCluster("test");

        assertEquals(connection, dataSource.getConnection());
        assertEquals(preparedStatement, connection.prepareStatement(any(String.class)));
        assertEquals(resultSet,preparedStatement.executeQuery());
        assertEquals(resultSetMetaData,resultSet.getMetaData());
        assertNotNull(resultSetMetaData.getColumnCount());
    }

    @Test
    public void testGetClusterSetupStrategy()
    {
        sharkImpl.getClusterSetupStrategy(trackerOperation, sharkClusterConfig, environment);

        assertNotNull(sharkImpl.getClusterSetupStrategy(trackerOperation, sharkClusterConfig, environment));
    }
}