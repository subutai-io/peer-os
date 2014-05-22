package org.safehaus.kiskis.mgmt.impl.hive.handler;

import org.junit.*;
import org.safehaus.kiskis.mgmt.api.hive.Config;
import org.safehaus.kiskis.mgmt.impl.hive.mock.HiveImplMock;
import org.safehaus.kiskis.mgmt.product.common.test.unit.mock.CommonMockBuilder;
import org.safehaus.kiskis.mgmt.shared.operation.ProductOperation;
import org.safehaus.kiskis.mgmt.shared.operation.ProductOperationState;

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
