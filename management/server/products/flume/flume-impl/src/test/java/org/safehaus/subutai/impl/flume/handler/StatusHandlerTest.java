package org.safehaus.subutai.impl.flume.handler;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.api.flume.Config;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.impl.flume.handler.mock.FlumeImplMock;

public class StatusHandlerTest {

	private FlumeImplMock mock;
	private AbstractOperationHandler handler;

	@Before
	public void setUp() {
		mock = new FlumeImplMock();
		handler = new StatusHandler(mock, "test-cluster", "test-host");
	}

	@Test
	public void testWithoutCluster() {

		handler.run();

		ProductOperation po = handler.getProductOperation();
		Assert.assertTrue(po.getLog().contains("not exist"));
		Assert.assertEquals(po.getState(), ProductOperationState.FAILED);
	}

	@Test
	public void testFail() {
		mock.setConfig(new Config());
		handler.run();

		ProductOperation po = handler.getProductOperation();
		Assert.assertTrue(po.getLog().contains("not connected"));
		Assert.assertEquals(po.getState(), ProductOperationState.FAILED);
	}

}
