//package org.safehaus.subutai.plugin.zookeeper.impl;
//
//
//import java.util.Arrays;
//import java.util.HashSet;
//import java.util.Set;
//
//import org.junit.Ignore;
//import org.junit.Test;
//import org.safehaus.subutai.common.exception.ClusterConfigurationException;
//import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
//import org.safehaus.subutai.common.protocol.PlacementStrategy;
//import org.safehaus.subutai.common.tracker.OperationState;
//import org.safehaus.subutai.core.agent.api.AgentManager;
//import org.safehaus.subutai.core.command.api.CommandRunner;
//import org.safehaus.subutai.plugin.common.mock.CommonMockBuilder;
//import org.safehaus.subutai.plugin.common.mock.TrackerMock;
//import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
//import org.safehaus.subutai.plugin.zookeeper.api.SetupType;
//import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
//
//import static junit.framework.Assert.assertTrue;
//import static org.junit.Assert.assertEquals;
//import static org.mockito.Matchers.anyString;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//
//
//public class AddEnvironmentContainerNodeOperationHandlerTest
//{
//
//    @Test
//    public void testWithoutSetupType()
//    {
//        ZookeeperImpl zookeeperMock = mock( ZookeeperImpl.class );
//        when( zookeeperMock.getHadoopManager() ).thenReturn( mock( Hadoop.class ) );
//        when( zookeeperMock.getAgentManager() ).thenReturn( mock( AgentManager.class ) );
//        when( zookeeperMock.getCommandRunner() ).thenReturn( mock( CommandRunner.class ) );
//        when( zookeeperMock.getTracker() ).thenReturn( new TrackerMock() );
//        when( zookeeperMock.getContainerManager() ).thenReturn( mock( ContainerManager.class ) );
//        when( zookeeperMock.getHadoopManager() ).thenReturn( mock( Hadoop.class ) );
//        when( zookeeperMock.getCluster( anyString() ) ).thenReturn( null );
//        AbstractOperationHandler operationHandler = new AddNodeOperationHandler( zookeeperMock, "test-cluster" );
//        operationHandler.run();
//
//        assertTrue( operationHandler.getTrackerOperation().getLog().contains( "not exist" ) );
//        assertEquals( operationHandler.getTrackerOperation().getState(), OperationState.FAILED );
//    }
//
//
//    @Ignore
//    @Test
//    public void testWithStandaloneSetupType() throws LxcCreateException, ClusterConfigurationException
//    {
//        ZookeeperImpl zookeeperMock = mock( ZookeeperImpl.class );
//        when( zookeeperMock.getHadoopManager() ).thenReturn( mock( Hadoop.class ) );
//        when( zookeeperMock.getAgentManager() ).thenReturn( mock( AgentManager.class ) );
//        when( zookeeperMock.getCommandRunner() ).thenReturn( mock( CommandRunner.class ) );
//        when( zookeeperMock.getTracker() ).thenReturn( new TrackerMock() );
//        when( zookeeperMock.getContainerManager() ).thenReturn( mock( ContainerManager.class ) );
//        when( zookeeperMock.getHadoopManager() ).thenReturn( mock( Hadoop.class ) );
//
//        ZookeeperClusterConfig config = mock( ZookeeperClusterConfig.class );
//        when( config.getHadoopClusterName() ).thenReturn( "test-hadoop" );
//        when( config.getTemplateName() ).thenReturn( "hadoop" );
//        when( config.getSetupType() ).thenReturn( SetupType.STANDALONE );
//
//
//        Set agents = new HashSet( new HashSet<>( Arrays.asList( CommonMockBuilder.createAgent() ) ) );
//        when( zookeeperMock.getContainerManager()
//                           .clone( config.getTemplateName(), 1, null, PlacementStrategy.ROUND_ROBIN ) )
//                .thenReturn( agents );
//        when( zookeeperMock.getCluster( anyString() ) ).thenReturn( config );
//
//        AddNodeOperationHandler operationHandler = mock( AddNodeOperationHandler.class );
//        when( operationHandler.getClusterName() ).thenReturn( "test-cluster" );
//        when( operationHandler.getClusterConfiguration() ).thenReturn( mock( ClusterConfiguration.class ) );
//        //        doAnswer( new Answer() {
//        //            public Object answer( InvocationOnMock invocation ) {
//        //                Object[] args = invocation.getArguments();
//        //                return null;
//        //            }
//        //        } ).when( operationHandler ).getClusterConfiguration().configureCluster( config );
//
//        //        AddNodeOperationHandler operationHandler = new AddNodeOperationHandler( zookeeperMock,
//        // "test-cluster" );
//        operationHandler.run();
//
//        assertTrue( operationHandler.getTrackerOperation().getLog().contains( "not exist" ) );
//        assertEquals( operationHandler.getTrackerOperation().getState(), OperationState.FAILED );
//    }
//}
