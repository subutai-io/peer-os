//package org.safehaus.subutai.plugin.presto.impl;
//
//
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Ignore;
//import org.junit.Test;
//import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
//import org.safehaus.subutai.common.tracker.OperationState;
//import org.safehaus.subutai.common.tracker.TrackerOperation;
//import org.safehaus.subutai.plugin.common.api.ClusterOperationType;
//import org.safehaus.subutai.plugin.presto.api.PrestoClusterConfig;
//import org.safehaus.subutai.plugin.presto.api.SetupType;
//import org.safehaus.subutai.plugin.presto.impl.handler.ClusterOperationHandler;
//import org.safehaus.subutai.plugin.presto.impl.mock.PrestoImplMock;
//
//
//public class UninstallOperationHandlerTest
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
//
//        handler = new ClusterOperationHandler( mock, mock.getCluster( "test-cluster" ), ClusterOperationType.UNINSTALL );
//    }
//
//
//    @Test
//    public void testWithoutCluster()
//    {
//        handler.run();
//
//        TrackerOperation po = handler.getTrackerOperation();
//        Assert.assertTrue( po.getLog().toLowerCase().contains( "not exist" ) );
//        Assert.assertEquals( po.getState(), OperationState.FAILED );
//    }
//
//
//    @Ignore
//    @Test
//    public void testWithExistingCluster()
//    {
//        PrestoClusterConfig config = new PrestoClusterConfig();
//        config.setSetupType( SetupType.OVER_HADOOP );
//        mock.setClusterConfig( config );
//        handler.run();
//
//        TrackerOperation po = handler.getTrackerOperation();
//        Assert.assertTrue( po.getLog().toLowerCase().contains( "uninstallation failed" ) );
//        Assert.assertEquals( po.getState(), OperationState.FAILED );
//    }
//}
