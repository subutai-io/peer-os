package org.safehaus.subutai.plugin.cassandra.impl;


import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.safehaus.subutai.plugin.cassandra.api.CassandraClusterConfig;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

//import org.safehaus.subutai.plugin.common.PluginDao;
//import org.safehaus.subutai.plugin.common.mock.TrackerMock;


public class InstallServiceHandlerTest
{

    CassandraImpl cassandraMock;


    @Before
    public void setup()
    {
        cassandraMock = mock( CassandraImpl.class );
//        when( cassandraMock.getTracker() ).thenReturn( new TrackerMock() );
        when( cassandraMock.getCluster( anyString() ) ).thenReturn( null );
//        when( cassandraMock.getPluginDAO() ).thenReturn( mock( PluginDao.class ) );
        when( cassandraMock.getPluginDAO().getInfo( CassandraClusterConfig.PRODUCT_KEY.toLowerCase(), "Cassandra",
                CassandraClusterConfig.class ) ).thenReturn( null );
    }


    @Ignore
    @Test
    public void testWithoutCluster()
    {

//        CassandraClusterConfig config = mock( CassandraClusterConfig.class );
//        when( config.getClusterName() ).thenReturn( "test-cluster" );
//
//        AbstractOperationHandler operationHandler = new InstallClusterHandler( cassandraMock, config );
//        operationHandler.run();
//
//        assertTrue( operationHandler.getTrackerOperation().getLog().contains( "not exist" ) );
//        assertEquals( operationHandler.getTrackerOperation().getState(), OperationState.FAILED );
    }
}
