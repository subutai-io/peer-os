package org.safehaus.kiskis.mgmt.impl.hive.handler;

import org.junit.*;
import org.safehaus.kiskis.mgmt.api.hive.Config;
import org.safehaus.kiskis.mgmt.impl.hive.mock.HiveImplMock;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.operation.ProductOperationState;

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
        mock.setConfig(new Config());
        handler.run();

        ProductOperation po = handler.getProductOperation();
        Assert.assertTrue(po.getLog().toLowerCase().contains("not connected"));
        Assert.assertEquals(po.getState(), ProductOperationState.FAILED);
    }

}
