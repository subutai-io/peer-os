package org.safehaus.subutai.plugin.zookeeper.impl;


import org.junit.Test;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.OperationState;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.plugin.common.mock.TrackerMock;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.zookeeper.impl.handler.AddPropertyOperationHandler;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class AddPropertyOperationHandlerTest
{
    @Test
    public void testWithoutCluster()
    {
        ZookeeperImpl zookeeperMock = mock( ZookeeperImpl.class );
        when( zookeeperMock.getHadoopManager() ).thenReturn( mock( Hadoop.class ) );
        when( zookeeperMock.getTracker() ).thenReturn( new TrackerMock() );
        when( zookeeperMock.getEnvironmentManager() ).thenReturn( mock( EnvironmentManager.class ) );
        when( zookeeperMock.getHadoopManager() ).thenReturn( mock( Hadoop.class ) );
        when( zookeeperMock.getCluster( anyString() ) ).thenReturn( null );
        AbstractOperationHandler operationHandler =
                new AddPropertyOperationHandler( zookeeperMock, "test-cluster", "test-file", "test-property",
                        "test-value" );
        operationHandler.run();

        assertTrue( operationHandler.getTrackerOperation().getLog().contains( "not exist" ) );
        assertEquals( operationHandler.getTrackerOperation().getState(), OperationState.FAILED );
    }

    //    @Ignore
    //    @Test
    //    public void testStandaloneSetupType() throws LxcCreateException {
    //        ZookeeperClusterConfig config = mock( ZookeeperClusterConfig.class );
    //        when( config.getClusterName() ).thenReturn( "test-cluster" );
    //        when( config.getHadoopClusterName() ).thenReturn( "test-hadoop" );
    //        when( config.getSetupType() ).thenReturn( SetupType.STANDALONE );
    //        when( config.getTemplateName() ).thenReturn( "hadoop" );
    //        Set<Agent> agents = new HashSet<>( Arrays.asList( CommonMockBuilder.createAgent() ));
    //        when( config.getNodes() ).thenReturn( agents );
    //        ZookeeperImpl zookeeperMock = mock( ZookeeperImpl.class );
    //        when( zookeeperMock.getHadoopManager() ).thenReturn( mock( Hadoop.class ) );
    //        when( zookeeperMock.getAgentManager() ).thenReturn( mock( AgentManager.class ) );
    //
    //        final CommandRunner commandRunnerMock = mock( CommandRunner.class );
    //        doAnswer(new Answer() {
    //            public Object answer(InvocationOnMock invocation) {
    //                Object[] args = invocation.getArguments();
    //                return null;
    //            }}).when( commandRunnerMock ).runCommand( any( Command.class ) );
    //
    //        when( zookeeperMock.getCommandRunner() ).thenReturn( commandRunnerMock );
    //        when( zookeeperMock.getTracker() ).thenReturn( new TrackerMock() );
    //        when( zookeeperMock.getContainerManager() ).thenReturn( mock( ContainerManager.class ) );
    //        when( zookeeperMock.getHadoopManager() ).thenReturn( mock( Hadoop.class ) );
    //        when( zookeeperMock.getCluster( anyString() ) ).thenReturn( config );
    //        Set<Agent> agentSet = new HashSet<>(Arrays.asList( CommonMockBuilder.createAgent()));
    //        when( zookeeperMock.getContainerManager().clone( config.getTemplateName(), 1, null,
    // PlacementStrategy.ROUND_ROBIN ) ).thenReturn( agentSet );
    //        AbstractOperationHandler operationHandler = new AddPropertyOperationHandler( zookeeperMock,
    // "test-cluster" , "test-file", "test-property", "test-value" );
    //        operationHandler.run();
    //
    //        assertTrue( operationHandler.getTrackerOperation().getLog().contains( "not exist" ) );
    //        assertEquals( operationHandler.getTrackerOperation().getState(), ProductOperationState.FAILED );
    //    }
}
