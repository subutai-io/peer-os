package org.safehaus.subutai.plugin.lucene.impl;


import org.safehaus.subutai.core.commandrunner.api.Command;
import org.safehaus.subutai.core.commandrunner.api.CommandRunner;
import org.safehaus.subutai.core.commandrunner.api.CommandsSingleton;
import org.safehaus.subutai.core.commandrunner.api.RequestBuilder;
import org.safehaus.subutai.common.enums.OutputRedirection;
import org.safehaus.subutai.common.protocol.Agent;

import java.util.Set;


public class Commands extends CommandsSingleton {

	public static final String PACKAGE_NAME = "ksks-lucene";
	public static final String INSTALL = "sleep 20; apt-get --force-yes --assume-yes install ksks-lucene";
	public static final String UNINSTALL = "apt-get --force-yes --assume-yes purge ksks-lucene";
	public static final String CHECK = "dpkg -l | grep '^ii' | grep ksks";


	public Commands(CommandRunner commandRunner) {
		init(commandRunner);
	}

	public static Command getUninstallCommand(Set<Agent> agents) {
		return createCommand(
				new RequestBuilder(UNINSTALL).withTimeout(60), agents);
	}

	public static Command getCheckInstalledCommand(Set<Agent> agents) {
		return createCommand(new RequestBuilder(CHECK), agents);
	}

	public Command getInstallCommand(Set<Agent> agents) {
		return createCommand(
				new RequestBuilder(INSTALL).withTimeout(90).withStdOutRedirection( OutputRedirection.NO), agents);
	}
}
