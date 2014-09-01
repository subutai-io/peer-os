package org.safehaus.subutai.plugin.flume.impl.handler;

import org.junit.*;
import org.safehaus.subutai.plugin.flume.api.FlumeConfig;
import org.safehaus.subutai.plugin.flume.api.SetupType;
import org.safehaus.subutai.plugin.flume.impl.handler.mock.FlumeImplMock;
import org.safehaus.subutai.shared.operation.*;

public class AddNodeHandlerTest {

    private FlumeImplMock mock;
    private AbstractOperationHandler handler;

    @Before
    public void setUp() {
        mock = new FlumeImplMock();
        handler = new AddNodeHandler(mock, "test-cluster", "test-host");
    }

    @Test
    public void testWithoutCluster() {
        handler.run();

        ProductOperation po = handler.getProductOperation();
        Assert.assertTrue(po.getLog().toLowerCase().contains("not found"));
        Assert.assertEquals(po.getState(), ProductOperationState.FAILED);
    }

    @Test
    public void testWithExistingCluster() {
        FlumeConfig config = new FlumeConfig();
        config.setSetupType(SetupType.OVER_HADOOP);
        mock.setConfig(config);
        handler.run();

        ProductOperation po = handler.getProductOperation();
        Assert.assertTrue(po.getLog().toLowerCase().contains("not connected"));
        Assert.assertEquals(po.getState(), ProductOperationState.FAILED);
    }
}
