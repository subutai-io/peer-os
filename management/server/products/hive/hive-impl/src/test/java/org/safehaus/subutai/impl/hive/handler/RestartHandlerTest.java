package org.safehaus.subutai.impl.hive.handler;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.api.hive.Config;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.impl.hive.handler.mock.HiveImplMock;

public class RestartHandlerTest {

	HiveImplMock mock;
	AbstractHandler handler;

	@Before
	public void setUp() {
		mock = new HiveImplMock();
		handler = new RestartHandler(mock, "test-cluster", "test-host");
	}

	@Test
	public void testWithoutCluster() {
		handler.run();

		ProductOperation po = handler.getProductOperation();
		Assert.assertTrue(po.getLog().toLowerCase().contains("not exist"));
		Assert.assertEquals(po.getState(), ProductOperationState.FAILED);
	}

	@Test
	public void testFail() {
		mock.setConfig(new Config());
		handler.run();

		ProductOperation po = handler.getProductOperation();
		Assert.assertTrue(po.getLog().toLowerCase().contains("not connected"));
		Assert.assertEquals(po.getState(), ProductOperationState.FAILED);
	}

}
