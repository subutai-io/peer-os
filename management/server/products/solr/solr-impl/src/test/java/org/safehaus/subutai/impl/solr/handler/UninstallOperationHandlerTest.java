package org.safehaus.subutai.impl.solr.handler;


import org.junit.Ignore;
import org.junit.Test;
import org.safehaus.subutai.impl.solr.handler.mock.MockBuilder;
import org.safehaus.subutai.impl.solr.handler.mock.SolrImplMock;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperationState;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;


public class UninstallOperationHandlerTest {

	@Test
	public void testWithoutCluster() {
		AbstractOperationHandler operationHandler = new UninstallOperationHandler(new SolrImplMock(), "test-cluster");

		operationHandler.run();

		assertTrue(operationHandler.getProductOperation().getLog().contains("test-cluster"));
		assertTrue(operationHandler.getProductOperation().getLog().contains("not exist"));
		assertEquals(operationHandler.getProductOperation().getState(), ProductOperationState.FAILED);
	}


	@Test
	public void testClusterDeletionSuccess() {
		AbstractOperationHandler operationHandler = MockBuilder.getUninstallOperationWithResult(true);

		operationHandler.run();

//        assertTrue( operationHandler.getProductOperation().getLog().contains( "Installation info deleted" ) );
		assertEquals(operationHandler.getProductOperation().getState(), ProductOperationState.SUCCEEDED);
	}


	@Test
	@Ignore
	public void testClusterDeletionFail() {
		AbstractOperationHandler operationHandler = MockBuilder.getUninstallOperationWithResult(false);

		operationHandler.run();

//        assertTrue( operationHandler.getProductOperation().getLog().contains( "Error while deleting installation" ) );
		assertEquals(operationHandler.getProductOperation().getState(), ProductOperationState.FAILED);
	}
}
