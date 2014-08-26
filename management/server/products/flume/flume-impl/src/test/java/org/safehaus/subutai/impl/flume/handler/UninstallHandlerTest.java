package org.safehaus.subutai.impl.flume.handler;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.api.flume.Config;
import org.safehaus.subutai.impl.flume.handler.mock.FlumeImplMock;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.operation.ProductOperationState;

public class UninstallHandlerTest {

	private FlumeImplMock mock;
	private AbstractOperationHandler handler;

	@Before
	public void setUp() {
		mock = new FlumeImplMock();
		handler = new UninstallHandler(mock, "test-cluster");
	}

	@Test
	public void testWithoutCluster() {
		handler.run();

		ProductOperation po = handler.getProductOperation();
		Assert.assertTrue(po.getLog().toLowerCase().contains("not exist"));
		Assert.assertEquals(po.getState(), ProductOperationState.FAILED);
	}

	@Test
	public void testWithExistingCluster() {
		mock.setConfig(new Config());
		handler.run();

		ProductOperation po = handler.getProductOperation();
		Assert.assertTrue(po.getLog().toLowerCase().contains("uninstallation failed"));
		Assert.assertEquals(po.getState(), ProductOperationState.FAILED);
	}
}
