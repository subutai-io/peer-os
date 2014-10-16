package org.safehaus.subutai.plugin.hipi.impl.handler;

/* TODO Rewrite tests
import org.junit.Test;
import org.safehaus.subutai.api.lucene.Config;
import org.safehaus.subutai.impl.lucene.LuceneImpl;
import org.safehaus.subutai.impl.lucene.handler.mock.LuceneImplMock;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperationState;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;


public class AddNodeOperationHandlerTest {


	@Test
	public void testWithoutCluster() {
		AbstractOperationHandler operationHandler = new AddNodeOperationHandler(new LuceneImplMock(), "test-cluster",
				"lxc-host");

		operationHandler.run();

		assertTrue(operationHandler.getProductOperation().getLog().contains("not exist"));
		assertEquals(operationHandler.getProductOperation().getState(), ProductOperationState.FAILED);
	}


	@Test
	public void testWithExistingCluster() {
		LuceneImpl impl = new LuceneImplMock().setClusterConfig(new Config());
		AbstractOperationHandler operationHandler = new AddNodeOperationHandler(impl, "test-cluster", "lxc-host");

		operationHandler.run();

		assertTrue(operationHandler.getProductOperation().getLog().contains("not connected"));
		assertEquals(operationHandler.getProductOperation().getState(), ProductOperationState.FAILED);
	}

}
*/