package org.safehaus.subutai.plugin.flume.impl.handler;

import org.junit.*;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.plugin.flume.api.FlumeConfig;
import org.safehaus.subutai.plugin.flume.api.SetupType;
import org.safehaus.subutai.plugin.flume.impl.handler.mock.FlumeImplMock;


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
        FlumeConfig config = new FlumeConfig();
        config.setSetupType(SetupType.OVER_HADOOP);
        mock.setConfig(config);
        handler.run();

        ProductOperation po = handler.getProductOperation();
        Assert.assertTrue(po.getLog().toLowerCase().contains("uninstallation failed"));
        Assert.assertEquals(po.getState(), ProductOperationState.FAILED);
    }
}
