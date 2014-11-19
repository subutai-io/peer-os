//package org.safehaus.subutai.plugin.presto.impl;
//
//
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
//import org.safehaus.subutai.common.tracker.OperationState;
//import org.safehaus.subutai.common.tracker.TrackerOperation;
//import org.safehaus.subutai.core.environment.api.EnvironmentManager;
//import org.safehaus.subutai.plugin.common.api.NodeOperationType;
//import org.safehaus.subutai.plugin.common.mock.TrackerMock;
//import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
//import org.safehaus.subutai.plugin.presto.api.PrestoClusterConfig;
//import org.safehaus.subutai.plugin.presto.impl.handler.NodeOperationHanler;
//import org.safehaus.subutai.plugin.presto.impl.mock.PrestoImplMock;
//
//import static junit.framework.TestCase.assertEquals;
//import static org.junit.Assert.assertTrue;
//import static org.mockito.Matchers.anyString;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//
//
//public class StartEnvironmentContainerNodeOperationHandlerTest
//{
//
//
//    @Test
//    public void testWithoutCluster()
//    {
//
//        PrestoImpl prestoMock = mock( PrestoImpl.class );
//        when( prestoMock.getHadoopManager() ).thenReturn( mock( Hadoop.class ) );
//        when( prestoMock.getTracker() ).thenReturn( new TrackerMock() );
//        when( prestoMock.getEnvironmentManager() ).thenReturn( mock( EnvironmentManager.class ) );
//        when( prestoMock.getHadoopManager() ).thenReturn( mock( Hadoop.class ) );
//        when( prestoMock.getCluster( anyString() ) ).thenReturn( null );
//        AbstractOperationHandler operationHandler =
//                new NodeOperationHanler( prestoMock, "test-cluster", "test-node", NodeOperationType.START );
//        operationHandler.run();
//
//        assertTrue( operationHandler.getTrackerOperation().getLog().contains( "not exist" ) );
//        assertEquals( operationHandler.getTrackerOperation().getState(), OperationState.FAILED );
//    }
//
//
//    @Test
//    public void testFail()
//    {
//        PrestoImpl prestoMock = mock( PrestoImpl.class );
//        AbstractOperationHandler operationHandler =
//                new NodeOperationHanler( prestoMock, "test-cluster", "test-node", NodeOperationType.START );
//
//        operationHandler.run();
//
//        TrackerOperation po = operationHandler.getTrackerOperation();
//        assertTrue( po.getLog().contains( "not connected" ) );
//        Assert.assertEquals( po.getState(), OperationState.FAILED );
//    }
//}