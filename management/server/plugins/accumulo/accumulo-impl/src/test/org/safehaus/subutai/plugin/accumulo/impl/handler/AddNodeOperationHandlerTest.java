package org.safehaus.subutai.plugin.accumulo.impl.handler;


import org.junit.Test;
import org.safehaus.subutai.plugin.accumulo.api.NodeType;
import org.safehaus.subutai.plugin.accumulo.impl.handler.mock.AccumuloImplMock;
import org.safehaus.subutai.plugin.accumulo.impl.handler.mock.MockBuilder;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperationState;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;


public class AddNodeOperationHandlerTest {

    @Test
    public void testWithoutCluster() {
        AbstractOperationHandler operationHandler = new AddNodeOperationHandler( new AccumuloImplMock(), "test-cluster", "test-node", NodeType.TRACER );

        operationHandler.run();

        assertTrue( operationHandler.getProductOperation().getLog().contains( "not exist" ) );
        assertEquals( operationHandler.getProductOperation().getState(), ProductOperationState.FAILED );
    }


    @Test
    public void testAgentNotConnected() {
        AbstractOperationHandler operationHandler = MockBuilder.getAddNodeOperationWithResult( true );

        operationHandler.run();

        assertTrue( operationHandler.getProductOperation().getLog().contains( "not connected" ) );
        assertEquals( operationHandler.getProductOperation().getState(), ProductOperationState.FAILED );
    }
}
