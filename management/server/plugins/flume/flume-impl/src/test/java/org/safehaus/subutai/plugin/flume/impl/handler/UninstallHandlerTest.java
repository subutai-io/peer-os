//package org.safehaus.subutai.plugin.flume.impl.handler;
//
//
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
//import org.safehaus.subutai.common.tracker.OperationState;
//import org.safehaus.subutai.common.tracker.TrackerOperation;
//import org.safehaus.subutai.plugin.common.api.ClusterOperationType;
//import org.safehaus.subutai.plugin.flume.api.FlumeConfig;
//import org.safehaus.subutai.plugin.flume.api.SetupType;
//import org.safehaus.subutai.plugin.flume.impl.handler.mock.FlumeImplMock;
//
//
//public class UninstallHandlerTest
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
//        handler = new ClusterOperationHandler( mock, null, ClusterOperationType.DESTROY );
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
//    @Test
//    public void testWithExistingCluster()
//    {
//        FlumeConfig config = new FlumeConfig();
//        config.setSetupType( SetupType.OVER_HADOOP );
//        mock.setConfig( config );
//        handler.run();
//
//        TrackerOperation po = handler.getTrackerOperation();
//        Assert.assertTrue( po.getLog().toLowerCase().contains( "uninstallation failed" ) );
//        Assert.assertEquals( po.getState(), OperationState.FAILED );
//    }
//}
