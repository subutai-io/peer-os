package org.safehaus.subutai.plugin.presto.impl.mock;


import org.safehaus.subutai.plugin.presto.impl.PrestoImpl;
import org.safehaus.subutai.plugin.presto.api.PrestoClusterConfig;
import org.safehaus.subutai.product.common.test.unit.mock.AgentManagerMock;
import org.safehaus.subutai.product.common.test.unit.mock.CommandRunnerMock;
import org.safehaus.subutai.product.common.test.unit.mock.DbManagerMock;
import org.safehaus.subutai.product.common.test.unit.mock.TrackerMock;

public class PrestoImplMock extends PrestoImpl {

    private PrestoClusterConfig clusterConfig;

    public PrestoImplMock() {
        super(new CommandRunnerMock(), new AgentManagerMock(), new DbManagerMock(), new TrackerMock());
    }


    public PrestoImplMock setClusterConfig(PrestoClusterConfig clusterConfig) {
        this.clusterConfig = clusterConfig;
        return this;
    }


    @Override
    public PrestoClusterConfig getCluster(String clusterName) {
        return clusterConfig;
    }
}