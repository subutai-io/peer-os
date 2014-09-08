package org.safehaus.subutai.plugin.spark.impl.mock;

import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;
import org.safehaus.subutai.plugin.spark.impl.SparkImpl;
import org.safehaus.subutai.product.common.test.unit.mock.AgentManagerMock;
import org.safehaus.subutai.product.common.test.unit.mock.CommandRunnerMock;
import org.safehaus.subutai.product.common.test.unit.mock.DbManagerMock;
import org.safehaus.subutai.product.common.test.unit.mock.TrackerMock;

public class SparkImplMock extends SparkImpl {

    private SparkClusterConfig clusterConfig;

    public SparkImplMock() {
        setCommandRunner(new CommandRunnerMock());
        setAgentManager(new AgentManagerMock());
        setDbManager(new DbManagerMock());
        setTracker(new TrackerMock());
        init();
    }

    public SparkImplMock setClusterConfig(SparkClusterConfig clusterConfig) {
        this.clusterConfig = clusterConfig;
        return this;
    }

    @Override
    public SparkClusterConfig getCluster(String clusterName) {
        return clusterConfig;
    }
}
