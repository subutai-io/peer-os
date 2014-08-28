package org.safehaus.subutai.plugin.presto.impl;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.plugin.presto.api.PrestoClusterConfig;
import org.safehaus.subutai.plugin.presto.impl.handler.StopNodeOperationHandler;
import org.safehaus.subutai.plugin.presto.impl.mock.PrestoImplMock;
import org.safehaus.subutai.product.common.test.unit.mock.CommonMockBuilder;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.operation.ProductOperationState;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.settings.Common;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class StopNodeOperationHandlerTest {

    private PrestoImplMock mock;
    private AbstractOperationHandler handler;

    @Before
    public void setUp() {
        mock = new PrestoImplMock();
        handler = new StopNodeOperationHandler(mock, "test-cluster", "test-host");
    }

    @Test
    public void testWithoutCluster() {

        handler.run();

        ProductOperation po = handler.getProductOperation();
        Assert.assertTrue( po.getLog().contains( "not exist" ) );
        Assert.assertEquals(po.getState(), ProductOperationState.FAILED);
    }

    @Test
    public void testWithNotConnectedAgents() {
        PrestoClusterConfig config = new PrestoClusterConfig();
        mock.setClusterConfig( config );
        handler.run();

        ProductOperation po = handler.getProductOperation();
        System.out.println( po.getLog() );
        Assert.assertTrue(po.getLog().contains("not connected"));
        Assert.assertEquals(po.getState(), ProductOperationState.FAILED);
    }

    @Test
    public void testNodeAlreadyBelongsToCluster(){
        PrestoClusterConfig config = new PrestoClusterConfig();
        final Agent tmp1 = CommonMockBuilder.createAgent();
        tmp1.setHostname( "test-host" );
        final Agent tmp2 = CommonMockBuilder.createAgent();
        tmp1.setHostname( "test-host2" );

        Set<Agent> workers = new HashSet<Agent>() {{
            add(tmp1);
            add(tmp2);
        }};

        config.setWorkers( workers );
        config.setCoordinatorNode( tmp2 );

        mock.setClusterConfig( config );
        handler.run();

        ProductOperation po = handler.getProductOperation();
        System.out.println( po.getLog() );
        Assert.assertTrue(po.getLog().contains("belong"));
        Assert.assertEquals(po.getState(), ProductOperationState.FAILED);
    }
}
