package org.safehaus.subutai.plugin.spark.impl;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.OperationState;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.plugin.spark.impl.handler.StartNodeOperationHandler;


@Ignore
public class StartEnvironmentContainerNodeOperationHandlerTest
{

    @Mock
    SparkImpl mock;
    private AbstractOperationHandler handler;


    @Before
    public void setUp()
    {
        //        mock = new SparkImplMock();
        handler = new StartNodeOperationHandler( mock, "test-cluster", "test-host", true );
    }


    @Test
    public void testWithoutCluster()
    {

        handler.run();

        TrackerOperation po = handler.getTrackerOperation();
        Assert.assertTrue( po.getLog().contains( "not exist" ) );
        Assert.assertEquals( po.getState(), OperationState.FAILED );
    }


    @Test
    public void testFail()
    {
        //        mock.setClusterConfig( new SparkClusterConfig() );
        handler.run();

        TrackerOperation po = handler.getTrackerOperation();
        Assert.assertTrue( po.getLog().contains( "not connected" ) );
        Assert.assertEquals( po.getState(), OperationState.FAILED );
    }
}
