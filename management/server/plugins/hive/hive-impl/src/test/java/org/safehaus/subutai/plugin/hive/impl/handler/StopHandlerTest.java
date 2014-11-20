//package org.safehaus.subutai.plugin.hive.impl.handler;
//
//
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//import org.safehaus.subutai.common.tracker.OperationState;
//import org.safehaus.subutai.common.tracker.TrackerOperation;
//import org.safehaus.subutai.plugin.hive.api.HiveConfig;
//import org.safehaus.subutai.plugin.hive.impl.handler.mock.HiveImplMock;
//
//
//public class StopHandlerTest
//{
//
//    HiveImplMock mock;
//    AbstractHandler handler;
//
//
//    @Before
//    public void setUp()
//    {
//        mock = new HiveImplMock();
//        handler = new StatusHandler( mock, "test-cluster", "test-host" );
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
//    public void testFail()
//    {
//        mock.setConfig( new HiveConfig() );
//        handler.run();
//
//        TrackerOperation po = handler.getTrackerOperation();
//        Assert.assertTrue( po.getLog().toLowerCase().contains( "not connected" ) );
//        Assert.assertEquals( po.getState(), OperationState.FAILED );
//    }
//}
