package org.safehaus.subutai.plugin.shark.impl;


import java.util.Arrays;
import java.util.HashSet;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.plugin.common.mock.CommonMockBuilder;
import org.safehaus.subutai.plugin.shark.api.SetupType;
import org.safehaus.subutai.plugin.shark.api.SharkClusterConfig;
import org.safehaus.subutai.plugin.shark.impl.handler.ActualizeMasterIpOperationHandler;
import org.safehaus.subutai.plugin.shark.impl.mock.SharkImplMock;


@Ignore
public class ActualizeMasterIpOperationHandlerTest
{

    private SharkImplMock mock;
    private AbstractOperationHandler handler;


    @Before
    public void setUp()
    {
        mock = new SharkImplMock();
        handler = new ActualizeMasterIpOperationHandler( mock, "test-cluster" );
    }


    @Test
    public void testWithoutCluster()
    {
        handler.run();

        TrackerOperation po = handler.getTrackerOperation();
        Assert.assertTrue( po.getLog().toLowerCase().contains( "not exist" ) );
        Assert.assertEquals( po.getState(), ProductOperationState.FAILED );
    }


    @Test
    public void testWithhoutSparkCluster()
    {
        SharkClusterConfig config = new SharkClusterConfig();
        config.setSetupType( SetupType.OVER_SPARK );
        config.setClusterName( "test-cluster" );
        config.setSparkClusterName( "test-cluster" );
        config.setNodes( new HashSet<>( Arrays.asList( CommonMockBuilder.createAgent() ) ) );

        mock.setClusterConfig( config );

        handler.run();

        TrackerOperation po = handler.getTrackerOperation();
        Assert.assertTrue( po.getLog().toLowerCase().contains( "spark cluster" ) );
        Assert.assertEquals( po.getState(), ProductOperationState.FAILED );
    }
}

