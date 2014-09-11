package org.safehaus.subutai.plugin.accumulo.impl.handler;

import org.junit.Test;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.plugin.accumulo.api.NodeType;
import org.safehaus.subutai.plugin.accumulo.impl.handler.mock.AccumuloImplMock;
import org.safehaus.subutai.common.tracker.ProductOperationState;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;


public class AddNodeOperationHandlerTest {

    @Test
    public void testWithoutCluster() {
        AbstractOperationHandler operationHandler = new AddNodeOperationHandler( new AccumuloImplMock(), "test-cluster", "test-node", NodeType.TRACER );

        operationHandler.run();

        assertTrue( operationHandler.getProductOperation().getLog().contains( "not exist" ) );
        assertEquals( operationHandler.getProductOperation().getState(), ProductOperationState.FAILED );
    }
}
