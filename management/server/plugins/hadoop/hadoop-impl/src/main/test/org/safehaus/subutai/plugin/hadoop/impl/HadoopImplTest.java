package org.safehaus.subutai.plugin.hadoop.impl;

import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.common.util.DbUtil;
import org.safehaus.subutai.core.container.api.ContainerManager;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
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

    @Before
    public void setUp() throws Exception {
        dataSource = mock(DataSource.class);
        hadoopImpl = new HadoopImpl(dataSource);
        executorService = mock(ExecutorService.class);
        trackerOperation = mock(TrackerOperation.class);
        tracker = mock(Tracker.class);
        hadoopClusterConfig = mock(HadoopClusterConfig.class);
        uuid = new UUID(50,50);
    }

    @Test
    public void testGetCommands() throws Exception {
        hadoopImpl.getCommands();
    }

    @Test
    public void testSetCommands() throws Exception {
        Commands commands = mock(Commands.class);
        hadoopImpl.setCommands(commands);
    }

    @Test
    public void testInit() throws Exception {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        Connection connection = mock(Connection.class);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(5);
        when(dataSource.getConnection()).thenReturn(connection);
        HadoopImpl hadoop = new HadoopImpl(dataSource);
        DbUtil dbUtil = mock(DbUtil.class);
        hadoop.init();
    }

    @Test
    public void testDestroy() throws Exception {
        hadoopImpl.setExecutor(executorService);
        hadoopImpl.destroy();
    }

    @Test
    public void testGetTracker() throws Exception {
        hadoopImpl.getTracker();
    }

    @Test
    public void testSetTracker() throws Exception {
        Tracker tracker = mock(Tracker.class);
        hadoopImpl.setTracker(tracker);
    }

    @Test
    public void testGetContainerManager() throws Exception {
        hadoopImpl.getContainerManager();
    }

    @Test
    public void testSetContainerManager() throws Exception {
        ContainerManager containerManager = mock(ContainerManager.class);
        hadoopImpl.setContainerManager(containerManager);
    }

    @Test
    public void testGetExecutor() throws Exception {
        hadoopImpl.getExecutor();
    }

    @Test
    public void testSetExecutor() throws Exception {
        hadoopImpl.setExecutor(executorService);
    }

    @Test
    public void testGetEnvironmentManager() throws Exception {
        hadoopImpl.getEnvironmentManager();
    }

    @Test
    public void testSetEnvironmentManager() throws Exception {
        EnvironmentManager environmentManager = mock(EnvironmentManager.class);
        hadoopImpl.setEnvironmentManager(environmentManager);
    }

    @Test
    public void testGetPluginDAO() throws Exception {
        hadoopImpl.getPluginDAO();
    }

    @Test
    public void testInstallCluster() throws Exception {
        HadoopImpl hadoop = new HadoopImpl(dataSource);
        when(trackerOperation.getId()).thenReturn(uuid);
        when(tracker.createTrackerOperation(anyString(),anyString())).thenReturn(trackerOperation);
        hadoop.setTracker(tracker);
        hadoop.setExecutor(executorService);
        when(hadoopClusterConfig.getClusterName()).thenReturn("test");

        hadoop.installCluster(hadoopClusterConfig);

        assertNotNull(hadoop.installCluster(hadoopClusterConfig));
        assertEquals(uuid,hadoop.installCluster(hadoopClusterConfig));
    }

    @Test
    public void testUninstallCluster() throws Exception {
        HadoopImpl hadoop = new HadoopImpl(dataSource);
        when(trackerOperation.getId()).thenReturn(uuid);
        when(tracker.createTrackerOperation(anyString(),anyString())).thenReturn(trackerOperation);
        hadoop.setTracker(tracker);
        hadoop.setExecutor(executorService);
        when(hadoopClusterConfig.getClusterName()).thenReturn("test");

        hadoop.uninstallCluster(hadoopClusterConfig);

        assertNotNull(hadoop.uninstallCluster(hadoopClusterConfig));
        assertEquals(uuid,hadoop.uninstallCluster(hadoopClusterConfig));
    }

    @Test
    public void testGetClusters() throws Exception {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        Connection connection = mock(Connection.class);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(5);
        when(dataSource.getConnection()).thenReturn(connection);
        hadoopImpl.init();
        hadoopImpl.getClusters();
    }

    @Test
    public void testGetCluster() throws Exception {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        Connection connection = mock(Connection.class);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(5);
        when(dataSource.getConnection()).thenReturn(connection);
        hadoopImpl.init();
        String clusterName = "test";
        hadoopImpl.getCluster(clusterName);
    }

    @Test
    public void testAddNode() throws Exception {
    }

    @Test
    public void testUninstallCluster1() throws Exception {

    }

    @Test
    public void testStartNameNode() throws Exception {
        HadoopImpl hadoop = new HadoopImpl(dataSource);
        when(trackerOperation.getId()).thenReturn(uuid);
        when(tracker.createTrackerOperation(anyString(),anyString())).thenReturn(trackerOperation);
        hadoop.setTracker(tracker);
        hadoop.setExecutor(executorService);
        when(hadoopClusterConfig.getClusterName()).thenReturn("test");

        hadoop.startNameNode(hadoopClusterConfig);

        assertNotNull(hadoop.startNameNode(hadoopClusterConfig));
        assertEquals(uuid, hadoop.startNameNode(hadoopClusterConfig));
    }

    @Test
    public void testStopNameNode() throws Exception {
        HadoopImpl hadoop = new HadoopImpl(dataSource);
        when(trackerOperation.getId()).thenReturn(uuid);
        when(tracker.createTrackerOperation(anyString(),anyString())).thenReturn(trackerOperation);
        hadoop.setTracker(tracker);
        hadoop.setExecutor(executorService);
        when(hadoopClusterConfig.getClusterName()).thenReturn("test");

        hadoop.stopNameNode(hadoopClusterConfig);

        assertNotNull(hadoop.stopNameNode(hadoopClusterConfig));
        assertEquals(uuid, hadoop.stopNameNode(hadoopClusterConfig));
    }

    @Test
    public void testStatusNameNode() throws Exception {
        HadoopImpl hadoop = new HadoopImpl(dataSource);
        when(trackerOperation.getId()).thenReturn(uuid);
        when(tracker.createTrackerOperation(anyString(),anyString())).thenReturn(trackerOperation);
        hadoop.setTracker(tracker);
        hadoop.setExecutor(executorService);
        when(hadoopClusterConfig.getClusterName()).thenReturn("test");

        hadoop.statusNameNode(hadoopClusterConfig);

        assertNotNull(hadoop.statusNameNode(hadoopClusterConfig));
        assertEquals(uuid,hadoop.statusNameNode(hadoopClusterConfig));

    }

    @Test
    public void testStatusSecondaryNameNode() throws Exception {
        HadoopImpl hadoop = new HadoopImpl(dataSource);
        when(trackerOperation.getId()).thenReturn(uuid);
        when(tracker.createTrackerOperation(anyString(),anyString())).thenReturn(trackerOperation);
        hadoop.setTracker(tracker);
        hadoop.setExecutor(executorService);
        when(hadoopClusterConfig.getClusterName()).thenReturn("test");

        hadoop.statusSecondaryNameNode(hadoopClusterConfig);

        assertNotNull(hadoop.statusSecondaryNameNode(hadoopClusterConfig));
        assertEquals(uuid,hadoop.statusSecondaryNameNode(hadoopClusterConfig));
    }

    @Test
    public void testStartDataNode() throws Exception {
        HadoopImpl hadoop = new HadoopImpl(dataSource);
        when(trackerOperation.getId()).thenReturn(uuid);
        when(tracker.createTrackerOperation(anyString(),anyString())).thenReturn(trackerOperation);
        hadoop.setTracker(tracker);
        hadoop.setExecutor(executorService);
        when(hadoopClusterConfig.getClusterName()).thenReturn("test");
        String hostname = "test";
        hadoop.startDataNode(hadoopClusterConfig,hostname);

        assertNotNull(hadoop.startDataNode(hadoopClusterConfig, hostname));
        assertEquals(uuid,hadoop.startDataNode(hadoopClusterConfig, hostname));
    }

    @Test
    public void testStopDataNode() throws Exception {
        HadoopImpl hadoop = new HadoopImpl(dataSource);
        when(trackerOperation.getId()).thenReturn(uuid);
        when(tracker.createTrackerOperation(anyString(),anyString())).thenReturn(trackerOperation);
        hadoop.setTracker(tracker);
        hadoop.setExecutor(executorService);
        when(hadoopClusterConfig.getClusterName()).thenReturn("test");
        String hostname = "test";
        hadoop.stopDataNode(hadoopClusterConfig, hostname);

        assertNotNull(hadoop.stopDataNode(hadoopClusterConfig, hostname));
        assertEquals(uuid,hadoop.stopDataNode(hadoopClusterConfig, hostname));
    }

    @Test
    public void testStatusDataNode() throws Exception {
        HadoopImpl hadoop = new HadoopImpl(dataSource);
        when(trackerOperation.getId()).thenReturn(uuid);
        when(tracker.createTrackerOperation(anyString(),anyString())).thenReturn(trackerOperation);
        hadoop.setTracker(tracker);
        hadoop.setExecutor(executorService);
        when(hadoopClusterConfig.getClusterName()).thenReturn("test");
        String hostname = "test";
        hadoop.statusDataNode(hadoopClusterConfig, hostname);

        assertNotNull(hadoop.statusDataNode(hadoopClusterConfig, hostname));
        assertEquals(uuid,hadoop.statusDataNode(hadoopClusterConfig, hostname));

    }

    @Test
    public void testStartJobTracker() throws Exception {
        HadoopImpl hadoop = new HadoopImpl(dataSource);
        when(trackerOperation.getId()).thenReturn(uuid);
        when(tracker.createTrackerOperation(anyString(),anyString())).thenReturn(trackerOperation);
        hadoop.setTracker(tracker);
        hadoop.setExecutor(executorService);
        when(hadoopClusterConfig.getClusterName()).thenReturn("test");

        hadoop.startJobTracker(hadoopClusterConfig);

        assertNotNull(hadoop.startJobTracker(hadoopClusterConfig));
        assertEquals(uuid,hadoop.startJobTracker(hadoopClusterConfig));
    }

    @Test
    public void testStopJobTracker() throws Exception {
        HadoopImpl hadoop = new HadoopImpl(dataSource);
        when(trackerOperation.getId()).thenReturn(uuid);
        when(tracker.createTrackerOperation(anyString(),anyString())).thenReturn(trackerOperation);
        hadoop.setTracker(tracker);
        hadoop.setExecutor(executorService);
        when(hadoopClusterConfig.getClusterName()).thenReturn("test");

        hadoop.stopJobTracker(hadoopClusterConfig);

        assertNotNull(hadoop.stopJobTracker(hadoopClusterConfig));
        assertEquals(uuid,hadoop.stopJobTracker(hadoopClusterConfig));
    }

    @Test
    public void testStatusJobTracker() throws Exception {
        HadoopImpl hadoop = new HadoopImpl(dataSource);
        when(trackerOperation.getId()).thenReturn(uuid);
        when(tracker.createTrackerOperation(anyString(),anyString())).thenReturn(trackerOperation);
        hadoop.setTracker(tracker);
        hadoop.setExecutor(executorService);
        when(hadoopClusterConfig.getClusterName()).thenReturn("test");

        hadoop.statusJobTracker(hadoopClusterConfig);

        assertNotNull(hadoop.statusJobTracker(hadoopClusterConfig));
        assertEquals(uuid,hadoop.statusJobTracker(hadoopClusterConfig));
    }

    @Test
    public void testStartTaskTracker() throws Exception {
        HadoopImpl hadoop = new HadoopImpl(dataSource);
        when(trackerOperation.getId()).thenReturn(uuid);
        when(tracker.createTrackerOperation(anyString(),anyString())).thenReturn(trackerOperation);
        hadoop.setTracker(tracker);
        hadoop.setExecutor(executorService);
        when(hadoopClusterConfig.getClusterName()).thenReturn("test");
        String hostname = "test";
        hadoop.startTaskTracker(hadoopClusterConfig, hostname);

        assertNotNull(hadoop.startTaskTracker(hadoopClusterConfig, hostname));
        assertEquals(uuid,hadoop.startTaskTracker(hadoopClusterConfig, hostname));
    }

    @Test
    public void testStopTaskTracker() throws Exception {
        HadoopImpl hadoop = new HadoopImpl(dataSource);
        when(trackerOperation.getId()).thenReturn(uuid);
        when(tracker.createTrackerOperation(anyString(),anyString())).thenReturn(trackerOperation);
        hadoop.setTracker(tracker);
        hadoop.setExecutor(executorService);
        when(hadoopClusterConfig.getClusterName()).thenReturn("test");
        String hostname = "test";
        hadoop.stopTaskTracker(hadoopClusterConfig, hostname);

        assertNotNull(hadoop.stopTaskTracker(hadoopClusterConfig, hostname));
        assertEquals(uuid,hadoop.stopTaskTracker(hadoopClusterConfig, hostname));
    }

    @Test
    public void testStatusTaskTracker() throws Exception {
        HadoopImpl hadoop = new HadoopImpl(dataSource);
        when(trackerOperation.getId()).thenReturn(uuid);
        when(tracker.createTrackerOperation(anyString(),anyString())).thenReturn(trackerOperation);
        hadoop.setTracker(tracker);
        hadoop.setExecutor(executorService);
        when(hadoopClusterConfig.getClusterName()).thenReturn("test");
        String hostname = "test";
        hadoop.statusTaskTracker(hadoopClusterConfig, hostname);

        assertNotNull(hadoop.statusTaskTracker(hadoopClusterConfig, hostname));
        assertEquals(uuid,hadoop.statusTaskTracker(hadoopClusterConfig, hostname));
    }

    @Test
    public void testAddNode1() throws Exception {
        HadoopImpl hadoop = new HadoopImpl(dataSource);
        when(trackerOperation.getId()).thenReturn(uuid);
        when(tracker.createTrackerOperation(anyString(),anyString())).thenReturn(trackerOperation);
        hadoop.setTracker(tracker);
        hadoop.setExecutor(executorService);
        when(hadoopClusterConfig.getClusterName()).thenReturn("test");
        String clusterName = "test";
        hadoop.addNode(clusterName,5);

        assertNotNull(hadoop.addNode(clusterName, 5));
        assertEquals(uuid,hadoop.addNode(clusterName, 5));
    }

    @Test
    public void testDestroyNode() throws Exception {
        HadoopImpl hadoop = new HadoopImpl(dataSource);
        when(trackerOperation.getId()).thenReturn(uuid);
        when(tracker.createTrackerOperation(anyString(),anyString())).thenReturn(trackerOperation);
        hadoop.setTracker(tracker);
        hadoop.setExecutor(executorService);
        when(hadoopClusterConfig.getClusterName()).thenReturn("test");
        String hostname = "test";
        hadoop.destroyNode(hadoopClusterConfig, hostname);

        assertNotNull(hadoop.destroyNode(hadoopClusterConfig, hostname));
        assertEquals(uuid,hadoop.destroyNode(hadoopClusterConfig, hostname));
    }

    @Test
    public void testCheckDecomissionStatus() throws Exception {
        HadoopImpl hadoop = new HadoopImpl(dataSource);
        when(trackerOperation.getId()).thenReturn(uuid);
        when(tracker.createTrackerOperation(anyString(),anyString())).thenReturn(trackerOperation);
        hadoop.setTracker(tracker);
        hadoop.setExecutor(executorService);
        when(hadoopClusterConfig.getClusterName()).thenReturn("test");

        hadoop.checkDecomissionStatus(hadoopClusterConfig);

        assertNotNull(hadoop.checkDecomissionStatus(hadoopClusterConfig));
        assertEquals(uuid,hadoop.checkDecomissionStatus(hadoopClusterConfig));
    }

    @Test
    public void testExcludeNode() throws Exception {
        HadoopImpl hadoop = new HadoopImpl(dataSource);
        when(trackerOperation.getId()).thenReturn(uuid);
        when(tracker.createTrackerOperation(anyString(),anyString())).thenReturn(trackerOperation);
        hadoop.setTracker(tracker);
        hadoop.setExecutor(executorService);
        when(hadoopClusterConfig.getClusterName()).thenReturn("test");
        String hostname = "test";
        hadoop.excludeNode(hadoopClusterConfig, hostname);

        assertNotNull(hadoop.excludeNode(hadoopClusterConfig, hostname));
        assertEquals(uuid,hadoop.excludeNode(hadoopClusterConfig, hostname));
    }

    @Test
    public void testIncludeNode() throws Exception {
        HadoopImpl hadoop = new HadoopImpl(dataSource);
        when(trackerOperation.getId()).thenReturn(uuid);
        when(tracker.createTrackerOperation(anyString(),anyString())).thenReturn(trackerOperation);
        hadoop.setTracker(tracker);
        hadoop.setExecutor(executorService);
        when(hadoopClusterConfig.getClusterName()).thenReturn("test");
        String hostname = "test";
        hadoop.includeNode(hadoopClusterConfig, hostname);

        assertNotNull(hadoop.includeNode(hadoopClusterConfig, hostname));
        assertEquals(uuid,hadoop.includeNode(hadoopClusterConfig, hostname));
    }

    @Test
    public void testGetDefaultEnvironmentBlueprint() throws Exception {
        HadoopImpl hadoop = new HadoopImpl(dataSource);
        hadoop.getDefaultEnvironmentBlueprint(hadoopClusterConfig);
        assertNotNull(hadoop.getDefaultEnvironmentBlueprint(hadoopClusterConfig));
    }
}