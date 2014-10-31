package org.safehaus.subutai.plugin.cassandra.impl;


import org.junit.Before;
import org.junit.Test;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

//import org.safehaus.subutai.plugin.common.mock.TrackerMock;


public class CheckEnvironmentContainerNodeHandlerTest
{

    CassandraImpl cassandraMock;


    @Before
    public void setup()
    {
        cassandraMock = mock( CassandraImpl.class );
//        when( cassandraMock.getTracker() ).thenReturn( new TrackerMock() );
        when( cassandraMock.getCluster( anyString() ) ).thenReturn( null );
    }


    @Test
    public void testWithoutCluster()
    {
//        AbstractOperationHandler operationHandler =
//                new CheckNodeHandler( cassandraMock, "test-cluster", UUID.randomUUID() );
//        operationHandler.run();
//        assertTrue( operationHandler.getTrackerOperation().getLog().contains( "not exist" ) );
//        assertEquals( operationHandler.getTrackerOperation().getState(), OperationState.FAILED );
    }


    @Test
    public void testWithNotConnectedAgents()
    {
        //        when( cassandraMock.getCluster( anyString() ) ).thenReturn( new CassandraClusterConfig() );
        //        AbstractOperationHandler operationHandler = new CheckNodeHandler( cassandraMock, "test-cluster",
        // UUID.randomUUID() );
        //        operationHandler.run();
        //        assertTrue( operationHandler.getTrackerOperation().getLog().contains( "not connected" ) );
        //        assertEquals( operationHandler.getTrackerOperation().getState(), OperationState.FAILED );
    }
}
