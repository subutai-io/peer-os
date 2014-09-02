package org.safehaus.subutai.impl.lucene.handler;


import org.junit.Test;
import org.safehaus.subutai.api.lucene.Config;
import org.safehaus.subutai.impl.lucene.LuceneImpl;
import org.safehaus.subutai.impl.lucene.handler.mock.LuceneImplMock;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.ProductOperationState;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;


public class DestroyNodeOperationHandlerTest {


	@Test
	public void testWithoutCluster() {
		AbstractOperationHandler operationHandler = new DestroyNodeOperationHandler(new LuceneImplMock(), "test-cluster",
				"lxc-host");

		operationHandler.run();

		assertTrue(operationHandler.getProductOperation().getLog().contains("not exist"));
		assertEquals(operationHandler.getProductOperation().getState(), ProductOperationState.FAILED);
	}


	@Test
	public void testWithExistingCluster() {
		LuceneImpl impl = new LuceneImplMock().setClusterConfig(new Config());
		AbstractOperationHandler operationHandler = new DestroyNodeOperationHandler(impl, "test-cluster", "lxc-host");

		operationHandler.run();

		assertTrue(operationHandler.getProductOperation().getLog().contains("not connected"));
		assertEquals(operationHandler.getProductOperation().getState(), ProductOperationState.FAILED);
	}

}
