package org.safehaus.subutai.impl.solr.handler;


import org.junit.Test;
import org.safehaus.subutai.api.solr.Config;
import org.safehaus.subutai.impl.solr.SolrImpl;
import org.safehaus.subutai.impl.solr.handler.mock.MockBuilder;
import org.safehaus.subutai.impl.solr.handler.mock.SolrImplMock;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperationState;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;


public class InstallOperationHandlerTest {

    @Test(expected = NullPointerException.class)
    public void testWithNullConfig() {
        new SolrImplMock().installCluster( null );
    }


    @Test
    public void testWithMalformedConfiguration() {
        Config config = new Config();
        config.setClusterName( "test" );
        config.setNumberOfNodes( -1 );
        AbstractOperationHandler operationHandler = new InstallOperationHandler( new SolrImplMock(), config );

        operationHandler.run();

        assertTrue( operationHandler.getProductOperation().getLog().contains( "Malformed configuration" ) );
        assertEquals( operationHandler.getProductOperation().getState(), ProductOperationState.FAILED );
    }


    @Test
    public void testWithClusterExists() {
        SolrImpl solrImpl = new SolrImplMock().setClusterConfig( new Config() );
        Config config = new Config().setClusterName( "test-cluster" );
        AbstractOperationHandler operationHandler = new InstallOperationHandler( solrImpl, config );

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
