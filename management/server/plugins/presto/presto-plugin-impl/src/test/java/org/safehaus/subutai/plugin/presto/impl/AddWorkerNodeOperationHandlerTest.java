package org.safehaus.subutai.plugin.presto.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.safehaus.subutai.plugin.presto.api.PrestoClusterConfig;
import org.safehaus.subutai.plugin.presto.impl.handler.AddWorkerNodeOperationHandler;
import org.safehaus.subutai.plugin.presto.impl.mock.PrestoImplMock;
import org.safehaus.subutai.product.common.test.unit.mock.CommonMockBuilder;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.operation.ProductOperationState;
import org.safehaus.subutai.shared.protocol.Agent;

import java.util.Arrays;
import java.util.HashSet;


public class AddWorkerNodeOperationHandlerTest {

    private PrestoImplMock mock;
    private AbstractOperationHandler handler;

    @Before
    public void setUp() {
        mock = new PrestoImplMock();
        handler = new AddWorkerNodeOperationHandler(mock, "test-cluster", "test-host");
    }

    @Test
    public void testWithoutCluster() {
        handler.run();

        ProductOperation po = handler.getProductOperation();
        Assert.assertTrue( po.getLog().toLowerCase().contains( "not exist" ) );
        Assert.assertEquals(po.getState(), ProductOperationState.FAILED);
    }

    @Test
    public void testWithUnconnectedAgents() {
        PrestoClusterConfig config = new PrestoClusterConfig();
        config.setClusterName( "test-cluster" );
        config.setWorkers( new HashSet< Agent >( Arrays.asList( CommonMockBuilder.createAgent() ) ) );
        config.setCoordinatorNode( CommonMockBuilder.createAgent() );
        mock.setClusterConfig( config );

        handler.run();

        ProductOperation po = handler.getProductOperation();
        Assert.assertTrue(po.getLog().toLowerCase().contains("not connected"));
        Assert.assertEquals(po.getState(), ProductOperationState.FAILED);
    }
}