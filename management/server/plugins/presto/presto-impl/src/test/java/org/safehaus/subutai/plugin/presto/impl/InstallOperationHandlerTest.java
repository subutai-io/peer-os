package org.safehaus.subutai.plugin.presto.impl;


import java.util.Arrays;
import java.util.HashSet;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.plugin.common.mock.CommonMockBuilder;
import org.safehaus.subutai.plugin.presto.api.PrestoClusterConfig;
import org.safehaus.subutai.plugin.presto.api.SetupType;
import org.safehaus.subutai.plugin.presto.impl.handler.InstallOperationHandler;
import org.safehaus.subutai.plugin.presto.impl.mock.PrestoImplMock;


public class InstallOperationHandlerTest
{

    private PrestoImplMock mock;
    private AbstractOperationHandler handler;


    @Before
    public void setUp()
    {
        mock = new PrestoImplMock();
    }


    @Test( expected = NullPointerException.class )
    public void testWithNullConfig()
    {
        handler = new InstallOperationHandler( mock, null );
        handler.run();
    }


    @Test
    public void testWithInvalidConfig()
    {
        PrestoClusterConfig config = new PrestoClusterConfig();
        config.setSetupType( SetupType.OVER_HADOOP );
        config.setClusterName( "test" );
        handler = new InstallOperationHandler( mock, config );
        handler.run();

        TrackerOperation po = handler.getTrackerOperation();
        Assert.assertTrue( po.getLog().toLowerCase().contains( "malformed" ) );
        Assert.assertEquals( po.getState(), ProductOperationState.FAILED );
    }


    @Test
    public void testWithExistingCluster()
    {
        PrestoClusterConfig config = new PrestoClusterConfig();
        config.setSetupType( SetupType.OVER_HADOOP );
        config.setClusterName( "test-cluster" );
        config.setWorkers( new HashSet<>( Arrays.asList( CommonMockBuilder.createAgent() ) ) );
        config.setCoordinatorNode( CommonMockBuilder.createAgent() );

        mock.setClusterConfig( config );
        handler = new InstallOperationHandler( mock, config );
        handler.run();

        TrackerOperation po = handler.getTrackerOperation();
        Assert.assertTrue( po.getLog().toLowerCase().contains( "exists" ) );
        Assert.assertTrue( po.getLog().toLowerCase().contains( config.getClusterName() ) );
        Assert.assertEquals( po.getState(), ProductOperationState.FAILED );
    }
}

