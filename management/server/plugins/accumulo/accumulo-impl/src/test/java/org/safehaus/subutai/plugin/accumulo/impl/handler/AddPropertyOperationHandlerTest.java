package org.safehaus.subutai.plugin.accumulo.impl.handler;


import org.junit.Ignore;
import org.junit.Test;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.OperationState;
import org.safehaus.subutai.plugin.accumulo.impl.handler.mock.AccumuloImplMock;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;


@Ignore
public class AddPropertyOperationHandlerTest
{

    @Test
    public void testWithoutCluster()
    {
        AbstractOperationHandler operationHandler =
                new AddPropertyOperationHandler( new AccumuloImplMock(), "test-cluster", "test-property",
                        "test-value" );

        operationHandler.run();

        assertTrue( operationHandler.getTrackerOperation().getLog().contains( "not exist" ) );
        assertEquals( operationHandler.getTrackerOperation().getState(), OperationState.FAILED );
    }
}
