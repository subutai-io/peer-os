package org.safehaus.subutai.impl.solr.handler;


import org.junit.Test;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.impl.solr.handler.mock.MockBuilder;
import org.safehaus.subutai.impl.solr.handler.mock.SolrImplMock;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;


public class AddNodeOperationHandlerTest {

	@Test
	public void testWithoutCluster() {
		AbstractOperationHandler operationHandler = new AddNodeOperationHandler(new SolrImplMock(), "test-cluster");

		operationHandler.run();

		assertTrue(operationHandler.getProductOperation().getLog().contains("not exist"));
		assertEquals(operationHandler.getProductOperation().getState(), ProductOperationState.FAILED);
	}


	@Test
	public void testSuccess() {
		AbstractOperationHandler operationHandler = MockBuilder.getAddNodeOperationWithResult(true);

		operationHandler.run();

		assertTrue(operationHandler.getProductOperation().getLog().contains("Installation succeeded"));
		assertEquals(operationHandler.getProductOperation().getState(), ProductOperationState.SUCCEEDED);
	}


	@Test
	public void testFail() {
		AbstractOperationHandler operationHandler = MockBuilder.getAddNodeOperationWithResult(false);

		operationHandler.run();

		assertTrue(operationHandler.getProductOperation().getLog().contains("Installation failed"));
		assertEquals(operationHandler.getProductOperation().getState(), ProductOperationState.FAILED);
	}

}
