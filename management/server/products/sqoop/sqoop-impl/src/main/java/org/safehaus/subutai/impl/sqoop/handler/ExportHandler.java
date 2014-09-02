package org.safehaus.subutai.impl.sqoop.handler;

import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.RequestBuilder;
import org.safehaus.subutai.api.sqoop.setting.ExportSetting;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.impl.sqoop.CommandFactory;
import org.safehaus.subutai.impl.sqoop.CommandType;
import org.safehaus.subutai.impl.sqoop.SqoopImpl;
import org.safehaus.subutai.common.protocol.Agent;

import java.util.Arrays;
import java.util.HashSet;

public class ExportHandler extends AbstractHandler {

	private ExportSetting settings;

	public ExportHandler(SqoopImpl manager, String clusterName, ProductOperation po) {
		super(manager, clusterName, po);
	}

	public ExportSetting getSettings() {
		return settings;
	}

	public void setSettings(ExportSetting settings) {
		this.settings = settings;
		this.hostname = settings.getHostname();
	}

	@Override
	public void run() {
		Agent agent = manager.getAgentManager().getAgentByHostname(hostname);
		if (agent == null) {
			po.addLogFailed("Node is not connected");
			return;
		}

		String s = CommandFactory.build(CommandType.EXPORT, settings);
		Command cmd = manager.getCommandRunner().createCommand(
				new RequestBuilder(s).withTimeout(60),
				new HashSet<>(Arrays.asList(agent)));

		manager.getCommandRunner().runCommand(cmd);

		AgentResult res = cmd.getResults().get(agent.getUuid());
		if (cmd.hasSucceeded()) {
			po.addLog(res.getStdOut());
			po.addLogDone("Export completed on " + hostname);
		} else {
			po.addLog(res.getStdOut());
			po.addLogFailed(res.getStdErr());
		}
	}

}
