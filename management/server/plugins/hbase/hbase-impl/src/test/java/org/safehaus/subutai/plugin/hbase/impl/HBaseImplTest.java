package org.safehaus.subutai.plugin.hbase.impl;

import com.sun.rowset.CachedRowSetImpl;
import org.junit.Before;
import org.junit.Test;
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

import javax.sql.DataSource;
import javax.sql.rowset.RowSetMetaDataImpl;
import java.sql.*;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class HBaseImplTest
{
    private CachedRowSetImpl cachedRowSet;
    private RowSetMetaDataImpl rowSetMetaData;
    private HBaseImpl hBaseImpl;
    private Tracker tracker;
    private TrackerOperation trackerOperation;
    private EnvironmentManager environmentManager;
    private Environment environment;
    private ContainerHost containerHost;
    private RequestBuilder requestBuilder;
    private CommandResult commandResult;
    private UUID uuid;
    private Commands commands;
    private HadoopClusterConfig hadoopClusterConfig;
    private ClusterSetupStrategy clusterSetupStrategy;
    private EnvironmentBlueprint environmentBlueprint;
    private DataSource dataSource;
    private AbstractOperationHandler abstractOperationHandler;
    private ResultSet resultSet;
    private PreparedStatement preparedStatement;
    private Connection connection;
    private ResultSetMetaData resultSetMetaData;
    private PluginDAO pluginDAO;
    private ExecutorService executorService;
    private Hadoop hadoop;
    private Clob clob;
    @Before
    public void setUp() throws Exception
    {
        cachedRowSet = mock(CachedRowSetImpl.class);
        rowSetMetaData = mock(RowSetMetaDataImpl.class);
        clob = mock(Clob.class);
        hadoop = mock(Hadoop.class);
        executorService = mock(ExecutorService.class);
        pluginDAO = mock(PluginDAO.class);
        clusterSetupStrategy = mock(ClusterSetupStrategy.class);
        environmentBlueprint = mock(EnvironmentBlueprint.class);
        resultSetMetaData = mock(ResultSetMetaData.class);
        preparedStatement = mock(PreparedStatement.class);
        connection = mock(Connection.class);
        hadoopClusterConfig = mock(HadoopClusterConfig.class);
        resultSet = mock(ResultSet.class);
        commands = mock(Commands.class);
        uuid = new UUID(50, 50);
        commandResult = mock(CommandResult.class);
        requestBuilder = mock(RequestBuilder.class);
        containerHost = mock(ContainerHost.class);
        environment = mock(Environment.class);
        environmentManager = mock(EnvironmentManager.class);
        trackerOperation = mock(TrackerOperation.class);
        tracker = mock(Tracker.class);
        hadoop = mock(Hadoop.class);
        dataSource = mock(DataSource.class);
        abstractOperationHandler = mock(AbstractOperationHandler.class);



        hBaseImpl = new HBaseImpl(dataSource);
    }

    @Test
    public void testGetPluginDAO() throws Exception
    {
        hBaseImpl.setPluginDAO(pluginDAO);
        hBaseImpl.getPluginDAO();
    }

    @Test
    public void testSetPluginDAO() throws Exception
    {
        hBaseImpl.setPluginDAO(pluginDAO);
    }

    @Test
    public void testGetTracker() throws Exception
    {
        hBaseImpl.setTracker(tracker);
        hBaseImpl.getTracker();
    }

    @Test
    public void testSetTracker() throws Exception
    {
        hBaseImpl.setTracker(tracker);
    }

    @Test
    public void testGetExecutor() throws Exception
    {
        hBaseImpl.setExecutor(executorService);
        hBaseImpl.getExecutor();
    }

    @Test
    public void testSetExecutor() throws Exception
    {
        hBaseImpl.setExecutor(executorService);
    }

    @Test
    public void testGetEnvironmentManager() throws Exception
    {
        hBaseImpl.setEnvironmentManager(environmentManager);
        hBaseImpl.getEnvironmentManager();
    }

    @Test
    public void testSetEnvironmentManager() throws Exception
    {
        hBaseImpl.setEnvironmentManager(environmentManager);
    }

    @Test
    public void testInit() throws Exception
    {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any(String.class))).thenReturn(preparedStatement);

        hBaseImpl.init();

        assertEquals(connection, dataSource.getConnection());
        verify(connection).prepareStatement(any(String.class));
        assertEquals(preparedStatement, connection.prepareStatement(any(String.class)));

    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowsNullPointerExceptionInMethodInit()
    {
        hBaseImpl.init();
    }

    @Test
    public void testDestroy() throws Exception
    {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any(String.class))).thenReturn(preparedStatement);
        hBaseImpl.init();

        hBaseImpl.destroy();
        assertEquals(connection, dataSource.getConnection());
        assertEquals(preparedStatement, connection.prepareStatement(any(String.class)));
    }

    @Test
    public void testGetCommands() throws Exception
    {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any(String.class))).thenReturn(preparedStatement);
        hBaseImpl.init();
        hBaseImpl.getCommands();

        assertNotNull(hBaseImpl.getCommands());
        assertEquals(connection, dataSource.getConnection());
        assertEquals(preparedStatement, connection.prepareStatement(any(String.class)));

    }

    @Test
    public void testGetHadoopManager() throws Exception
    {
        hBaseImpl.setHadoopManager(hadoop);
        hBaseImpl.getHadoopManager();
    }

    @Test
    public void testSetHadoopManager() throws Exception
    {
        hBaseImpl.setHadoopManager(hadoop);
    }

    @Test
    public void testInstallCluster() throws Exception
    {

    }

    @Test
    public void testDestroyNode() throws Exception
    {

    }

    @Test
    public void testGetClusterSetupStrategy() throws Exception
    {

    }

    @Test
    public void testStopCluster() throws Exception
    {

    }

    @Test
    public void testStartCluster() throws Exception
    {

    }

    @Test
    public void testCheckNode() throws Exception
    {

    }

    @Test
    public void testUninstallCluster() throws Exception
    {

    }

    @Test
    public void testGetClusters() throws Exception
    {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any(String.class))).thenReturn(preparedStatement);
        hBaseImpl.init();
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
        when(resultSetMetaData.getColumnCount()).thenReturn(1);
//        when(resultSet.next()).thenReturn(true).thenReturn(false);
        when(resultSet.getClob(anyString())).thenReturn(clob);
        when(cachedRowSet.getClob(anyString())).thenReturn(clob);

        hBaseImpl.getClusters();

    }

    @Test
    public void testGetCluster() throws Exception
    {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any(String.class))).thenReturn(preparedStatement);
        hBaseImpl.init();
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
        when(resultSetMetaData.getColumnCount()).thenReturn(1);
//        when(resultSet.next()).thenReturn(true).thenReturn(false);
        when(resultSet.getClob(anyString())).thenReturn(clob);
        when(cachedRowSet.getClob(anyString())).thenReturn(clob);

        hBaseImpl.getCluster("test");
//        assertNull(hBaseImpl.getCluster("test"));
    }

    @Test
    public void testAddNode() throws Exception
    {

    }
}