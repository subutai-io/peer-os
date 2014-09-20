package org.safehaus.subutai.plugin.hive.impl.handler;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.plugin.common.mock.CommonMockBuilder;
import org.safehaus.subutai.plugin.hive.api.HiveConfig;
import org.safehaus.subutai.plugin.hive.impl.handler.mock.HiveImplMock;


public class UninstallHandlerTest
{

    private HiveImplMock mock = new HiveImplMock();
    private AbstractHandler handler;


    @Before
    public void setUp()
    {
        mock = new HiveImplMock();
        handler = new UninstallHandler( mock, "test-cluster" );
    }


    @Test
    public void testWithoutCluster()
    {
        handler.run();

        ProductOperation po = handler.getProductOperation();
        Assert.assertTrue( po.getLog().toLowerCase().contains( "not exist" ) );
        Assert.assertEquals( po.getState(), ProductOperationState.FAILED );
    }


    @Test
    public void testWithExistingCluster()
    {
        HiveConfig config = new HiveConfig();
        config.setServer( CommonMockBuilder.createAgent() );
        mock.setConfig( config );
        handler.run();

        ProductOperation po = handler.getProductOperation();
        Assert.assertTrue( po.getLog().toLowerCase().contains( "not connected" ) );
        Assert.assertTrue( po.getLog().contains( config.getServer().getHostname() ) );
        Assert.assertEquals( po.getState(), ProductOperationState.FAILED );
    }
}
