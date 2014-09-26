package org.safehaus.subutai.plugin.presto.impl;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.plugin.presto.api.PrestoClusterConfig;
import org.safehaus.subutai.plugin.presto.impl.handler.StartNodeOperationHandler;
import org.safehaus.subutai.plugin.presto.impl.mock.PrestoImplMock;


public class StartNodeOperationHandlerTest
{

    private PrestoImplMock mock;
    private AbstractOperationHandler handler;


    @Before
    public void setUp()
    {
        mock = new PrestoImplMock();
        handler = new StartNodeOperationHandler( mock, "test-cluster", "test-host" );
    }


    @Test
    public void testWithoutCluster()
    {

        handler.run();

        ProductOperation po = handler.getProductOperation();
        Assert.assertTrue( po.getLog().contains( "not exist" ) );
        Assert.assertEquals( po.getState(), ProductOperationState.FAILED );
    }


    @Test
    public void testFail()
    {
        mock.setClusterConfig( new PrestoClusterConfig() );
        handler.run();

        ProductOperation po = handler.getProductOperation();
        Assert.assertTrue( po.getLog().contains( "not connected" ) );
        Assert.assertEquals( po.getState(), ProductOperationState.FAILED );
    }
}