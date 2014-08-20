package org.safehaus.subutai.cli.commands;


import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.api.accumulo.Accumulo;
import org.safehaus.subutai.api.accumulo.Config;

import java.util.List;


/**
 * Displays the last log entries
 */
@Command (scope = "accumulo", name = "list-clusters", description = "mydescription")
public class ListClustersCommand extends OsgiCommandSupport {

	private Accumulo accumuloManager;


	public Accumulo getAccumuloManager() {
		return accumuloManager;
	}


	public void setAccumuloManager(Accumulo accumuloManager) {
		this.accumuloManager = accumuloManager;
	}


	protected Object doExecute() {
		List<Config> configList = accumuloManager.getClusters();
		if (!configList.isEmpty()) {
			for (Config config : configList) {
				System.out.println(config.getClusterName());
			}
		} else {
			System.out.println("No Accumulo cluster");
		}

		return null;
	}
}
