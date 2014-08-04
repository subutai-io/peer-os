package org.safehaus.subutai.impl.networkmanager;

import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.RequestBuilder;
import org.safehaus.subutai.shared.protocol.Agent;

import java.util.HashSet;
import java.util.List;

/**
 * Created by daralbaev on 03.04.14.
 */
public class Commands {

	public static Command getCreateSSHCommand(List<Agent> agentList) {
		return NetwokManagerImpl.getCommandRunner().createCommand(
				new RequestBuilder("rm -Rf /root/.ssh && " +
						"mkdir -p /root/.ssh && " +
						"chmod 700 /root/.ssh && " +
						"ssh-keygen -t dsa -P '' -f /root/.ssh/id_dsa"),
				new HashSet<Agent>(agentList)
		);
	}

	public static Command getReadSSHCommand(List<Agent> agentList) {
		return NetwokManagerImpl.getCommandRunner().createCommand(
				new RequestBuilder("cat /root/.ssh/id_dsa.pub"),
				new HashSet<Agent>(agentList)
		);
	}

	public static Command getWriteSSHCommand(List<Agent> agentList, String key) {
		return NetwokManagerImpl.getCommandRunner().createCommand(
				new RequestBuilder(String.format("mkdir -p /root/.ssh && " +
						"chmod 700 /root/.ssh && " +
						"echo '%s' > /root/.ssh/authorized_keys && " +
						"chmod 644 /root/.ssh/authorized_keys", key)),
				new HashSet<Agent>(agentList)
		);
	}

	public static Command getConfigSSHCommand(List<Agent> agentList) {
		return NetwokManagerImpl.getCommandRunner().createCommand(
				new RequestBuilder("echo 'Host *' > /root/.ssh/config && " +
						"echo '    StrictHostKeyChecking no' >> /root/.ssh/config && " +
						"chmod 644 /root/.ssh/config"),
				new HashSet<Agent>(agentList)
		);
	}

	public static Command getReadHostsCommand(List<Agent> agentList) {
		return NetwokManagerImpl.getCommandRunner().createCommand(
				new RequestBuilder("cat /etc/hosts"),
				new HashSet<Agent>(agentList)
		);
	}

	public static Command getWriteHostsCommand(List<Agent> agentList, String hosts) {
		return NetwokManagerImpl.getCommandRunner().createCommand(
				new RequestBuilder(String.format("echo '%s' > /etc/hosts", hosts)),
				new HashSet<Agent>(agentList)
		);
	}
}
