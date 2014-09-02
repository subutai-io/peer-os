package org.safehaus.subutai.plugin.solr.impl.handler;


import org.junit.Test;
import org.safehaus.subutai.plugin.solr.api.SolrClusterConfig;
import org.safehaus.subutai.plugin.solr.impl.SolrImpl;
import org.safehaus.subutai.plugin.solr.impl.handler.mock.SolrImplMock;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.ProductOperationState;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;


public class StartNodeOperationHandlerTest {


    @Test
    public void testWithoutCluster() {
        AbstractOperationHandler operationHandler =
                new StartNodeOperationHandler( new SolrImplMock(), "test-cluster", "test-lxc" );

        operationHandler.run();

        assertTrue( operationHandler.getProductOperation().getLog().contains( "not exist" ) );
        assertEquals( operationHandler.getProductOperation().getState(), ProductOperationState.FAILED );
    }


    @Test
    public void testFail() {
        SolrImpl solrImpl = new SolrImplMock().setClusterSolrClusterConfig( new SolrClusterConfig() );
        AbstractOperationHandler operationHandler =
                new StartNodeOperationHandler( solrImpl, "test-cluster", "test-lxc" );

        operationHandler.run();

        assertTrue( operationHandler.getProductOperation().getLog().contains( "not connected" ) );
        assertEquals( operationHandler.getProductOperation().getState(), ProductOperationState.FAILED );
    }
}
