package org.safehaus.subutai.plugin.spark.impl;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.OperationState;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.plugin.spark.impl.handler.UninstallOperationHandler;


@Ignore
public class UninstallEnvironmentContainerNodeOperationHandlerTest
{
    @Mock
    SparkImpl mock;
    private AbstractOperationHandler handler;


    @Before
    public void setUp()
    {
        //        mock = new SparkImplMock();
        handler = new UninstallOperationHandler( mock, "test-cluster" );
    }


    @Test
    public void testWithoutCluster()
    {
        handler.run();

        TrackerOperation po = handler.getTrackerOperation();
        Assert.assertTrue( po.getLog().toLowerCase().contains( "not exist" ) );
        Assert.assertEquals( po.getState(), OperationState.FAILED );
    }
}
