//package org.safehaus.subutai.plugin.presto.impl;
//
//
//import java.util.Arrays;
//import java.util.HashSet;
//import java.util.UUID;
//
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
//import org.safehaus.subutai.common.protocol.Agent;
//import org.safehaus.subutai.common.tracker.OperationState;
//import org.safehaus.subutai.common.tracker.TrackerOperation;
//import org.safehaus.subutai.plugin.common.api.NodeOperationType;
//import org.safehaus.subutai.plugin.common.mock.CommonMockBuilder;
//import org.safehaus.subutai.plugin.presto.api.PrestoClusterConfig;
//import org.safehaus.subutai.plugin.presto.impl.handler.NodeOperationHanler;
//import org.safehaus.subutai.plugin.presto.impl.mock.PrestoImplMock;
//
//
//public class CheckEnvironmentContainerNodeOperationHandlerTest
//{
//    private PrestoImplMock mock;
//    private AbstractOperationHandler handler;
//
//
//    @Before
//    public void setUp()
//    {
//        mock = new PrestoImplMock();
//        handler = new NodeOperationHanler( mock, "test-cluster", "test-host", NodeOperationType.STATUS );
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
//    public void testWithUnconnectedAgents()
//    {
//        PrestoClusterConfig config = new PrestoClusterConfig();
//        config.setClusterName( "test-cluster" );
//        config.setWorkers( new HashSet<UUID>( Arrays.asList( CommonMockBuilder.createAgent().getUuid() ) ) );
//        config.setCoordinatorNode( CommonMockBuilder.createAgent().getUuid() );
//        mock.setClusterConfig( config );
//
//        handler.run();
//
//        TrackerOperation po = handler.getTrackerOperation();
//        Assert.assertTrue( po.getLog().toLowerCase().contains( "not connected" ) );
//        Assert.assertEquals( po.getState(), OperationState.FAILED );
//    }
//}
