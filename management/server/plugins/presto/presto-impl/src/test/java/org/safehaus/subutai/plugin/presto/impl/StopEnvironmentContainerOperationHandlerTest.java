//package org.safehaus.subutai.plugin.presto.impl;
//
//
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
//import org.safehaus.subutai.common.tracker.OperationState;
//import org.safehaus.subutai.common.tracker.TrackerOperation;
//import org.safehaus.subutai.plugin.common.api.NodeOperationType;
//import org.safehaus.subutai.plugin.presto.api.PrestoClusterConfig;
//import org.safehaus.subutai.plugin.presto.impl.handler.NodeOperationHanler;
//import org.safehaus.subutai.plugin.presto.impl.mock.PrestoImplMock;
//
//
//public class StopEnvironmentContainerOperationHandlerTest
//{
//    private PrestoImplMock mock;
//    private AbstractOperationHandler handler;
//
//
//    @Before
//    public void setUp()
//    {
//        mock = new PrestoImplMock();
//        handler = new NodeOperationHanler( mock, "test-cluster", "test-host", NodeOperationType.STOP );
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
//    public void testWithNotConnectedAgents()
//    {
//        PrestoClusterConfig config = new PrestoClusterConfig();
//        mock.setClusterConfig( config );
//        handler.run();
//
//        TrackerOperation po = handler.getTrackerOperation();
//        Assert.assertTrue( po.getLog().contains( "not connected" ) );
//        Assert.assertEquals( po.getState(), OperationState.FAILED );
//    }
//}
