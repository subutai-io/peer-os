package org.safehaus.subutai.impl.hive.handler;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.api.hive.Config;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.impl.hive.handler.mock.HiveImplMock;
import org.safehaus.subutai.product.common.test.unit.mock.CommonMockBuilder;

public class UninstallHandlerTest {

	private HiveImplMock mock = new HiveImplMock();
	private AbstractHandler handler;

	@Before
	public void setUp() {
		mock = new HiveImplMock();
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
		Config config = new Config();
		config.setServer(CommonMockBuilder.createAgent());
		mock.setConfig(config);
		handler.run();

		ProductOperation po = handler.getProductOperation();
		Assert.assertTrue(po.getLog().toLowerCase().contains("not connected"));
		Assert.assertTrue(po.getLog().contains(config.getServer().getHostname()));
		Assert.assertEquals(po.getState(), ProductOperationState.FAILED);
	}

}
