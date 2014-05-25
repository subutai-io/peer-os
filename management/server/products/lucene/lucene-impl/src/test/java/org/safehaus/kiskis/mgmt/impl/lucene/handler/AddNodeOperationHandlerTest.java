package org.safehaus.kiskis.mgmt.impl.lucene.handler;


import org.junit.Test;
import org.safehaus.kiskis.mgmt.api.lucene.Config;
import org.safehaus.kiskis.mgmt.impl.lucene.LuceneImpl;
import org.safehaus.kiskis.mgmt.impl.lucene.mock.LuceneImplMock;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperationState;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;


public class AddNodeOperationHandlerTest {


    @Test
    public void testWithoutCluster() {
        AbstractOperationHandler operationHandler = new AddNodeOperationHandler( new LuceneImplMock(), "test-cluster",
                "lxc-host" );

        operationHandler.run();

        assertTrue( operationHandler.getProductOperation().getLog().contains( "not exist" ) );
        assertEquals( operationHandler.getProductOperation().getState(), ProductOperationState.FAILED );
    }


    @Test
    public void testWithExistingCluster() {
        LuceneImpl impl = new LuceneImplMock().setClusterConfig( new Config() );
        AbstractOperationHandler operationHandler = new AddNodeOperationHandler( impl, "test-cluster", "lxc-host" );

        operationHandler.run();

        assertTrue( operationHandler.getProductOperation().getLog().contains( "not connected" ) );
        assertEquals( operationHandler.getProductOperation().getState(), ProductOperationState.FAILED );
    }

}
