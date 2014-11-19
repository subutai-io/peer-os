package org.safehaus.subutai.plugin.solr.impl.handler;


/*import org.junit.Ignore;
import org.junit.Test;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.OperationState;
import org.safehaus.subutai.plugin.solr.impl.handler.mock.MockBuilder;
import org.safehaus.subutai.plugin.solr.impl.handler.mock.SolrImplMock;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;


@Ignore
public class AddEnvironmentContainerNodeOperationHandlerTest
{

    @Test
    public void testWithoutCluster()
    {
        AbstractOperationHandler operationHandler = new AddNodeOperationHandler( new SolrImplMock(), "test-cluster" );

        operationHandler.run();

        assertTrue( operationHandler.getTrackerOperation().getLog().contains( "not exist" ) );
        assertEquals( operationHandler.getTrackerOperation().getState(), OperationState.FAILED );
    }


    @Test
    public void testSuccess()
    {
        AbstractOperationHandler operationHandler = MockBuilder.getAddNodeOperationWithResult( true );

        operationHandler.run();

        assertTrue( operationHandler.getTrackerOperation().getLog().contains( "Installation succeeded" ) );
        assertEquals( operationHandler.getTrackerOperation().getState(), OperationState.SUCCEEDED );
    }


    @Test
    public void testFail()
    {
        AbstractOperationHandler operationHandler = MockBuilder.getAddNodeOperationWithResult( false );

        operationHandler.run();

        assertTrue( operationHandler.getTrackerOperation().getLog().contains( "Installation failed" ) );
        assertEquals( operationHandler.getTrackerOperation().getState(), OperationState.FAILED );
    }
}*/
