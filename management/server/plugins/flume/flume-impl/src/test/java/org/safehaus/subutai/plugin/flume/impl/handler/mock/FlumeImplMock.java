package org.safehaus.subutai.plugin.flume.impl.handler.mock;

import org.safehaus.subutai.plugin.flume.api.FlumeConfig;
import org.safehaus.subutai.plugin.flume.impl.FlumeImpl;
import org.safehaus.subutai.product.common.test.unit.mock.*;

public class FlumeImplMock extends FlumeImpl {

    private FlumeConfig config;

    public FlumeImplMock() {
        setCommandRunner(new CommandRunnerMock());
        setAgentManager(new AgentManagerMock());
        setTracker(new TrackerMock());
        setDbManager(new DbManagerMock());
    }

    @Override
    public FlumeConfig getCluster(String clusterName) {
        return config;
    }

    public void setConfig(FlumeConfig config) {
        this.config = config;
    }

}
