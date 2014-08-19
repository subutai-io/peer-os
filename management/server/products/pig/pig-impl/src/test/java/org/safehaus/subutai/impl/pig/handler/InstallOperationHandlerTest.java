package org.safehaus.subutai.impl.pig.handler;


import org.junit.Test;
import org.safehaus.subutai.api.pig.Config;
import org.safehaus.subutai.impl.pig.PigImpl;
import org.safehaus.subutai.impl.pig.handler.mock.PigImplMock;
import org.safehaus.subutai.product.common.test.unit.mock.CommonMockBuilder;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperationState;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;


public class InstallOperationHandlerTest {

	@Test (expected = NullPointerException.class)
	public void testWithNullConfig() {
		new PigImplMock().installCluster(null);
	}


	@Test
	public void testWithMalformedConfiguration() {
		AbstractOperationHandler operationHandler = new InstallOperationHandler(new PigImplMock(), new Config());

		operationHandler.run();

		assertTrue(operationHandler.getProductOperation().getLog().contains("Malformed configuration"));
		assertEquals(operationHandler.getProductOperation().getState(), ProductOperationState.FAILED);
	}


	@Test
	public void testWithExistingCluster() {
		Config config = new Config().setClusterName("test-cluster");
		config.getNodes().add(CommonMockBuilder.createAgent());

		PigImpl pigImpl = new PigImplMock().setClusterConfig(new Config());
		AbstractOperationHandler operationHandler = new InstallOperationHandler(pigImpl, config);

		operationHandler.run();

		assertTrue(operationHandler.getProductOperation().getLog().contains("test-cluster"));
		assertTrue(operationHandler.getProductOperation().getLog().contains("already exists"));
		assertEquals(operationHandler.getProductOperation().getState(), ProductOperationState.FAILED);
	}

}
