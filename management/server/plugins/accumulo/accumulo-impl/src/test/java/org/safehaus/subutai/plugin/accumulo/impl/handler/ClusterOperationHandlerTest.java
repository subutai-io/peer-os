package org.safehaus.subutai.plugin.accumulo.impl.handler;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.OperationState;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.plugin.accumulo.api.AccumuloClusterConfig;
import org.safehaus.subutai.plugin.accumulo.impl.AccumuloImpl;
import org.safehaus.subutai.plugin.common.api.ClusterOperationType;
import org.safehaus.subutai.plugin.common.mock.TrackerMock;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class ClusterOperationHandlerTest
{
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    HadoopClusterConfig hadoopClusterConfig;
    AccumuloClusterConfig accumuloClusterConfig;
    AccumuloImpl accumuloMock;
    ZookeeperClusterConfig zookeeperClusterConfig;


    @Before
    public void setUp()
    {
        hadoopClusterConfig = mock( HadoopClusterConfig.class );
        when( hadoopClusterConfig.getEnvironmentId() ).thenReturn( UUID.randomUUID() );

        Environment environmentMock = mock( Environment.class );
        when( environmentMock.getId() ).thenReturn( UUID.randomUUID() );

        accumuloClusterConfig = mock( AccumuloClusterConfig.class );
        when( accumuloClusterConfig.getMasterNode() ).thenReturn( UUID.randomUUID() );
        when( accumuloClusterConfig.getGcNode() ).thenReturn( UUID.randomUUID() );
        when( accumuloClusterConfig.getMonitor() ).thenReturn( UUID.randomUUID() );
        when( accumuloClusterConfig.getClusterName() ).thenReturn( "test-cluster" );
        when( accumuloClusterConfig.getInstanceName() ).thenReturn( "test-instance" );
        when( accumuloClusterConfig.getPassword() ).thenReturn( "test-password" );

        accumuloMock = mock( AccumuloImpl.class );
        when( accumuloMock.getTracker() ).thenReturn( new TrackerMock() );
        when( accumuloMock.getEnvironmentManager() ).thenReturn( mock( EnvironmentManager.class ) );
        when( accumuloMock.getEnvironmentManager().getEnvironmentByUUID( hadoopClusterConfig.getEnvironmentId() ) )
                .thenReturn( environmentMock );

        Set<UUID> set = new HashSet<>();
        set.add( UUID.randomUUID() );
        set.add( UUID.randomUUID() );
        when( accumuloClusterConfig.getTracers() ).thenReturn( set );
        when( accumuloClusterConfig.getSlaves() ).thenReturn( set );

        zookeeperClusterConfig = mock( ZookeeperClusterConfig.class );
    }


    @Test
    public void testSetupClusterWithExistingClusterName()
    {
        when( accumuloMock.getCluster( anyString() ) ).thenReturn( accumuloClusterConfig );
        AbstractOperationHandler operationHandler =
                new ClusterOperationHandler( accumuloMock, accumuloClusterConfig, hadoopClusterConfig,
                        zookeeperClusterConfig, ClusterOperationType.INSTALL );

        operationHandler.run();

        assertTrue( operationHandler.getTrackerOperation().getLog().contains( "already a cluster" ) );
        assertEquals( operationHandler.getTrackerOperation().getState(), OperationState.FAILED );
    }


    @Test
    public void testDestroyCluster()
    {
        AbstractOperationHandler operationHandler =
                new ClusterOperationHandler( accumuloMock, accumuloClusterConfig, hadoopClusterConfig,
                        zookeeperClusterConfig, ClusterOperationType.UNINSTALL );
        operationHandler.run();
        assertTrue( operationHandler.getTrackerOperation().getLog().contains( "not exist" ) );
        assertEquals( operationHandler.getTrackerOperation().getState(), OperationState.FAILED );
    }
}
