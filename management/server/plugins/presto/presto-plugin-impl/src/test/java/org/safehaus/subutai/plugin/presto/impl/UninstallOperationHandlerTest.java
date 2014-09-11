package org.safehaus.subutai.plugin.presto.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.plugin.presto.api.PrestoClusterConfig;
import org.safehaus.subutai.plugin.presto.api.SetupType;
import org.safehaus.subutai.plugin.presto.impl.handler.UninstallOperationHandler;
import org.safehaus.subutai.plugin.presto.impl.mock.PrestoImplMock;


public class UninstallOperationHandlerTest {

    private PrestoImplMock mock;
    private AbstractOperationHandler handler;

    @Before
    public void setUp() {
        mock = new PrestoImplMock();
        handler = new UninstallOperationHandler(mock, "test-cluster");
    }

    @Test
    public void testWithoutCluster() {
        handler.run();

        ProductOperation po = handler.getProductOperation();
        Assert.assertTrue( po.getLog().toLowerCase().contains( "not exist" ) );
        Assert.assertEquals(po.getState(), ProductOperationState.FAILED);
    }

    @Ignore
    @Test
    public void testWithExistingCluster() {
        PrestoClusterConfig config = new PrestoClusterConfig();
        config.setSetupType(SetupType.OVER_HADOOP);
        mock.setClusterConfig(config);
        handler.run();

        ProductOperation po = handler.getProductOperation();
        Assert.assertTrue(po.getLog().toLowerCase().contains("uninstallation failed"));
        Assert.assertEquals(po.getState(), ProductOperationState.FAILED);
    }
}
