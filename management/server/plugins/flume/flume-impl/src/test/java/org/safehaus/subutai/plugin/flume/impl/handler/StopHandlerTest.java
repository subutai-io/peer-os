//package org.safehaus.subutai.plugin.flume.impl.handler;
//
//
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
//import org.safehaus.subutai.common.tracker.OperationState;
//import org.safehaus.subutai.common.tracker.TrackerOperation;
//import org.safehaus.subutai.plugin.common.api.NodeOperationType;
//import org.safehaus.subutai.plugin.flume.api.FlumeConfig;
//import org.safehaus.subutai.plugin.flume.impl.handler.mock.FlumeImplMock;
//
//
//public class StopHandlerTest
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
//        handler = new NodeOperationHandler( mock, "test-cluster", "test-host", NodeOperationType.STOP);
//    }
//
//
//    @Test
//    public void testWithoutCluster()
//    {
//
//        handler.run();
//
//        TrackerOperation po = handler.getTrackerOperation();
//        Assert.assertTrue( po.getLog().contains( "not exist" ) );
//        Assert.assertEquals( po.getState(), OperationState.FAILED );
//    }
//
//
//    @Test
//    public void testFail()
//    {
//        mock.setConfig( new FlumeConfig() );
//        handler.run();
//
//        TrackerOperation po = handler.getTrackerOperation();
//        Assert.assertTrue( po.getLog().contains( "not connected" ) );
//        Assert.assertEquals( po.getState(), OperationState.FAILED );
//    }
//}
