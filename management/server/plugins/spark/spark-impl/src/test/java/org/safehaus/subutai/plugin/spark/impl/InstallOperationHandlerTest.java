package org.safehaus.subutai.plugin.spark.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.plugin.common.mock.CommonMockBuilder;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;
import org.safehaus.subutai.plugin.spark.impl.handler.InstallOperationHandler;
import org.safehaus.subutai.plugin.spark.impl.mock.SparkImplMock;

import java.util.Arrays;
import java.util.HashSet;

public class InstallOperationHandlerTest {
    private SparkImplMock mock;
    private AbstractOperationHandler handler;

    @Before
    public void setUp() {
        mock = new SparkImplMock();
    }

    @Test(expected = NullPointerException.class)
    public void testWithNullConfig() {
        handler = new InstallOperationHandler(mock, null);
        handler.run();
    }

    @Test
    public void testWithInvalidConfig() {
        SparkClusterConfig config = new SparkClusterConfig();
        config.setClusterName( "test" );
        handler = new InstallOperationHandler( mock, config );
        handler.run();

        ProductOperation po = handler.getProductOperation();
        Assert.assertTrue( po.getLog().toLowerCase().contains( "malformed" ) );
        Assert.assertEquals(po.getState(), ProductOperationState.FAILED);
    }

    @Test
    public void testWithExistingCluster() {
        SparkClusterConfig config = new SparkClusterConfig();
        config.setClusterName( "test-cluster" );
        config.setSlaveNodes( new HashSet<Agent>( Arrays.asList( CommonMockBuilder.createAgent() ) ) );
        config.setMasterNode( CommonMockBuilder.createAgent() );

        mock.setClusterConfig( config );
        handler = new InstallOperationHandler(mock, config);
        handler.run();

        ProductOperation po = handler.getProductOperation();
        Assert.assertTrue(po.getLog().toLowerCase().contains("exists"));
        Assert.assertTrue(po.getLog().toLowerCase().contains(config.getClusterName()));
        Assert.assertEquals(po.getState(), ProductOperationState.FAILED);
    }
}
