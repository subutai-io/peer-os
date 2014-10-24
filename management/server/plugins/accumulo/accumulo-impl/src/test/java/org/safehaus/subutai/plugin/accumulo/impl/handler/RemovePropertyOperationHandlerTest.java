package org.safehaus.subutai.plugin.accumulo.impl.handler;


import org.junit.Ignore;
import org.junit.Test;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.plugin.accumulo.impl.handler.mock.AccumuloImplMock;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;


@Ignore
public class RemovePropertyOperationHandlerTest
{
    @Test
    public void testWithoutCluster()
    {
        AbstractOperationHandler operationHandler =
                new RemovePropertyOperationHandler( new AccumuloImplMock(), "test-cluster", "test-property" );

        operationHandler.run();

        assertTrue( operationHandler.getTrackerOperation().getLog().contains( "not exist" ) );
        assertEquals( operationHandler.getTrackerOperation().getState(), ProductOperationState.FAILED );
    }
}
