package org.safehaus.subutai.impl.storm.handler;

import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.RequestBuilder;
import org.safehaus.subutai.api.storm.Config;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.impl.storm.CommandType;
import org.safehaus.subutai.impl.storm.Commands;
import org.safehaus.subutai.impl.storm.StormImpl;
import org.safehaus.subutai.impl.storm.StormService;
import org.safehaus.subutai.common.protocol.Agent;

import java.util.HashSet;
import java.util.Set;

public class StatusHandler extends AbstractHandler {

	private final String hostname;

	public StatusHandler(StormImpl manager, String clusterName, String hostname) {
		super(manager, clusterName);
		this.hostname = hostname;
		this.productOperation = manager.getTracker().createProductOperation(
				Config.PRODUCT_NAME,
				"Check status of " + hostname);
	}

	@Override
	public void run() {
		ProductOperation po = productOperation;
		Config config = manager.getCluster(clusterName);
		if (config == null) {
			po.addLogFailed(String.format("Cluster '%s' does not exist",
					clusterName));
			return;
		}

		Agent agent = manager.getAgentManager().getAgentByHostname(hostname);
		if (agent == null) {
			po.addLogFailed(hostname + " is not connected");
			return;
		}
		Set<Agent> set = new HashSet<>(2);
		set.add(agent);

		StormService[] services = isNimbusNode(config, hostname)
				? new StormService[] {StormService.NIMBUS, StormService.UI}
				: new StormService[] {StormService.SUPERVISOR};
		boolean result = true;
		for (StormService service : services) {
			String s = Commands.make(CommandType.STATUS, service);
			Command cmd = manager.getCommandRunner().createCommand(
					new RequestBuilder(s), set);
			manager.getCommandRunner().runCommand(cmd);
			result = result && cmd.hasSucceeded();

			po.addLog(String.format("Storm %s is %s running on %s", service,
					cmd.hasSucceeded() ? "" : "not",
					agent.getHostname()));
		}
		if (result) po.addLogDone(null);
		else po.addLogFailed(null);

	}

}
