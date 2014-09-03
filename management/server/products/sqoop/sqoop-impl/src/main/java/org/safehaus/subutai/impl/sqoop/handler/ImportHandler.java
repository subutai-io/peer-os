package org.safehaus.subutai.impl.sqoop.handler;

import org.safehaus.subutai.core.commandrunner.api.AgentResult;
import org.safehaus.subutai.core.commandrunner.api.Command;
import org.safehaus.subutai.core.commandrunner.api.RequestBuilder;
import org.safehaus.subutai.api.sqoop.setting.ImportSetting;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.impl.sqoop.CommandFactory;
import org.safehaus.subutai.impl.sqoop.CommandType;
import org.safehaus.subutai.impl.sqoop.SqoopImpl;
import org.safehaus.subutai.common.protocol.Agent;

import java.util.Arrays;
import java.util.HashSet;

public class ImportHandler extends AbstractHandler {

	private ImportSetting settings;

	public ImportHandler(SqoopImpl manager, String clusterName, ProductOperation po) {
		super(manager, clusterName, po);
	}

	public ImportSetting getSettings() {
		return settings;
	}

	public void setSettings(ImportSetting settings) {
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

		String s = CommandFactory.build(CommandType.IMPORT, settings);
		Command cmd = manager.getCommandRunner().createCommand(
				new RequestBuilder(s).withTimeout(60),
				new HashSet<>(Arrays.asList(agent)));

		manager.getCommandRunner().runCommand(cmd);

		AgentResult res = cmd.getResults().get(agent.getUuid());
		if (cmd.hasSucceeded()) {
			po.addLog(res.getStdOut());
			po.addLogDone("Import completed on " + hostname);
		} else {
			po.addLog(res.getStdOut());
			po.addLogFailed(res.getStdErr());
		}
	}

}
