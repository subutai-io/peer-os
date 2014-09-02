package org.safehaus.subutai.impl.hive.handler.mock;

import org.safehaus.subutai.api.hive.Config;
import org.safehaus.subutai.impl.hive.HiveImpl;
import org.safehaus.subutai.product.common.test.unit.mock.AgentManagerMock;
import org.safehaus.subutai.product.common.test.unit.mock.CommandRunnerMock;
import org.safehaus.subutai.product.common.test.unit.mock.DbManagerMock;
import org.safehaus.subutai.product.common.test.unit.mock.TrackerMock;
import org.safehaus.subutai.common.protocol.Agent;

import java.util.Arrays;
import java.util.UUID;

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
