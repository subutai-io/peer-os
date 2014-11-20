package org.safehaus.subutai.plugin.solr.impl.handler;


/*import org.junit.Test;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.OperationState;
import org.safehaus.subutai.plugin.solr.api.SolrClusterConfig;
import org.safehaus.subutai.plugin.solr.impl.SolrImpl;
import org.safehaus.subutai.plugin.solr.impl.handler.mock.SolrImplMock;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;


public class CheckEnvironmentContainerNodeOperationHandlerTest
{


    @Test
    public void testWithoutCluster()
    {
        AbstractOperationHandler operationHandler =
                new CheckNodeOperationHandler( new SolrImplMock(), "test-cluster", "test-lxc" );

        operationHandler.run();

        assertTrue( operationHandler.getTrackerOperation().getLog().contains( "not exist" ) );
        assertEquals( operationHandler.getTrackerOperation().getState(), OperationState.FAILED );
    }


    @Test
    public void testFail()
    {
        SolrImpl solrImpl = new SolrImplMock().setClusterSolrClusterConfig( new SolrClusterConfig() );
        AbstractOperationHandler operationHandler =
                new CheckNodeOperationHandler( solrImpl, "test-cluster", "test-lxc" );

        operationHandler.run();

        assertTrue( operationHandler.getTrackerOperation().getLog().contains( "not connected" ) );
        assertEquals( operationHandler.getTrackerOperation().getState(), OperationState.FAILED );
    }
}*/
