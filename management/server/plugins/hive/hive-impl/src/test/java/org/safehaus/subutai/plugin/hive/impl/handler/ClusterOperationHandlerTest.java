package org.safehaus.subutai.plugin.hive.impl.handler;


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
import org.safehaus.subutai.plugin.common.api.ClusterOperationType;
import org.safehaus.subutai.plugin.common.mock.TrackerMock;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hive.api.HiveConfig;
import org.safehaus.subutai.plugin.hive.impl.HiveImpl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class ClusterOperationHandlerTest
{
    HadoopClusterConfig hadoopClusterConfig;
    HiveConfig hiveConfig;
    HiveImpl hiveImpl;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp(){
        hadoopClusterConfig = mock( HadoopClusterConfig.class );
        when( hadoopClusterConfig.getEnvironmentId() ).thenReturn( UUID.randomUUID() );

        Environment environmentMock = mock( Environment.class );
        when( environmentMock.getId() ).thenReturn( UUID.randomUUID() );

        hiveConfig = mock( HiveConfig.class );
        when( hiveConfig.getServer() ).thenReturn( UUID.randomUUID() );
        when( hiveConfig.getClusterName() ).thenReturn( "test-cluster" );

        hiveImpl = mock( HiveImpl.class );
        when( hiveImpl.getTracker() ).thenReturn( new TrackerMock() );
        when( hiveImpl.getEnvironmentManager() ).thenReturn( mock( EnvironmentManager.class ) );
        when( hiveImpl.getEnvironmentManager().getEnvironmentByUUID( hadoopClusterConfig.getEnvironmentId() ) ).thenReturn(
                environmentMock );

        Set<UUID> set = new HashSet<>( );
        set.add( UUID.randomUUID() );
        set.add( UUID.randomUUID() );
        when( hiveConfig.getClients() ).thenReturn( set );
    }


    @Test
    public void testSetupClusterWithExistingClusterName()
    {
        when( hiveImpl.getCluster( anyString() ) ).thenReturn( hiveConfig );
        AbstractOperationHandler operationHandler =
                new ClusterOperationHandler( hiveImpl, hiveConfig, hadoopClusterConfig, ClusterOperationType.INSTALL );
        operationHandler.run();
        assertTrue( operationHandler.getTrackerOperation().getLog().contains( "already exists" ) );
    }


    @Test
    public void testDestroyCluster()
    {
        AbstractOperationHandler operationHandler =
                new ClusterOperationHandler( hiveImpl, hiveConfig, hadoopClusterConfig, ClusterOperationType.UNINSTALL );
        operationHandler.run();
        assertTrue( operationHandler.getTrackerOperation().getLog().contains( "not exist" ) );
        assertEquals( operationHandler.getTrackerOperation().getState(), OperationState.FAILED );
    }
}
