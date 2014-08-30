package org.safehaus.subutai.plugin.flume.impl.handler;

import org.safehaus.subutai.plugin.flume.impl.handler.DestroyNodeHandler;
import org.junit.*;
import org.safehaus.subutai.plugin.flume.api.FlumeConfig;
import org.safehaus.subutai.plugin.flume.impl.handler.mock.FlumeImplMock;
import org.safehaus.subutai.shared.operation.*;

public class DestroyNodeHandlerTest {

    private FlumeImplMock mock;
    private AbstractOperationHandler handler;

    @Before
    public void setUp() {
        mock = new FlumeImplMock();
        handler = new DestroyNodeHandler(mock, "test-cluster", "test-host");
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
        mock.setConfig(new FlumeConfig());
        handler.run();

        ProductOperation po = handler.getProductOperation();
        Assert.assertTrue(po.getLog().toLowerCase().contains("not connected"));
        Assert.assertEquals(po.getState(), ProductOperationState.FAILED);
    }
}
