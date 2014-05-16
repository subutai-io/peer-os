package org.safehaus.kiskis.mgmt.impl.lucene.handler;


import org.junit.Test;
import org.safehaus.kiskis.mgmt.api.lucene.Config;
import org.safehaus.kiskis.mgmt.impl.lucene.LuceneImpl;
import org.safehaus.kiskis.mgmt.impl.lucene.mock.LuceneImplMock;
import org.safehaus.kiskis.mgmt.shared.operation.AbstractOperationHandler;
import org.safehaus.kiskis.mgmt.shared.operation.ProductOperationState;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;


public class UninstallOperationHandlerTest {


    @Test
    public void testWithoutCluster() {
        AbstractOperationHandler operationHandler = new UninstallOperationHandler( new LuceneImplMock(), "test-cluster" );

        operationHandler.run();

        assertTrue( operationHandler.getProductOperation().getLog().contains( "not exist" ) );
        assertEquals( operationHandler.getProductOperation().getState(), ProductOperationState.FAILED );
    }


    @Test
    public void testWithExistingCluster() {
        LuceneImpl pigImpl = new LuceneImplMock().setClusterConfig( new Config() );
        AbstractOperationHandler operationHandler = new UninstallOperationHandler( pigImpl, "test-cluster" );

        operationHandler.run();

        assertTrue( operationHandler.getProductOperation().getLog().contains( "Uninstallation failed" ) );
        assertEquals( operationHandler.getProductOperation().getState(), ProductOperationState.FAILED );
    }

}
