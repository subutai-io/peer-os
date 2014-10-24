package org.safehaus.subutai.plugin.spark.impl;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.OperationState;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.plugin.spark.impl.handler.UninstallOperationHandler;
import org.safehaus.subutai.plugin.spark.impl.mock.SparkImplMock;


@Ignore
public class UninstallEnvironmentContainerNodeOperationHandlerTest
{
    private SparkImplMock mock;
    private AbstractOperationHandler handler;


    @Before
    public void setUp()
    {
        mock = new SparkImplMock();
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
