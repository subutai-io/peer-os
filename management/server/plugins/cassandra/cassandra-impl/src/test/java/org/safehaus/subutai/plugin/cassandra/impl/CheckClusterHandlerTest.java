package org.safehaus.subutai.plugin.cassandra.impl;


import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;

//import org.safehaus.subutai.plugin.common.PluginDao;
//import org.safehaus.subutai.plugin.common.mock.TrackerMock;


public class CheckClusterHandlerTest
{

    CassandraImpl cassandraMock;


    @Before
    public void setup()
    {
        cassandraMock = mock( CassandraImpl.class );
//        when( cassandraMock.getTracker() ).thenReturn( new TrackerMock() );
//        when( cassandraMock.getCluster( anyString() ) ).thenReturn( null );
//        when( cassandraMock.getPluginDAO() ).thenReturn( mock( PluginDao.class ) );
//        when( cassandraMock.getPluginDAO().getInfo( CassandraClusterConfig.PRODUCT_KEY.toLowerCase(), "Cassandra",
//                CassandraClusterConfig.class ) ).thenReturn( null );
    }


    @Test
    public void testWithoutCluster()
    {

//        AbstractOperationHandler operationHandler = new CheckClusterHandler( cassandraMock, "test-cluster" );
//        operationHandler.run();
//
//        assertTrue( operationHandler.getTrackerOperation().getLog().contains( "not exist" ) );
//        assertEquals( operationHandler.getTrackerOperation().getState(), OperationState.FAILED );
    }
}
