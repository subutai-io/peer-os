package org.safehaus.kiskis.mgmt.impl.flume.mock;

import org.safehaus.kiskis.mgmt.api.flume.Config;
import org.safehaus.kiskis.mgmt.impl.flume.FlumeImpl;
import org.safehaus.subutai.product.common.test.unit.mock.AgentManagerMock;
import org.safehaus.subutai.product.common.test.unit.mock.CommandRunnerMock;
import org.safehaus.subutai.product.common.test.unit.mock.DbManagerMock;
import org.safehaus.subutai.product.common.test.unit.mock.TrackerMock;

public class FlumeImplMock extends FlumeImpl {

    private Config config;

    public FlumeImplMock() {
        super(new CommandRunnerMock(), new AgentManagerMock(),
                new TrackerMock(), new DbManagerMock());
    }

    @Override
    public Config getCluster(String clusterName) {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

}
