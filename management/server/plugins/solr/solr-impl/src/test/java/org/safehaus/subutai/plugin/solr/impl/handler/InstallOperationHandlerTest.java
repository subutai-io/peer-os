package org.safehaus.subutai.plugin.solr.impl.handler;


import org.junit.Ignore;
import org.junit.Test;
import org.safehaus.subutai.plugin.solr.api.SolrClusterConfig;
import org.safehaus.subutai.plugin.solr.impl.SolrImpl;
import org.safehaus.subutai.plugin.solr.impl.handler.mock.MockBuilder;
import org.safehaus.subutai.plugin.solr.impl.handler.mock.SolrImplMock;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperationState;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;


@Ignore
public class InstallOperationHandlerTest {

    @Test(expected = NullPointerException.class)
    public void testWithNullConfig() {
        new SolrImplMock().installCluster( null );
    }


    @Test
    public void testWithMalformedConfiguration() {
        SolrClusterConfig solrClusterConfig = new SolrClusterConfig();
        solrClusterConfig.setClusterName( "test" );
        solrClusterConfig.setNumberOfNodes( -1 );
        AbstractOperationHandler operationHandler = new InstallOperationHandler( new SolrImplMock(), solrClusterConfig );

        operationHandler.run();

        assertTrue( operationHandler.getProductOperation().getLog().contains( "Malformed configuration" ) );
        assertEquals( operationHandler.getProductOperation().getState(), ProductOperationState.FAILED );
    }


    @Test
    public void testWithClusterExists() {
        SolrImpl solrImpl = new SolrImplMock().setClusterSolrClusterConfig( new SolrClusterConfig() );
        SolrClusterConfig solrClusterConfig = new SolrClusterConfig().setClusterName( "test-cluster" );
        AbstractOperationHandler operationHandler = new InstallOperationHandler( solrImpl, solrClusterConfig );

        operationHandler.run();

        assertTrue( operationHandler.getProductOperation().getLog().contains( "test-cluster" ) );
        assertTrue( operationHandler.getProductOperation().getLog().contains( "already exists" ) );
        assertEquals( operationHandler.getProductOperation().getState(), ProductOperationState.FAILED );
    }


    @Test
    public void testSuccess() {
        AbstractOperationHandler operationHandler = MockBuilder.getInstallOperationWithResult( true );

        operationHandler.run();

        assertTrue( operationHandler.getProductOperation().getLog().contains( "Installation succeeded" ) );
        assertEquals( operationHandler.getProductOperation().getState(), ProductOperationState.SUCCEEDED );
    }


    @Test
    public void testFail() {
        AbstractOperationHandler operationHandler = MockBuilder.getInstallOperationWithResult( false );

        operationHandler.run();

        assertTrue( operationHandler.getProductOperation().getLog().contains( "Installation failed" ) );
        assertEquals( operationHandler.getProductOperation().getState(), ProductOperationState.FAILED );
    }
}
