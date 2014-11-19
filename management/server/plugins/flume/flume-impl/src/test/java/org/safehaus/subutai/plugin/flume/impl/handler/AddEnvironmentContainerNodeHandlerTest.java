//package org.safehaus.subutai.plugin.flume.impl.handler;
//
//
//import java.util.HashSet;
//import java.util.Set;
//
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Ignore;
//import org.junit.Test;
//import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
//import org.safehaus.subutai.common.tracker.OperationState;
//import org.safehaus.subutai.common.tracker.TrackerOperation;
//import org.safehaus.subutai.core.environment.api.helper.Environment;
//import org.safehaus.subutai.core.peer.api.ContainerHost;
//import org.safehaus.subutai.plugin.common.api.NodeOperationType;
//import org.safehaus.subutai.plugin.flume.api.FlumeConfig;
//import org.safehaus.subutai.plugin.flume.api.SetupType;
//import org.safehaus.subutai.plugin.flume.impl.FlumeImpl;
//import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
//
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//
//
//@Ignore
//public class AddEnvironmentContainerNodeHandlerTest
//{
//    private AbstractOperationHandler handler;
//    private FlumeConfig config;
//    private FlumeImpl flumeMock;
//
//
//    @Before
//    public void setUp()
//    {
//        flumeMock = mock( FlumeImpl.class );
//        config = mock( FlumeConfig.class );
//        handler = new NodeOperationHandler( flumeMock, "test-cluster", "", NodeOperationType.INSTALL );
//
//    }
//
//
//    @Test
//    public void testWithoutCluster()
//    {
//        Environment environment = flumeMock.getEnvironmentManager().getEnvironmentByUUID( config.getEnvironmentId() );
//        Set<ContainerHost> containers = new HashSet<>( );
//
//
//        when( flumeMock ).thenReturn( mock( Hadoop.class ) );
//        handler.run();
//
//        TrackerOperation po = handler.getTrackerOperation();
//        Assert.assertTrue( po.getLog().toLowerCase().contains( "not found" ) );
//        Assert.assertEquals( po.getState(), OperationState.FAILED );
//    }
//
//
//    @Test
//    public void testWithExistingCluster()
//    {
//        FlumeConfig config = new FlumeConfig();
//        config.setSetupType( SetupType.OVER_HADOOP );
//        //mock.setConfig( config );
//        handler.run();
//
//        TrackerOperation po = handler.getTrackerOperation();
//        Assert.assertTrue( po.getLog().toLowerCase().contains( "not connected" ) );
//        Assert.assertEquals( po.getState(), OperationState.FAILED );
//    }
//}
