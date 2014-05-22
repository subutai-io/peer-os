package org.safehaus.kiskis.mgmt.impl.hive.mock;

import java.util.Arrays;
import java.util.UUID;
import org.safehaus.kiskis.mgmt.api.hive.Config;
import org.safehaus.kiskis.mgmt.impl.hive.HiveImpl;
import org.safehaus.kiskis.mgmt.product.common.test.unit.mock.*;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

public class HiveImplMock extends HiveImpl {

    private Config config;

    public HiveImplMock() {
        setAgentManager(new AgentManagerMock());
        setCommandRunner(new CommandRunnerMock());
        setDbManager(new DbManagerMock());
        setTracker(new TrackerMock());
    }

    public static Agent createAgent(String hostname) {
        return new Agent(UUID.randomUUID(), hostname, "parent-host",
                "00:00:00:00", Arrays.asList("127.0.0.1", "127.0.0.1"),
                true, "transportId");
    }

    @Override
    public Config getCluster(String clusterName) {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

}
