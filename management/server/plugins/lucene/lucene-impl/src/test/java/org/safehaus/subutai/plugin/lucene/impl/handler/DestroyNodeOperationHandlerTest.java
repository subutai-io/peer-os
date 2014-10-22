package org.safehaus.subutai.plugin.lucene.impl.handler;

/* TODO Rewrite tests
import org.junit.Test;
import org.safehaus.subutai.api.lucene.Config;
import org.safehaus.subutai.impl.lucene.LuceneImpl;
import org.safehaus.subutai.impl.lucene.handler.mock.LuceneImplMock;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperationState;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;


public class DestroyNodeOperationHandlerTest {


	@Test
	public void testWithoutCluster() {
		AbstractOperationHandler operationHandler = new DestroyNodeOperationHandler(new LuceneImplMock(),
		"test-cluster",
				"lxc-host");

		operationHandler.run();

		assertTrue(operationHandler.getTrackerOperation().getLog().contains("not exist"));
		assertEquals(operationHandler.getTrackerOperation().getState(), ProductOperationState.FAILED);
	}


	@Test
	public void testWithExistingCluster() {
		LuceneImpl impl = new LuceneImplMock().setClusterConfig(new Config());
		AbstractOperationHandler operationHandler = new DestroyNodeOperationHandler(impl, "test-cluster", "lxc-host");

		operationHandler.run();

		assertTrue(operationHandler.getTrackerOperation().getLog().contains("not connected"));
		assertEquals(operationHandler.getTrackerOperation().getState(), ProductOperationState.FAILED);
	}

}
*/
