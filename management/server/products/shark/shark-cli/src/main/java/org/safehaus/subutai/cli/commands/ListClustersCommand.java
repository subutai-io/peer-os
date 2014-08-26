package org.safehaus.subutai.cli.commands;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.api.shark.Config;
import org.safehaus.subutai.api.shark.Shark;

import java.util.List;


/**
 * Displays the last log entries
 */
@Command (scope = "shark", name = "list-clusters", description = "mydescription")
public class ListClustersCommand extends OsgiCommandSupport {

	private Shark sharkManager;

	public Shark getSharkManager() {
		return sharkManager;
	}

	public void setSharkManager(Shark sharkManager) {
		this.sharkManager = sharkManager;
	}

	protected Object doExecute() {
		List<Config> configList = sharkManager.getClusters();
		if (!configList.isEmpty())
			for (Config config : configList) {
				System.out.println(config.getClusterName());
			}
		else System.out.println("No Shark cluster");

		return null;
	}
}
