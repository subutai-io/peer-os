package org.safehaus.subutai.plugin.hive.impl.handler;

import org.safehaus.subutai.plugin.hive.impl.handler.AbstractHandler;
import org.safehaus.subutai.plugin.hive.impl.handler.StatusHandler;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.plugin.hive.api.HiveConfig;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.plugin.hive.impl.handler.mock.HiveImplMock;

public class StatusHandlerTest {

	HiveImplMock mock;
	AbstractHandler handler;

	@Before
	public void setUp() {
		mock = new HiveImplMock();
		handler = new StatusHandler(mock, "test-cluster", "test-host");
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
		mock.setConfig(new HiveConfig());
		handler.run();

		ProductOperation po = handler.getProductOperation();
		Assert.assertTrue(po.getLog().toLowerCase().contains("not connected"));
		Assert.assertEquals(po.getState(), ProductOperationState.FAILED);
	}

}
