package org.safehaus.subutai.plugin.presto.impl.mock;

import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.db.api.DbManager;
import org.safehaus.subutai.plugin.presto.api.PrestoClusterConfig;
import org.safehaus.subutai.plugin.presto.impl.PrestoImpl;
import org.safehaus.subutai.product.common.test.unit.mock.AgentManagerMock;
import org.safehaus.subutai.product.common.test.unit.mock.CommandRunnerMock;
import org.safehaus.subutai.product.common.test.unit.mock.DbManagerMock;
import org.safehaus.subutai.product.common.test.unit.mock.TrackerMock;

import static org.mockito.Mockito.mock;

public class PrestoImplMock extends PrestoImpl {

    private PrestoClusterConfig clusterConfig;

    public PrestoImplMock() {
        setCommandRunner( mock( CommandRunner.class ) );
        setAgentManager( mock( AgentManager.class ) );
        setDbManager( mock( DbManager.class ) );
        setTracker( new TrackerMock() );
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
