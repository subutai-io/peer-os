package org.safehaus.subutai.plugin.shark.impl.mock;

import org.safehaus.subutai.plugin.shark.api.SharkClusterConfig;
import org.safehaus.subutai.plugin.shark.impl.SharkImpl;
import org.safehaus.subutai.plugin.spark.api.Spark;
import org.safehaus.subutai.product.common.test.unit.mock.AgentManagerMock;
import org.safehaus.subutai.product.common.test.unit.mock.CommandRunnerMock;
import org.safehaus.subutai.product.common.test.unit.mock.DbManagerMock;
import org.safehaus.subutai.product.common.test.unit.mock.TrackerMock;

import static org.mockito.Mockito.mock;

public class SharkImplMock extends SharkImpl{
    private SharkClusterConfig clusterConfig;

    public SharkImplMock() {
        super(new CommandRunnerMock(), new AgentManagerMock(), new DbManagerMock(), new TrackerMock(), mock( Spark.class ) );
    }


    public SharkImplMock setClusterConfig(SharkClusterConfig clusterConfig) {
        this.clusterConfig = clusterConfig;
        return this;
    }


    @Override
    public SharkClusterConfig getCluster(String clusterName) {
        return clusterConfig;
    }
}
