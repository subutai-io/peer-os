package org.safehaus.subutai.plugin.hbase.impl;

import com.sun.rowset.CachedRowSetImpl;
import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.exception.ClusterSetupException;
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

import javax.sql.DataSource;
import javax.sql.rowset.RowSetMetaDataImpl;
import java.sql.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class OverHadoopSetupStrategyTest
{
    private OverHadoopSetupStrategy overHadoopSetupStrategy;
    private CachedRowSetImpl cachedRowSet;
    private RowSetMetaDataImpl rowSetMetaData;
    private HBaseImpl hBaseImpl;
    private HBaseConfig hBaseConfig;
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
        hBaseImpl = mock(HBaseImpl.class);
        hBaseConfig = mock(HBaseConfig.class);
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


        overHadoopSetupStrategy = new OverHadoopSetupStrategy(hBaseImpl,hBaseConfig,environment,trackerOperation);
    }

    @Test
    public void testSetup() throws Exception
    {
        // mock setup method
        Set<UUID> myUUID = new HashSet<>();
        myUUID.add(uuid);
        Set<ContainerHost> mySet = mock(Set.class);
        mySet.add(containerHost);
        when(containerHost.getId()).thenReturn(uuid);
        ContainerHost[] arr = new ContainerHost[1];
        arr[0] = containerHost;
        when(environment.getContainerHostsByIds( any( Set.class ) )).thenReturn(mySet).thenReturn(mySet).thenReturn(mySet);
        Iterator<ContainerHost> iterator = mock(Iterator.class);
        when(mySet.iterator()).thenReturn(iterator).thenReturn(iterator).thenReturn(iterator);
        when(iterator.hasNext()).thenReturn(true).thenReturn(false).thenReturn(true).thenReturn(false).thenReturn(true).thenReturn(false);
        when(iterator.next()).thenReturn(containerHost);
        when(mySet.size()).thenReturn(1);
        when(containerHost.isConnected()).thenReturn(true);
        when(environment.getContainerHostById( any( UUID.class ) )).thenReturn(containerHost);
        when(mySet.toArray()).thenReturn(arr);
        when(hBaseConfig.getAllNodes()).thenReturn(myUUID);
        when(containerHost.execute(any(RequestBuilder.class))).thenReturn(commandResult);
        when(commandResult.hasSucceeded()).thenReturn(true);
        when(commandResult.getStdOut()).thenReturn(Commands.PACKAGE_NAME);
        when(hBaseImpl.getCommands()).thenReturn(commands);
        when(commands.getInstallCommand()).thenReturn(requestBuilder);
        when(containerHost.getHostname()).thenReturn("test");
        when(hBaseImpl.getPluginDAO()).thenReturn(pluginDAO);
        when(pluginDAO.saveInfo(anyString(),anyString(),any())).thenReturn(true);
        when(hBaseConfig.getClusterName()).thenReturn("test");


        overHadoopSetupStrategy.setup();

        // assertions
        verify(trackerOperation).addLog("Installation info successfully saved");
        assertTrue(commandResult.hasSucceeded());
        assertEquals(commandResult, containerHost.execute(requestBuilder));
        assertNotNull(environment);
        assertNotNull(containerHost);
        assertNotNull(hBaseConfig);
        assertNotNull(commandResult);
        assertEquals(uuid, containerHost.getId());
        assertTrue(containerHost.isConnected());
        assertTrue(pluginDAO.saveInfo(anyString(), anyString(), any()));
        assertEquals(hBaseConfig,overHadoopSetupStrategy.setup());
    }

    @Test (expected = ClusterSetupException.class)
    public void shouldThrowsClusterSetupExceptionWhenClusterNameNotSpecified() throws ClusterSetupException
    {
        overHadoopSetupStrategy.checkConfig();
    }

    @Test (expected = ClusterSetupException.class)
    public void shouldThrowsClusterSetupExceptionWhenClusterAlreadyExist() throws ClusterSetupException
    {
        when(hBaseConfig.getClusterName()).thenReturn("test");
        when(hBaseImpl.getCluster(anyString())).thenReturn(hBaseConfig);
        overHadoopSetupStrategy.checkConfig();
    }

}