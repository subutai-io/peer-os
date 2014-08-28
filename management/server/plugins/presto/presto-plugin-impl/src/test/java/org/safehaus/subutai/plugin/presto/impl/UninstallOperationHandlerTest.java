package org.safehaus.subutai.plugin.presto.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.plugin.presto.api.PrestoClusterConfig;
import org.safehaus.subutai.plugin.presto.impl.handler.UninstallOperationHandler;
import org.safehaus.subutai.plugin.presto.impl.mock.PrestoImplMock;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.operation.ProductOperationState;


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

    @Test
    public void testWithExistingCluster() {
        mock.setClusterConfig( new PrestoClusterConfig() );
        handler.run();

        ProductOperation po = handler.getProductOperation();
        Assert.assertTrue(po.getLog().toLowerCase().contains("uninstallation failed"));
        Assert.assertEquals(po.getState(), ProductOperationState.FAILED);
    }
}
