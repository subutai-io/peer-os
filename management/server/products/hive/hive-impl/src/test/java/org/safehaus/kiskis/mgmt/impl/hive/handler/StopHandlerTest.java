package org.safehaus.kiskis.mgmt.impl.hive.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.safehaus.kiskis.mgmt.api.hive.Config;
import org.safehaus.kiskis.mgmt.impl.hive.mock.HiveImplMock;
import org.safehaus.kiskis.mgmt.shared.operation.ProductOperation;
import org.safehaus.kiskis.mgmt.shared.operation.ProductOperationState;

public class StopHandlerTest {

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
        assertTrue(po.getLog().toLowerCase().contains("not exist"));
        assertEquals(po.getState(), ProductOperationState.FAILED);
    }

    @Test
    public void testFail() {
        mock.setConfig(new Config());
        handler.run();

        ProductOperation po = handler.getProductOperation();
        assertTrue(po.getLog().toLowerCase().contains("not connected"));
        assertEquals(po.getState(), ProductOperationState.FAILED);
    }

}
