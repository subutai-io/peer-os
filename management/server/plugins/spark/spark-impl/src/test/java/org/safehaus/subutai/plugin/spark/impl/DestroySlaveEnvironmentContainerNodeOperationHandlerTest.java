package org.safehaus.subutai.plugin.spark.impl;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.OperationState;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.plugin.spark.impl.handler.DestroySlaveNodeOperationHandler;


@Ignore
public class DestroySlaveEnvironmentContainerNodeOperationHandlerTest
{
    @Mock
    SparkImpl mock;
    private AbstractOperationHandler handler;


    @Before
    public void setUp()
    {
        //        mock = new SparkImplMock();
        handler = new DestroySlaveNodeOperationHandler( mock, "test-cluster", "test-host" );
    }


    @Test
    public void testWithoutCluster()
    {
        handler.run();

        TrackerOperation po = handler.getTrackerOperation();
        Assert.assertTrue( po.getLog().toLowerCase().contains( "not exist" ) );
        Assert.assertEquals( po.getState(), OperationState.FAILED );
    }


    @Test
    public void testWithUnconnectedAgents()
    {
        //        mock.setClusterConfig( new SparkClusterConfig() );
        handler.run();

        TrackerOperation po = handler.getTrackerOperation();
        Assert.assertTrue( po.getLog().toLowerCase().contains( "not connected" ) );
        Assert.assertEquals( po.getState(), OperationState.FAILED );
    }
}
