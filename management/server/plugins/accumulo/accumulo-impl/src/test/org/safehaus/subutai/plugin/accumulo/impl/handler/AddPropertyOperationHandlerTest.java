package org.safehaus.subutai.plugin.accumulo.impl.handler;


import org.junit.Test;
import org.safehaus.subutai.plugin.accumulo.api.AccumuloClusterConfig;
import org.safehaus.subutai.plugin.accumulo.impl.AccumuloImpl;
import org.safehaus.subutai.plugin.accumulo.impl.handler.mock.AccumuloImplMock;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperationState;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;


public class AddPropertyOperationHandlerTest {

    @Test
    public void testWithoutCluster() {
        AbstractOperationHandler operationHandler = new AddPropertyOperationHandler( new AccumuloImplMock(), "test-cluster", "test-property", "test-value" );

        operationHandler.run();

        assertTrue( operationHandler.getProductOperation().getLog().contains( "not exist" ) );
        assertEquals( operationHandler.getProductOperation().getState(), ProductOperationState.FAILED );
    }


    @Test
    public void testAddingPropertyFail() {
        AccumuloImpl accumuloImpl = new AccumuloImplMock().setClusterAccumuloClusterConfig( new AccumuloClusterConfig() );
        AbstractOperationHandler operationHandler = new AddPropertyOperationHandler( accumuloImpl, "test-cluster", "test-property", "test-value" );
        System.out.println( operationHandler );
        operationHandler.run();

        assertTrue( operationHandler.getProductOperation().getLog().contains( "Adding property failed" ) );
        assertEquals( operationHandler.getProductOperation().getState(), ProductOperationState.FAILED );
    }
}
