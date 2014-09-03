/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.impl.shark;

import org.safehaus.subutai.core.command.api.Command;
import org.safehaus.subutai.core.command.api.CommandsSingleton;
import org.safehaus.subutai.core.command.api.RequestBuilder;
import org.safehaus.subutai.common.enums.OutputRedirection;
import org.safehaus.subutai.common.protocol.Agent;

import java.util.Set;

/**
 * @author dilshat
 */
public class Commands extends CommandsSingleton {

	public static Command getInstallCommand(Set<Agent> agents) {
		return createCommand(
				new RequestBuilder("apt-get --force-yes --assume-yes install ksks-shark")
						.withTimeout(90).withStdOutRedirection( OutputRedirection.NO),
				agents
		);
	}

	public static Command getUninstallCommand(Set<Agent> agents) {
		return createCommand(
				new RequestBuilder("apt-get --force-yes --assume-yes purge ksks-shark")
						.withTimeout(60),
				agents
		);
	}

	public static Command getCheckInstalledCommand(Set<Agent> agents) {
		return createCommand(
				new RequestBuilder("dpkg -l | grep '^ii' | grep ksks"),
				agents);
	}

	public static Command getSetMasterIPCommand(Set<Agent> agents, Agent masterNode) {
		return createCommand(
				new RequestBuilder(String.format(". /etc/profile && sharkConf.sh clear master ; sharkConf.sh master %s", masterNode.getHostname()))
						.withTimeout(60),
				agents
		);
	}

}
