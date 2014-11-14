//package org.safehaus.subutai.plugin.presto.impl;
//
//
//import java.util.Arrays;
//import java.util.HashSet;
//
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
//import org.safehaus.subutai.common.tracker.OperationState;
//import org.safehaus.subutai.common.tracker.TrackerOperation;
//import org.safehaus.subutai.plugin.common.api.ClusterOperationType;
//import org.safehaus.subutai.plugin.common.mock.CommonMockBuilder;
//import org.safehaus.subutai.plugin.presto.api.PrestoClusterConfig;
//import org.safehaus.subutai.plugin.presto.api.SetupType;
//import org.safehaus.subutai.plugin.presto.impl.handler.ClusterOperationHandler;
//import org.safehaus.subutai.plugin.presto.impl.mock.PrestoImplMock;
//
//
//public class InstallOperationHandlerTest
//{
//
//    private PrestoImplMock mock;
//    private AbstractOperationHandler handler;
//
//
//    @Before
//    public void setUp()
//    {
//        mock = new PrestoImplMock();
//    }
//
//
//    @Test( expected = NullPointerException.class )
//    public void testWithNullConfig()
//    {
//        handler = new ClusterOperationHandler( mock, null, ClusterOperationType.INSTALL );
//        handler.run();
//    }
//
//
//    @Test
//    public void testWithInvalidConfig()
//    {
//        PrestoClusterConfig config = new PrestoClusterConfig();
//        config.setSetupType( SetupType.OVER_HADOOP );
//        config.setClusterName( "test" );
//        handler = new ClusterOperationHandler( mock, config, ClusterOperationType.INSTALL );
//        handler.run();
//
//        TrackerOperation po = handler.getTrackerOperation();
//        Assert.assertTrue( po.getLog().toLowerCase().contains( "malformed" ) );
//        Assert.assertEquals( po.getState(), OperationState.FAILED );
//    }
//
//
//    @Test
//    public void testWithExistingCluster()
//    {
//        PrestoClusterConfig config = new PrestoClusterConfig();
//        config.setSetupType( SetupType.OVER_HADOOP );
//        config.setClusterName( "test-cluster" );
//        config.setWorkers( new HashSet<>( Arrays.asList( CommonMockBuilder.createAgent().getUuid() ) ) );
//        config.setCoordinatorNode( CommonMockBuilder.createAgent().getUuid() );
//
//        mock.setClusterConfig( config );
//        handler = new ClusterOperationHandler( mock, config, ClusterOperationType.INSTALL );
//        handler.run();
//
//        TrackerOperation po = handler.getTrackerOperation();
//        Assert.assertTrue( po.getLog().toLowerCase().contains( "exists" ) );
//        Assert.assertTrue( po.getLog().toLowerCase().contains( config.getClusterName() ) );
//        Assert.assertEquals( po.getState(), OperationState.FAILED );
//    }
//}
//
