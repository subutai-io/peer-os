package org.safehaus.subutai.plugin.zookeeper.impl;


import org.junit.Ignore;
import org.junit.Test;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.OperationState;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentBuildException;
import org.safehaus.subutai.core.peer.api.LocalPeer;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.plugin.common.api.ClusterOperationType;
import org.safehaus.subutai.plugin.common.mock.TrackerMock;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.zookeeper.api.SetupType;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.impl.handler.ZookeeperClusterOperationHandler;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class AddEnvironmentContainerNodeOperationHandlerTest
{

    @Test
    public void testWithoutSetupType()
    {
        ZookeeperImpl zookeeperMock = mock( ZookeeperImpl.class );
        ZookeeperClusterConfig config = mock( ZookeeperClusterConfig.class );
        when( zookeeperMock.getHadoopManager() ).thenReturn( mock( Hadoop.class ) );
        when( zookeeperMock.getTracker() ).thenReturn( new TrackerMock() );
        when( zookeeperMock.getEnvironmentManager() ).thenReturn( mock( EnvironmentManager.class ) );
        when( zookeeperMock.getHadoopManager() ).thenReturn( mock( Hadoop.class ) );
        when( zookeeperMock.getCluster( anyString() ) ).thenReturn( null );
        when( config.getClusterName() ).thenReturn( "test" );
        AbstractOperationHandler operationHandler =
                new ZookeeperClusterOperationHandler( zookeeperMock, config, ClusterOperationType.ADD );
        operationHandler.run();

        assertTrue( operationHandler.getTrackerOperation().getLog().contains( "Not supported SetupType" ) );
        assertEquals( operationHandler.getTrackerOperation().getState(), OperationState.FAILED );
    }


    @Ignore
    @Test
    public void testWithStandaloneSetupType() throws EnvironmentBuildException
    {
        ZookeeperImpl zookeeperMock = mock( ZookeeperImpl.class );
        when( zookeeperMock.getHadoopManager() ).thenReturn( mock( Hadoop.class ) );
        when( zookeeperMock.getTracker() ).thenReturn( new TrackerMock() );
        when( zookeeperMock.getEnvironmentManager() ).thenReturn( mock( EnvironmentManager.class ) );
        when( zookeeperMock.getHadoopManager() ).thenReturn( mock( Hadoop.class ) );
        when( zookeeperMock.getPeerManager() ).thenReturn( mock( PeerManager.class ) );
        when( zookeeperMock.getPeerManager().getLocalPeer() ).thenReturn( mock( LocalPeer.class ) );

        ZookeeperClusterConfig config = mock( ZookeeperClusterConfig.class );
        when( config.getHadoopClusterName() ).thenReturn( "test-hadoop" );
        when( config.getTemplateName() ).thenReturn( "zookeeper" );
        when( config.getSetupType() ).thenReturn( SetupType.STANDALONE );


        AbstractOperationHandler operationHandler =
                new ZookeeperClusterOperationHandler( zookeeperMock, config, ClusterOperationType.ADD );
        operationHandler.run();

        assertTrue( operationHandler.getTrackerOperation().getLog().contains( "not supported" ) );
        assertEquals( operationHandler.getTrackerOperation().getState(), OperationState.FAILED );
    }
}
