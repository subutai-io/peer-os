package org.safehaus.subutai.plugin.spark.impl;


import java.util.Arrays;
import java.util.HashSet;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.OperationState;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.plugin.common.mock.CommonMockBuilder;
import org.safehaus.subutai.plugin.spark.api.SetupType;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;
import org.safehaus.subutai.plugin.spark.impl.handler.InstallOperationHandler;
import org.safehaus.subutai.plugin.spark.impl.mock.SparkImplMock;


@Ignore
public class InstallOperationHandlerTest
{
    private SparkImplMock mock;
    private AbstractOperationHandler handler;


    @Before
    public void setUp()
    {
        mock = new SparkImplMock();
    }


    @Test(expected = NullPointerException.class)
    public void testWithNullConfig()
    {
        handler = new InstallOperationHandler( mock, null );
        handler.run();
    }


    @Test
    public void testWithInvalidConfig()
    {
        SparkClusterConfig config = new SparkClusterConfig();
        config.setSetupType( SetupType.OVER_HADOOP );
        config.setClusterName( "test" );
        handler = new InstallOperationHandler( mock, config );
        handler.run();

        TrackerOperation po = handler.getTrackerOperation();
        Assert.assertTrue( po.getLog().toLowerCase().contains( "malformed" ) );
        Assert.assertEquals( po.getState(), OperationState.FAILED );
    }


    @Test
    public void testWithExistingCluster()
    {
        SparkClusterConfig config = new SparkClusterConfig();
        config.setSetupType( SetupType.OVER_HADOOP );
        config.setClusterName( "test-cluster" );
        config.setSlaveNodes( new HashSet<>( Arrays.asList( CommonMockBuilder.createAgent() ) ) );
        config.setMasterNode( CommonMockBuilder.createAgent() );

        mock.setClusterConfig( config );
        handler = new InstallOperationHandler( mock, config );
        handler.run();

        TrackerOperation po = handler.getTrackerOperation();
        Assert.assertTrue( po.getLog().toLowerCase().contains( "exists" ) );
        Assert.assertTrue( po.getLog().toLowerCase().contains( config.getClusterName() ) );
        Assert.assertEquals( po.getState(), OperationState.FAILED );
    }
}
