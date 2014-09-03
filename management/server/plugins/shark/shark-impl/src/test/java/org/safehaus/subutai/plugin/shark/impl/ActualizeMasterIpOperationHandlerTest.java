package org.safehaus.subutai.plugin.shark.impl;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.plugin.shark.api.SharkClusterConfig;
import org.safehaus.subutai.plugin.shark.impl.handler.ActualizeMasterIpOperationHandler;
import org.safehaus.subutai.plugin.shark.impl.mock.SharkImplMock;
import org.safehaus.subutai.product.common.test.unit.mock.CommonMockBuilder;

import java.util.Arrays;
import java.util.HashSet;

public class ActualizeMasterIpOperationHandlerTest {

    private SharkImplMock mock;
    private AbstractOperationHandler handler;

    @Before
    public void setUp() {
        mock = new SharkImplMock();
        handler = new ActualizeMasterIpOperationHandler( mock, "test-cluster" );
    }

    @Test
    public void testWithoutCluster() {
        handler.run();

        ProductOperation po = handler.getProductOperation();
        Assert.assertTrue( po.getLog().toLowerCase().contains( "not exist" ) );
        Assert.assertEquals(po.getState(), ProductOperationState.FAILED);
    }

    @Test
    public void testWithhoutSparkCluster() {
        SharkClusterConfig config = new SharkClusterConfig();
        config.setClusterName( "test-cluster" );
        config.setNodes( new HashSet<Agent>( Arrays.asList( CommonMockBuilder.createAgent() ) )  );
        mock.setClusterConfig( config );

        handler.run();

        ProductOperation po = handler.getProductOperation();
        Assert.assertTrue(po.getLog().toLowerCase().contains("spark cluster"));
        Assert.assertEquals(po.getState(), ProductOperationState.FAILED);
    }
}
