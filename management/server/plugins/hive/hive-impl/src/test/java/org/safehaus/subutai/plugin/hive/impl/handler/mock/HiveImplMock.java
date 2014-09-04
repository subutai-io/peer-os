package org.safehaus.subutai.plugin.hive.impl.handler.mock;

import org.safehaus.subutai.plugin.hive.api.HiveConfig;
import org.safehaus.subutai.plugin.hive.impl.HiveImpl;
import org.safehaus.subutai.product.common.test.unit.mock.AgentManagerMock;
import org.safehaus.subutai.product.common.test.unit.mock.CommandRunnerMock;
import org.safehaus.subutai.product.common.test.unit.mock.DbManagerMock;
import org.safehaus.subutai.product.common.test.unit.mock.TrackerMock;
import org.safehaus.subutai.common.protocol.Agent;

import java.util.Arrays;
import java.util.UUID;

public class HiveImplMock extends HiveImpl {

	private HiveConfig config;

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
	public HiveConfig getCluster(String clusterName) {
		return config;
	}

	public void setConfig(HiveConfig config) {
		this.config = config;
	}

}
