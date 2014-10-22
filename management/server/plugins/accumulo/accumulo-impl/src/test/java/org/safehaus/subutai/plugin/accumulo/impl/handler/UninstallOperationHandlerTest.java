package org.safehaus.subutai.plugin.accumulo.impl.handler;


import org.junit.Ignore;
import org.junit.Test;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.plugin.accumulo.api.AccumuloClusterConfig;
import org.safehaus.subutai.plugin.accumulo.impl.AccumuloImpl;
import org.safehaus.subutai.plugin.accumulo.impl.handler.mock.AccumuloImplMock;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;


@Ignore
public class UninstallOperationHandlerTest
{

    @Test
    public void testWithoutCluster()
    {
        AbstractOperationHandler operationHandler =
                new CheckNodeOperationHandler( new AccumuloImplMock(), "test-cluster", "test-node" );

        operationHandler.run();

        assertTrue( operationHandler.getTrackerOperation().getLog().contains( "not exist" ) );
        assertEquals( operationHandler.getTrackerOperation().getState(), ProductOperationState.FAILED );
    }


    @Test
    public void testAgentNotConnected()
    {
        AccumuloImpl accumuloImpl =
                new AccumuloImplMock().setClusterAccumuloClusterConfig( new AccumuloClusterConfig() );
        AbstractOperationHandler operationHandler =
                new CheckNodeOperationHandler( accumuloImpl, "test-cluster", "test-node" );
        operationHandler.run();

        assertTrue( operationHandler.getTrackerOperation().getLog().contains( "not connected" ) );
        assertEquals( operationHandler.getTrackerOperation().getState(), ProductOperationState.FAILED );
    }
}
