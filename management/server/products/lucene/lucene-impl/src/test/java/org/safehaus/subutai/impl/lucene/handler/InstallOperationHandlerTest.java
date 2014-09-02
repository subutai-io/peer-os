package org.safehaus.subutai.impl.lucene.handler;


import org.junit.Ignore;
import org.junit.Test;
import org.safehaus.subutai.api.lucene.Config;
import org.safehaus.subutai.impl.lucene.LuceneImpl;
import org.safehaus.subutai.impl.lucene.handler.mock.LuceneImplMock;
import org.safehaus.subutai.product.common.test.unit.mock.CommonMockBuilder;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.ProductOperationState;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;


public class InstallOperationHandlerTest {

    @Test( expected = NullPointerException.class )
    public void testWithNullConfig() {
        new LuceneImplMock().installCluster( null );
    }


    @Test
    public void testWithMalformedConfiguration() {
        Config      config = new Config();
        config.setClusterName( "test" );
        AbstractOperationHandler operationHandler = new InstallOperationHandler( new LuceneImplMock(), config );

        operationHandler.run();

        assertTrue( operationHandler.getProductOperation().getLog().contains( "Malformed configuration" ) );
        assertEquals( operationHandler.getProductOperation().getState(), ProductOperationState.FAILED );
    }


    @Test
    @Ignore
    public void testWithExistingCluster() {
        Config config = new Config();
        config.setClusterName( "test-cluster" );
        config.getNodes().add( CommonMockBuilder.createAgent() );

        LuceneImpl impl = new LuceneImplMock().setClusterConfig( new Config() );
        AbstractOperationHandler operationHandler = new InstallOperationHandler( impl, config );

        operationHandler.run();

        assertTrue( operationHandler.getProductOperation().getLog().contains( "test-cluster" ) );
        assertTrue( operationHandler.getProductOperation().getLog().contains( "already exists" ) );
        assertEquals( operationHandler.getProductOperation().getState(), ProductOperationState.FAILED );
    }
}
