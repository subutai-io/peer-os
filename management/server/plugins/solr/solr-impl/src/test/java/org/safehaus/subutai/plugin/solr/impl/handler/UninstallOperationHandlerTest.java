package org.safehaus.subutai.plugin.solr.impl.handler;


/*import org.junit.Ignore;
import org.junit.Test;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.OperationState;
import org.safehaus.subutai.plugin.solr.impl.handler.mock.MockBuilder;
import org.safehaus.subutai.plugin.solr.impl.handler.mock.SolrImplMock;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;


public class UninstallOperationHandlerTest
{

    @Test
    public void testWithoutCluster()
    {
        AbstractOperationHandler operationHandler = new UninstallOperationHandler( new SolrImplMock(), "test-cluster" );

        operationHandler.run();

        assertTrue( operationHandler.getTrackerOperation().getLog().contains( "test-cluster" ) );
        assertTrue( operationHandler.getTrackerOperation().getLog().contains( "not exist" ) );
        assertEquals( operationHandler.getTrackerOperation().getState(), OperationState.FAILED );
    }


    @Ignore
    @Test
    public void testClusterDeletionSuccess()
    {
        AbstractOperationHandler operationHandler = MockBuilder.getUninstallOperationWithResult( true );

        operationHandler.run();

        //        assertTrue( operationHandler.getTrackerOperation().getLog().contains( "Installation info deleted" ) );
        assertEquals( operationHandler.getTrackerOperation().getState(), OperationState.SUCCEEDED );
    }


    @Test
    @Ignore
    public void testClusterDeletionFail()
    {
        AbstractOperationHandler operationHandler = MockBuilder.getUninstallOperationWithResult( false );

        operationHandler.run();

        //        assertTrue( operationHandler.getTrackerOperation().getLog().contains( "Error while deleting
        // installation" ) );
        assertEquals( operationHandler.getTrackerOperation().getState(), OperationState.FAILED );
    }
}*/
