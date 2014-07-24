package org.safehaus.subutai.impl.networkmanager;

import com.google.common.base.Strings;
import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.shared.protocol.Agent;

import java.util.Arrays;
import java.util.List;

/**
 * Created by daralbaev on 04.04.14.
 */
public class SshManager {
	private List<Agent> agentList;
	private String keys;

	public SshManager(List<Agent> agentList) {
		this.agentList = agentList;
	}

	public boolean execute() {
		if (agentList != null && !agentList.isEmpty()) {
			if (create()) {
				if (read()) {
					if (write()) {
						return config();
					}
				}
			}
		}

		return false;
	}

	public boolean execute(Agent agent) {
		if (agentList != null && !agentList.isEmpty() && agent != null) {
			if (create(agent)) {
				agentList.add(agent);

				if (read()) {
					if (write()) {
						return config();
					}
				}
			}
		}

		return false;
	}

	private boolean read() {
		Command command = Commands.getReadSSHCommand(agentList);
		NetwokManagerImpl.getCommandRunner().runCommand(command);

		StringBuilder value = new StringBuilder();
		if (command.hasCompleted()) {
			for (Agent agent : agentList) {
				AgentResult result = command.getResults().get(agent.getUuid());
				if (!Strings.isNullOrEmpty(result.getStdOut())) {
					value.append(result.getStdOut());
				}
			}
		}
		keys = value.toString();

		if (!Strings.isNullOrEmpty(keys) && command.hasSucceeded()) {
			return true;
		} else {
			return false;
		}
	}

	private boolean write() {
		Command command = Commands.getWriteSSHCommand(agentList, keys);
		NetwokManagerImpl.getCommandRunner().runCommand(command);

		return command.hasSucceeded();
	}

	private boolean create(Agent agent) {
		Command command = Commands.getCreateSSHCommand(Arrays.asList(agent));
		NetwokManagerImpl.getCommandRunner().runCommand(command);

		return command.hasSucceeded();
	}

	private boolean create() {
		Command command = Commands.getCreateSSHCommand(agentList);
		NetwokManagerImpl.getCommandRunner().runCommand(command);

		return command.hasSucceeded();
	}

	private boolean config() {
		Command command = Commands.getConfigSSHCommand(agentList);
		NetwokManagerImpl.getCommandRunner().runCommand(command);

		return command.hasSucceeded();
	}
}
