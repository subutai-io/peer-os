package org.safehaus.subutai.cli.commands;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.api.pig.Config;
import org.safehaus.subutai.api.pig.Pig;

import java.util.List;


/**
 * Displays the last log entries
 */
@Command (scope = "pig", name = "list-clusters", description = "mydescription")
public class ListClustersCommand extends OsgiCommandSupport {

	private Pig pigManager;

	public Pig getPigManager() {
		return pigManager;
	}

	public void setPigManager(Pig pigManager) {
		this.pigManager = pigManager;
	}

	protected Object doExecute() {
		List<Config> configList = pigManager.getClusters();
		if (!configList.isEmpty())
			for (Config config : configList) {
				System.out.println(config.getClusterName());
			}
		else System.out.println("No Pig cluster");

		return null;
	}
}
