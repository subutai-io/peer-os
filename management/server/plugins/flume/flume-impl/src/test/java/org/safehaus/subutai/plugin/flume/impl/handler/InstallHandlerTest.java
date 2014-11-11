//package org.safehaus.subutai.plugin.flume.impl.handler;
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
//import org.safehaus.subutai.plugin.flume.api.FlumeConfig;
//import org.safehaus.subutai.plugin.flume.api.SetupType;
//import org.safehaus.subutai.plugin.flume.impl.handler.mock.FlumeImplMock;
//
//
//public class InstallHandlerTest
//{
//
//    private FlumeImplMock mock;
//    private AbstractOperationHandler handler;
//
//
//    @Before
//    public void setUp()
//    {
//        mock = new FlumeImplMock();
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
//        FlumeConfig config = new FlumeConfig();
//        config.setSetupType( SetupType.OVER_HADOOP );
//        config.setClusterName( "test-cluster" );
//        handler = new ClusterOperationHandler( mock, config, ClusterOperationType.INSTALL );
//        handler.run();
//
//        TrackerOperation po = handler.getTrackerOperation();
//        Assert.assertTrue( po.getLog().toLowerCase().contains( "invalid" ) );
//        Assert.assertEquals( po.getState(), OperationState.FAILED );
//    }
//
//
//    @Test
//    public void testWithExistingCluster()
//    {
//        FlumeConfig config = new FlumeConfig();
//        config.setSetupType( SetupType.OVER_HADOOP );
//        config.setClusterName( "test-cluster" );
//        //config.setNodes( new HashSet<>( Arrays.asList( CommonMockBuilder.createAgent() ) ) );
//
//        mock.setConfig( config );
//        handler = new ClusterOperationHandler( mock, config, ClusterOperationType.INSTALL );
//        handler.run();
//
//        TrackerOperation po = handler.getTrackerOperation();
//        Assert.assertTrue( po.getLog().toLowerCase().contains( "exists" ) );
//        Assert.assertTrue( po.getLog().toLowerCase().contains( config.getClusterName() ) );
//        Assert.assertEquals( po.getState(), OperationState.FAILED );
//    }
//}
