package org.safehaus.subutai.plugin.spark.impl.mock;

import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.db.api.DbManager;
import org.safehaus.subutai.plugin.common.mock.TrackerMock;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;
import org.safehaus.subutai.plugin.spark.impl.SparkImpl;

import static org.mockito.Mockito.mock;

public class SparkImplMock extends SparkImpl{

    private SparkClusterConfig clusterConfig;

    public SparkImplMock() {
        super( mock( CommandRunner.class ), mock( AgentManager.class ), mock( DbManager.class ), new TrackerMock() );
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
