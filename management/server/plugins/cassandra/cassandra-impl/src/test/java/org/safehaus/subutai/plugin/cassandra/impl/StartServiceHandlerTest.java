package org.safehaus.subutai.plugin.cassandra.impl;


import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.OperationState;
import org.safehaus.subutai.plugin.cassandra.impl.handler.StartServiceHandler;
import org.safehaus.subutai.plugin.common.mock.TrackerMock;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class StartServiceHandlerTest
{
    CassandraImpl cassandraMock;


    @Before
    public void setup()
    {
        cassandraMock = mock( CassandraImpl.class );
        when( cassandraMock.getTracker() ).thenReturn( new TrackerMock() );
        when( cassandraMock.getCluster( anyString() ) ).thenReturn( null );
    }


    @Test
    public void testWithoutCluster()
    {
        AbstractOperationHandler operationHandler =
                new StartServiceHandler( cassandraMock, "test-cluster", UUID.randomUUID() );
        operationHandler.run();
        assertTrue( operationHandler.getTrackerOperation().getLog().contains( "not exist" ) );
        assertEquals( operationHandler.getTrackerOperation().getState(), OperationState.FAILED );
    }
}
