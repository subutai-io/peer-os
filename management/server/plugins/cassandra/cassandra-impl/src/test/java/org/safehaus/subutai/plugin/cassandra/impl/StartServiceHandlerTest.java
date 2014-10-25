package org.safehaus.subutai.plugin.cassandra.impl;


import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.OperationState;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.container.api.container.ContainerManager;
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
        when( cassandraMock.getAgentManager() ).thenReturn( mock( AgentManager.class ) );
        when( cassandraMock.getCommandRunner() ).thenReturn( mock( CommandRunner.class ) );
        when( cassandraMock.getTracker() ).thenReturn( new TrackerMock() );
        when( cassandraMock.getContainerManager() ).thenReturn( mock( ContainerManager.class ) );
        when( cassandraMock.getCluster( anyString() ) ).thenReturn( null );
    }


    @Test
    public void testWithoutCluster()
    {
        AbstractOperationHandler operationHandler =
                new StartServiceHandler( cassandraMock, "test-cluster", "test-node" );
        operationHandler.run();
        assertTrue( operationHandler.getTrackerOperation().getLog().contains( "not exist" ) );
        assertEquals( operationHandler.getTrackerOperation().getState(), OperationState.FAILED );
    }
}
