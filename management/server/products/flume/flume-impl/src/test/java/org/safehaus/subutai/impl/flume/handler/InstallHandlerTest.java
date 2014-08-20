package org.safehaus.subutai.impl.flume.handler;

import java.util.Arrays;
import java.util.HashSet;
import org.junit.*;
import org.safehaus.subutai.api.flume.Config;
import org.safehaus.subutai.impl.flume.handler.mock.FlumeImplMock;
import org.safehaus.subutai.product.common.test.unit.mock.CommonMockBuilder;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.operation.ProductOperationState;

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
        Config config = new Config();
        config.setClusterName( "test" );
        handler = new InstallHandler(mock, config);
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
