package org.safehaus.subutai.impl.hive.handler;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.api.hive.Config;
import org.safehaus.subutai.impl.hive.handler.mock.HiveImplMock;
import org.safehaus.subutai.shared.protocol.Agent;

import java.util.HashSet;

public class AbstractHandlerTest {

	private final AbstractHandler impl;
	private final String serverHostname = "server-host";
	private Config config;

	public AbstractHandlerTest() {
		impl = new AbstractHandlerMock(new HiveImplMock(), "test-cluster");
	}

	@Before
	public void setUp() {
		config = new Config();
		config.setClusterName("hive-cluster");

		Agent a = HiveImplMock.createAgent(serverHostname);
		config.setServer(a);
		config.setClients(new HashSet<Agent>(4));
		for (int i = 0; i < config.getClients().size(); i++) {
			a = HiveImplMock.createAgent("hostname" + i);
			config.getClients().add(a);
		}
	}

	@Test
	public void testIsServerNode() {
		Assert.assertTrue(impl.isServerNode(config, serverHostname));
		Assert.assertFalse(impl.isServerNode(config, "other-name"));
	}

}
