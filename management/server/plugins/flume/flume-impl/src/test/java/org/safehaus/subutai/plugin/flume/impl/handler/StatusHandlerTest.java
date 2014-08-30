package org.safehaus.subutai.plugin.flume.impl.handler;

import org.safehaus.subutai.plugin.flume.impl.handler.StatusHandler;
import org.junit.*;
import org.safehaus.subutai.plugin.flume.api.FlumeConfig;
import org.safehaus.subutai.plugin.flume.impl.handler.mock.FlumeImplMock;
import org.safehaus.subutai.shared.operation.*;

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
        mock.setConfig(new FlumeConfig());
        handler.run();

        ProductOperation po = handler.getProductOperation();
        Assert.assertTrue(po.getLog().contains("not connected"));
        Assert.assertEquals(po.getState(), ProductOperationState.FAILED);
    }

}
