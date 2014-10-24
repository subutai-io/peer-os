package org.safehaus.subutai.plugin.cassandra.impl;


import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.container.api.container.ContainerManager;
import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.plugin.cassandra.api.CassandraClusterConfig;
import org.safehaus.subutai.plugin.cassandra.impl.handler.CheckNodeHandler;
import org.safehaus.subutai.plugin.cassandra.impl.handler.CheckServiceHandler;
import org.safehaus.subutai.plugin.common.PluginDao;
import org.safehaus.subutai.plugin.common.mock.TrackerMock;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class CheckServiceHandlerTest
{

    CassandraImpl cassandraMock;


    @Before
    public void setup() throws DBException
    {
        cassandraMock = mock( CassandraImpl.class );
        when( cassandraMock.getAgentManager() ).thenReturn( mock( AgentManager.class ) );
        when( cassandraMock.getCommandRunner() ).thenReturn( mock( CommandRunner.class ) );
        when( cassandraMock.getTracker() ).thenReturn( new TrackerMock() );
        when( cassandraMock.getContainerManager() ).thenReturn( mock( ContainerManager.class ) );
        when( cassandraMock.getCluster( anyString() ) ).thenReturn( null );
        when( cassandraMock.getPluginDAO() ).thenReturn( mock( PluginDao.class ) );
        when( cassandraMock.getPluginDAO().getInfo( CassandraClusterConfig.PRODUCT_KEY.toLowerCase(), "Cassandra",
                CassandraClusterConfig.class ) ).thenReturn( null );
    }


    @Test
    public void testWithoutCluster()
    {

        AbstractOperationHandler operationHandler =
                new CheckServiceHandler( cassandraMock, "test-cluster", "test-uuid" );
        operationHandler.run();

        assertTrue( operationHandler.getTrackerOperation().getLog().contains( "not exist" ) );
        assertEquals( operationHandler.getTrackerOperation().getState(), ProductOperationState.FAILED );
    }


    @Test
    public void testWithNotConnectedAgents()
    {
        when( cassandraMock.getCluster( anyString() ) ).thenReturn( new CassandraClusterConfig() );
        when( cassandraMock.getAgentManager().getAgentByHostname( anyString() ) ).thenReturn( null );
        AbstractOperationHandler operationHandler = new CheckNodeHandler( cassandraMock, "test-cluster", "test-node" );
        operationHandler.run();
        assertTrue( operationHandler.getTrackerOperation().getLog().contains( "not connected" ) );
        assertEquals( operationHandler.getTrackerOperation().getState(), ProductOperationState.FAILED );
    }
}
