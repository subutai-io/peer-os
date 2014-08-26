package org.safehaus.subutai.impl.pig.handler;


import org.junit.Test;
import org.safehaus.subutai.api.pig.Config;
import org.safehaus.subutai.impl.pig.PigImpl;
import org.safehaus.subutai.impl.pig.handler.mock.PigImplMock;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperationState;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;


public class DestroyNodeOperationHandlerTest {


	@Test
	public void testWithoutCluster() {
		AbstractOperationHandler operationHandler = new DestroyNodeOperationHandler(new PigImplMock(), "test-cluster",
				"lxc-host");

		operationHandler.run();

		assertTrue(operationHandler.getProductOperation().getLog().contains("not exist"));
		assertEquals(operationHandler.getProductOperation().getState(), ProductOperationState.FAILED);
	}


	@Test
	public void testWithExistingCluster() {
		PigImpl pigImpl = new PigImplMock().setClusterConfig(new Config());
		AbstractOperationHandler operationHandler = new DestroyNodeOperationHandler(pigImpl, "test-cluster", "lxc-host");

		operationHandler.run();

		assertTrue(operationHandler.getProductOperation().getLog().contains("not connected"));
		assertEquals(operationHandler.getProductOperation().getState(), ProductOperationState.FAILED);
	}

}
