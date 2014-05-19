package org.safehaus.kiskis.mgmt.impl.flume.handler;

import java.util.Arrays;
import java.util.HashSet;
import org.junit.*;
import org.safehaus.kiskis.mgmt.api.flume.Config;
import org.safehaus.kiskis.mgmt.impl.flume.mock.FlumeImplMock;
import org.safehaus.kiskis.mgmt.product.common.test.unit.mock.CommonMockBuilder;
import org.safehaus.kiskis.mgmt.shared.operation.AbstractOperationHandler;
import org.safehaus.kiskis.mgmt.shared.operation.ProductOperation;
import org.safehaus.kiskis.mgmt.shared.operation.ProductOperationState;

public class InstallHandlerTest {

    private FlumeImplMock mock;
    private AbstractOperationHandler handler;

    @Before
    public void setUp() {
        mock = new FlumeImplMock();
    }

    @Test(expected = NullPointerException.class)
    public void testWithNullConfig() {
        handler = new InstallHandler(mock, null);
        handler.run();
    }

    @Test
    public void testWithInvalidConfig() {
        handler = new InstallHandler(mock, new Config());
        handler.run();

        ProductOperation po = handler.getProductOperation();
        Assert.assertTrue(po.getLog().toLowerCase().contains("invalid"));
        Assert.assertEquals(po.getState(), ProductOperationState.FAILED);
    }

    @Test
    public void testWithExistingCluster() {
        Config config = new Config();
        config.setClusterName("test-cluster");
        config.setNodes(new HashSet<>(Arrays.asList(CommonMockBuilder.createAgent())));

        mock.setConfig(config);
        handler = new InstallHandler(mock, config);
        handler.run();

        ProductOperation po = handler.getProductOperation();
        Assert.assertTrue(po.getLog().toLowerCase().contains("exists"));
        Assert.assertTrue(po.getLog().toLowerCase().contains(config.getClusterName()));
        Assert.assertEquals(po.getState(), ProductOperationState.FAILED);
    }
}
