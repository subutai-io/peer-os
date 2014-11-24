package org.safehaus.subutai.plugin.hadoop.impl.handler;

import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.common.api.ClusterOperationType;
import org.safehaus.subutai.plugin.common.api.NodeType;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hadoop.impl.HadoopImpl;

import javax.sql.DataSource;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ClusterOperationHandlerTest {
    ClusterOperationHandler clusterOperationHandler;
    ClusterOperationHandler clusterOperationHandler1;
    ClusterOperationHandler clusterOperationHandler2;
    ClusterOperationHandler clusterOperationHandler3;
    TrackerOperation trackerOperation;
    UUID uuid;
    DataSource dataSource;
    ExecutorService executorService;
    HadoopClusterConfig hadoopClusterConfig;

    @Before
    public void setUp() throws Exception {
        ContainerHost containerHost = mock(ContainerHost.class);
        ContainerHost containerHost2 = mock(ContainerHost.class);
        Set<ContainerHost> mySet = mock(Set.class);
        mySet.add(containerHost);
        mySet.add(containerHost2);


        Environment environment = mock(Environment.class);
        EnvironmentManager environmentManager = mock(EnvironmentManager.class);
        hadoopClusterConfig = mock(HadoopClusterConfig.class);
        dataSource = mock(DataSource.class);
        executorService = mock(ExecutorService.class);
        trackerOperation = mock(TrackerOperation.class);
        uuid = new UUID(50, 50);
        Tracker tracker = mock(Tracker.class);
        HadoopImpl hadoop = mock(HadoopImpl.class);

        when(trackerOperation.getId()).thenReturn(uuid);
        when(tracker.createTrackerOperation(anyString(), anyString())).thenReturn(trackerOperation);
        when(hadoop.getTracker()).thenReturn(tracker);
        when(hadoop.getEnvironmentManager()).thenReturn(environmentManager);
        when(environmentManager.getEnvironmentByUUID(uuid)).thenReturn(environment);
        when(environment.getContainerHostByUUID(uuid)).thenReturn(containerHost);

        when(environmentManager.buildEnvironment(hadoop.getDefaultEnvironmentBlueprint(hadoopClusterConfig))).thenReturn(environment);
        when(hadoopClusterConfig.getNameNode()).thenReturn(uuid);

        when(hadoopClusterConfig.getClusterName()).thenReturn("test");

        clusterOperationHandler = new ClusterOperationHandler(hadoop,hadoopClusterConfig,ClusterOperationType.INSTALL,NodeType.MASTER_NODE);
        clusterOperationHandler1 = new ClusterOperationHandler(hadoop,hadoopClusterConfig,ClusterOperationType.UNINSTALL,NodeType.MASTER_NODE);
//        clusterOperationHandler2 = new ClusterOperationHandler(hadoop,hadoopClusterConfig,ClusterOperationType.STATUS_ALL,NodeType.MASTER_NODE);
        clusterOperationHandler3 = new ClusterOperationHandler(hadoop,hadoopClusterConfig,ClusterOperationType.DECOMISSION_STATUS,NodeType.MASTER_NODE);
    }

    @Test
    public void testRunClusterOperationTypeInstall() throws Exception {
        clusterOperationHandler.run();
    }

    @Test
    public void testRunClusterOperationTypeUninstall() throws Exception {
        clusterOperationHandler1.run();
    }

    @Test
    public void testRunOperationOnContainers() throws Exception {

    }

    @Test
    public void testLogStatusResultsWithNodeTypeNONAME() throws Exception {

        CommandResult commandResult = mock(CommandResult.class);
        when(commandResult.getStdOut()).thenReturn("NameNode");
        clusterOperationHandler.logStatusResults(trackerOperation,commandResult,NodeType.NAMENODE);

    }

    @Test
    public void testLogStatusResultsWithNodeTypeJOBTRACKER() throws Exception {
        CommandResult commandResult = mock(CommandResult.class);
        when(commandResult.getStdOut()).thenReturn("JobTracker");
        clusterOperationHandler.logStatusResults(trackerOperation,commandResult,NodeType.JOBTRACKER);
    }

    @Test
    public void testLogStatusResultsWithNodeTypeSECONDARY_NAMENODE() throws Exception {
        CommandResult commandResult = mock(CommandResult.class);
        when(commandResult.getStdOut()).thenReturn("SecondaryNameNode");
        clusterOperationHandler.logStatusResults(trackerOperation,commandResult,NodeType.SECONDARY_NAMENODE);
    }

    @Test
    public void testLogStatusResultsWithNodeTypeDATANODE() throws Exception {
        CommandResult commandResult = mock(CommandResult.class);
        when(commandResult.getStdOut()).thenReturn("DataNode");
        clusterOperationHandler.logStatusResults(trackerOperation,commandResult,NodeType.DATANODE);
    }

    @Test
    public void testLogStatusResultsWithNodeTypeTASKTRACKER() throws Exception {
        CommandResult commandResult = mock(CommandResult.class);
        when(commandResult.getStdOut()).thenReturn("TaskTracker");
        clusterOperationHandler.logStatusResults(trackerOperation,commandResult,NodeType.TASKTRACKER);
    }

    @Test
    public void testLogStatusResultsWithNodeTypeSLAVE_NODE() throws Exception {
        CommandResult commandResult = mock(CommandResult.class);
        clusterOperationHandler.logStatusResults(trackerOperation,commandResult,NodeType.SLAVE_NODE);
    }

    @Test
    public void testDestroyCluster() throws Exception {
   }
}