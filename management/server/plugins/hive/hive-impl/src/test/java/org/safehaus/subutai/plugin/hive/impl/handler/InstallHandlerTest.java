//package org.safehaus.subutai.plugin.hive.impl.handler;
//
//
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//import org.safehaus.subutai.common.tracker.OperationState;
//import org.safehaus.subutai.common.tracker.TrackerOperation;
//import org.safehaus.subutai.plugin.common.mock.CommonMockBuilder;
//import org.safehaus.subutai.plugin.hive.api.HiveConfig;
//import org.safehaus.subutai.plugin.hive.api.SetupType;
//import org.safehaus.subutai.plugin.hive.impl.handler.mock.HiveImplMock;
//
//
//public class InstallHandlerTest
//{
//
//    private HiveImplMock mock = new HiveImplMock();
//    private AbstractHandler handler;
//
//
//    @Before
//    public void setUp()
//    {
//        mock = new HiveImplMock();
//    }
//
//
//    @Test( expected = NullPointerException.class )
//    public void testWithNullConfig()
//    {
//        handler = new InstallHandler( mock, null );
//        handler.run();
//    }
//
//
//    @Test
//    public void testWithExistingConfig()
//    {
//        HiveConfig config = new HiveConfig();
//        config.setSetupType( SetupType.OVER_HADOOP );
//        config.setClusterName( "test-cluster" );
//        mock.setConfig( config );
//
//        handler = new InstallHandler( mock, config );
//        handler.run();
//
//        TrackerOperation po = handler.getTrackerOperation();
//        Assert.assertTrue( po.getLog().toLowerCase().contains( "exists" ) );
//        Assert.assertTrue( po.getLog().contains( config.getClusterName() ) );
//        Assert.assertEquals( po.getState(), OperationState.FAILED );
//    }
//
//
//    @Test
//    public void testWithoutServerNode()
//    {
//        HiveConfig config = new HiveConfig();
//        config.setSetupType( SetupType.OVER_HADOOP );
//        config.setClusterName( "test-cluster" );
//        config.setServer( CommonMockBuilder.createAgent() );
//
//        handler = new InstallHandler( mock, config );
//        handler.run();
//
//        TrackerOperation po = handler.getTrackerOperation();
//        Assert.assertEquals( po.getState(), OperationState.FAILED );
//    }
//}
