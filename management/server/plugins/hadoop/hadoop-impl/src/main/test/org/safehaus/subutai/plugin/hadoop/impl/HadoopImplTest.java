package org.safehaus.subutai.plugin.hadoop.impl;

import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.container.api.ContainerManager;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HadoopImplTest {
    private HadoopImpl hadoopImpl;
    private DataSource dataSource;
    ExecutorService executorService;
    TrackerOperation trackerOperation;
    Tracker tracker;
    HadoopClusterConfig hadoopClusterConfig;
    UUID uuid;
    Commands commands;
    PreparedStatement preparedStatement;
    Connection connection;
    ContainerManager containerManager;
    EnvironmentManager environmentManager;


    @Before
    public void setUp() {
        dataSource = mock(DataSource.class);
        hadoopImpl = new HadoopImpl(dataSource);
        executorService = mock(ExecutorService.class);
        trackerOperation = mock(TrackerOperation.class);
        tracker = mock(Tracker.class);
        hadoopClusterConfig = mock(HadoopClusterConfig.class);
        uuid = new UUID(50, 50);
        commands = mock(Commands.class);
        preparedStatement = mock(PreparedStatement.class);
        connection = mock(Connection.class);
        containerManager = mock(ContainerManager.class);
        environmentManager = mock(EnvironmentManager.class);
    }

    @Test
    public void testGetCommands() {
        hadoopImpl.setCommands(commands);

        assertNotNull(hadoopImpl.getCommands());
        assertEquals(commands, hadoopImpl.getCommands());
    }

    @Test
    public void testSetCommands() {
        hadoopImpl.setCommands(commands);

        assertEquals(commands, hadoopImpl.getCommands());
    }

    @Test
    public void testInit() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(5);
        when(dataSource.getConnection()).thenReturn(connection);
        HadoopImpl hadoop = new HadoopImpl(dataSource);
        hadoop.init();

        assertEquals(connection, dataSource.getConnection());
        assertEquals(5, preparedStatement.executeUpdate());

    }

    @Test
    public void testDestroy() {
        hadoopImpl.setExecutor(executorService);
        hadoopImpl.destroy();

    }

    @Test
    public void testGetTracker() {
        hadoopImpl.setTracker(tracker);
        hadoopImpl.getTracker();

        assertEquals(tracker, hadoopImpl.getTracker());
        assertNotNull(hadoopImpl.getTracker());

    }

    @Test
    public void testSetTracker() {
        hadoopImpl.setTracker(tracker);

        assertEquals(tracker, hadoopImpl.getTracker());
    }

    @Test
    public void testGetContainerManager() {
        hadoopImpl.setContainerManager(containerManager);
        hadoopImpl.getContainerManager();

        assertEquals(containerManager, hadoopImpl.getContainerManager());
        assertNotNull(hadoopImpl.getContainerManager());
    }

    @Test
    public void testSetContainerManager() {
        hadoopImpl.setContainerManager(containerManager);

        assertEquals(containerManager, hadoopImpl.getContainerManager());
    }

    @Test
    public void testGetExecutor() {
        hadoopImpl.setExecutor(executorService);
        hadoopImpl.getExecutor();

        assertEquals(executorService, hadoopImpl.getExecutor());
        assertNotNull(hadoopImpl.getExecutor());
    }

    @Test
    public void testSetExecutor() {
        hadoopImpl.setExecutor(executorService);
        hadoopImpl.getExecutor();

        assertEquals(executorService, hadoopImpl.getExecutor());
    }

    @Test
    public void testGetEnvironmentManager() {
        hadoopImpl.getEnvironmentManager();
    }

    @Test
    public void testSetEnvironmentManager() {
        hadoopImpl.setEnvironmentManager(environmentManager);
    }

    @Test
    public void testGetPluginDAO() {
        hadoopImpl.getPluginDAO();
    }

    @Test
    public void testInstallCluster() {
        when(trackerOperation.getId()).thenReturn(uuid);
        when(tracker.createTrackerOperation(anyString(), anyString())).thenReturn(trackerOperation);
        hadoopImpl.setTracker(tracker);
        hadoopImpl.setExecutor(executorService);
        when(hadoopClusterConfig.getClusterName()).thenReturn("test");

        hadoopImpl.installCluster(hadoopClusterConfig);

        assertNotNull(hadoopImpl.installCluster(hadoopClusterConfig));
        assertEquals(uuid, hadoopImpl.installCluster(hadoopClusterConfig));
    }

    @Test
    public void testUninstallCluster() {
        when(trackerOperation.getId()).thenReturn(uuid);
        when(tracker.createTrackerOperation(anyString(), anyString())).thenReturn(trackerOperation);
        hadoopImpl.setTracker(tracker);
        hadoopImpl.setExecutor(executorService);
        when(hadoopClusterConfig.getClusterName()).thenReturn("test");

        hadoopImpl.uninstallCluster(hadoopClusterConfig);

        assertNotNull(hadoopImpl.uninstallCluster(hadoopClusterConfig));
        assertEquals(uuid, hadoopImpl.uninstallCluster(hadoopClusterConfig));
    }

    @Test
    public void testStartNameNode() {
        when(trackerOperation.getId()).thenReturn(uuid);
        when(tracker.createTrackerOperation(anyString(), anyString())).thenReturn(trackerOperation);
        hadoopImpl.setTracker(tracker);
        hadoopImpl.setExecutor(executorService);
        when(hadoopClusterConfig.getClusterName()).thenReturn("test");

        hadoopImpl.startNameNode(hadoopClusterConfig);

        assertNotNull(hadoopImpl.startNameNode(hadoopClusterConfig));
        assertEquals(uuid, hadoopImpl.startNameNode(hadoopClusterConfig));
    }

    @Test
    public void testStopNameNode() {
        when(trackerOperation.getId()).thenReturn(uuid);
        when(tracker.createTrackerOperation(anyString(), anyString())).thenReturn(trackerOperation);
        hadoopImpl.setTracker(tracker);
        hadoopImpl.setExecutor(executorService);
        when(hadoopClusterConfig.getClusterName()).thenReturn("test");

        hadoopImpl.stopNameNode(hadoopClusterConfig);

        assertNotNull(hadoopImpl.stopNameNode(hadoopClusterConfig));
        assertEquals(uuid, hadoopImpl.stopNameNode(hadoopClusterConfig));
    }

    @Test
    public void testStatusNameNode() {
        when(trackerOperation.getId()).thenReturn(uuid);
        when(tracker.createTrackerOperation(anyString(), anyString())).thenReturn(trackerOperation);
        hadoopImpl.setTracker(tracker);
        hadoopImpl.setExecutor(executorService);
        when(hadoopClusterConfig.getClusterName()).thenReturn("test");

        hadoopImpl.statusNameNode(hadoopClusterConfig);

        assertNotNull(hadoopImpl.statusNameNode(hadoopClusterConfig));
        assertEquals(uuid, hadoopImpl.statusNameNode(hadoopClusterConfig));

    }

    @Test
    public void testStatusSecondaryNameNode() {
        when(trackerOperation.getId()).thenReturn(uuid);
        when(tracker.createTrackerOperation(anyString(), anyString())).thenReturn(trackerOperation);
        hadoopImpl.setTracker(tracker);
        hadoopImpl.setExecutor(executorService);
        when(hadoopClusterConfig.getClusterName()).thenReturn("test");

        hadoopImpl.statusSecondaryNameNode(hadoopClusterConfig);

        assertNotNull(hadoopImpl.statusSecondaryNameNode(hadoopClusterConfig));
        assertEquals(uuid, hadoopImpl.statusSecondaryNameNode(hadoopClusterConfig));
    }

    @Test
    public void testStartDataNode() {
        when(trackerOperation.getId()).thenReturn(uuid);
        when(tracker.createTrackerOperation(anyString(), anyString())).thenReturn(trackerOperation);
        hadoopImpl.setTracker(tracker);
        hadoopImpl.setExecutor(executorService);
        when(hadoopClusterConfig.getClusterName()).thenReturn("test");
        String hostname = "test";
        hadoopImpl.startDataNode(hadoopClusterConfig, hostname);

        assertNotNull(hadoopImpl.startDataNode(hadoopClusterConfig, hostname));
        assertEquals(uuid, hadoopImpl.startDataNode(hadoopClusterConfig, hostname));
    }

    @Test
    public void testStopDataNode() {
        when(trackerOperation.getId()).thenReturn(uuid);
        when(tracker.createTrackerOperation(anyString(), anyString())).thenReturn(trackerOperation);
        hadoopImpl.setTracker(tracker);
        hadoopImpl.setExecutor(executorService);
        when(hadoopClusterConfig.getClusterName()).thenReturn("test");
        String hostname = "test";
        hadoopImpl.stopDataNode(hadoopClusterConfig, hostname);

        assertNotNull(hadoopImpl.stopDataNode(hadoopClusterConfig, hostname));
        assertEquals(uuid, hadoopImpl.stopDataNode(hadoopClusterConfig, hostname));
    }

    @Test
    public void testStatusDataNode() {
        when(trackerOperation.getId()).thenReturn(uuid);
        when(tracker.createTrackerOperation(anyString(), anyString())).thenReturn(trackerOperation);
        hadoopImpl.setTracker(tracker);
        hadoopImpl.setExecutor(executorService);
        when(hadoopClusterConfig.getClusterName()).thenReturn("test");
        String hostname = "test";
        hadoopImpl.statusDataNode(hadoopClusterConfig, hostname);

        assertNotNull(hadoopImpl.statusDataNode(hadoopClusterConfig, hostname));
        assertEquals(uuid, hadoopImpl.statusDataNode(hadoopClusterConfig, hostname));

    }

    @Test
    public void testStartJobTracker() {
        when(trackerOperation.getId()).thenReturn(uuid);
        when(tracker.createTrackerOperation(anyString(), anyString())).thenReturn(trackerOperation);
        hadoopImpl.setTracker(tracker);
        hadoopImpl.setExecutor(executorService);
        when(hadoopClusterConfig.getClusterName()).thenReturn("test");

        hadoopImpl.startJobTracker(hadoopClusterConfig);

        assertNotNull(hadoopImpl.startJobTracker(hadoopClusterConfig));
        assertEquals(uuid, hadoopImpl.startJobTracker(hadoopClusterConfig));
    }

    @Test
    public void testStopJobTracker() {
        when(trackerOperation.getId()).thenReturn(uuid);
        when(tracker.createTrackerOperation(anyString(), anyString())).thenReturn(trackerOperation);
        hadoopImpl.setTracker(tracker);
        hadoopImpl.setExecutor(executorService);
        when(hadoopClusterConfig.getClusterName()).thenReturn("test");

        hadoopImpl.stopJobTracker(hadoopClusterConfig);

        assertNotNull(hadoopImpl.stopJobTracker(hadoopClusterConfig));
        assertEquals(uuid, hadoopImpl.stopJobTracker(hadoopClusterConfig));
    }

    @Test
    public void testStatusJobTracker() {
        when(trackerOperation.getId()).thenReturn(uuid);
        when(tracker.createTrackerOperation(anyString(), anyString())).thenReturn(trackerOperation);
        hadoopImpl.setTracker(tracker);
        hadoopImpl.setExecutor(executorService);
        when(hadoopClusterConfig.getClusterName()).thenReturn("test");

        hadoopImpl.statusJobTracker(hadoopClusterConfig);

        assertNotNull(hadoopImpl.statusJobTracker(hadoopClusterConfig));
        assertEquals(uuid, hadoopImpl.statusJobTracker(hadoopClusterConfig));
    }

    @Test
    public void testStartTaskTracker() {
        when(trackerOperation.getId()).thenReturn(uuid);
        when(tracker.createTrackerOperation(anyString(), anyString())).thenReturn(trackerOperation);
        hadoopImpl.setTracker(tracker);
        hadoopImpl.setExecutor(executorService);
        when(hadoopClusterConfig.getClusterName()).thenReturn("test");
        String hostname = "test";
        hadoopImpl.startTaskTracker(hadoopClusterConfig, hostname);

        assertNotNull(hadoopImpl.startTaskTracker(hadoopClusterConfig, hostname));
        assertEquals(uuid, hadoopImpl.startTaskTracker(hadoopClusterConfig, hostname));
    }

    @Test
    public void testStopTaskTracker() {
        when(trackerOperation.getId()).thenReturn(uuid);
        when(tracker.createTrackerOperation(anyString(), anyString())).thenReturn(trackerOperation);
        hadoopImpl.setTracker(tracker);
        hadoopImpl.setExecutor(executorService);
        when(hadoopClusterConfig.getClusterName()).thenReturn("test");
        String hostname = "test";
        hadoopImpl.stopTaskTracker(hadoopClusterConfig, hostname);

        assertNotNull(hadoopImpl.stopTaskTracker(hadoopClusterConfig, hostname));
        assertEquals(uuid, hadoopImpl.stopTaskTracker(hadoopClusterConfig, hostname));
    }

    @Test
    public void testStatusTaskTracker() {
        when(trackerOperation.getId()).thenReturn(uuid);
        when(tracker.createTrackerOperation(anyString(), anyString())).thenReturn(trackerOperation);
        hadoopImpl.setTracker(tracker);
        hadoopImpl.setExecutor(executorService);
        when(hadoopClusterConfig.getClusterName()).thenReturn("test");
        String hostname = "test";
        hadoopImpl.statusTaskTracker(hadoopClusterConfig, hostname);

        assertNotNull(hadoopImpl.statusTaskTracker(hadoopClusterConfig, hostname));
        assertEquals(uuid, hadoopImpl.statusTaskTracker(hadoopClusterConfig, hostname));
    }

    @Test
    public void testAddNode1() {
        when(trackerOperation.getId()).thenReturn(uuid);
        when(tracker.createTrackerOperation(anyString(), anyString())).thenReturn(trackerOperation);
        hadoopImpl.setTracker(tracker);
        hadoopImpl.setExecutor(executorService);
        when(hadoopClusterConfig.getClusterName()).thenReturn("test");
        String clusterName = "test";
        hadoopImpl.addNode(clusterName, 5);

        assertNotNull(hadoopImpl.addNode(clusterName, 5));
        assertEquals(uuid, hadoopImpl.addNode(clusterName, 5));
    }

    @Test
    public void testDestroyNode() {
        when(trackerOperation.getId()).thenReturn(uuid);
        when(tracker.createTrackerOperation(anyString(), anyString())).thenReturn(trackerOperation);
        hadoopImpl.setTracker(tracker);
        hadoopImpl.setExecutor(executorService);
        when(hadoopClusterConfig.getClusterName()).thenReturn("test");
        String hostname = "test";
        hadoopImpl.destroyNode(hadoopClusterConfig, hostname);

        assertNotNull(hadoopImpl.destroyNode(hadoopClusterConfig, hostname));
        assertEquals(uuid, hadoopImpl.destroyNode(hadoopClusterConfig, hostname));
    }

    @Test
    public void testCheckDecomissionStatus() {
        when(trackerOperation.getId()).thenReturn(uuid);
        when(tracker.createTrackerOperation(anyString(), anyString())).thenReturn(trackerOperation);
        hadoopImpl.setTracker(tracker);
        hadoopImpl.setExecutor(executorService);
        when(hadoopClusterConfig.getClusterName()).thenReturn("test");

        hadoopImpl.checkDecomissionStatus(hadoopClusterConfig);

        assertNotNull(hadoopImpl.checkDecomissionStatus(hadoopClusterConfig));
        assertEquals(uuid, hadoopImpl.checkDecomissionStatus(hadoopClusterConfig));
    }

    @Test
    public void testExcludeNode() {
        when(trackerOperation.getId()).thenReturn(uuid);
        when(tracker.createTrackerOperation(anyString(), anyString())).thenReturn(trackerOperation);
        hadoopImpl.setTracker(tracker);
        hadoopImpl.setExecutor(executorService);
        when(hadoopClusterConfig.getClusterName()).thenReturn("test");
        String hostname = "test";
        hadoopImpl.excludeNode(hadoopClusterConfig, hostname);

        assertNotNull(hadoopImpl.excludeNode(hadoopClusterConfig, hostname));
        assertEquals(uuid, hadoopImpl.excludeNode(hadoopClusterConfig, hostname));
    }

    @Test
    public void testIncludeNode() {
        when(trackerOperation.getId()).thenReturn(uuid);
        when(tracker.createTrackerOperation(anyString(), anyString())).thenReturn(trackerOperation);
        hadoopImpl.setTracker(tracker);
        hadoopImpl.setExecutor(executorService);
        when(hadoopClusterConfig.getClusterName()).thenReturn("test");
        String hostname = "test";
        hadoopImpl.includeNode(hadoopClusterConfig, hostname);

        assertNotNull(hadoopImpl.includeNode(hadoopClusterConfig, hostname));
        assertEquals(uuid, hadoopImpl.includeNode(hadoopClusterConfig, hostname));
    }

    @Test
    public void testGetDefaultEnvironmentBlueprint() throws ClusterSetupException {
        hadoopImpl.getDefaultEnvironmentBlueprint(hadoopClusterConfig);

        assertNotNull(hadoopImpl.getDefaultEnvironmentBlueprint(hadoopClusterConfig));
    }
}