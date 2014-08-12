package org.safehaus.subutai.plugin.presto.cli;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.plugin.presto.api.Config;
import org.safehaus.subutai.plugin.presto.api.Presto;

import java.util.List;


/**
 * Displays the last log entries
 */
@Command (scope = "presto", name = "list-clusters", description = "mydescription")
public class ListClustersCommand extends OsgiCommandSupport {

	private Presto prestoManager;

	public Presto getPrestoManager() {
		return prestoManager;
	}

	public void setPrestoManager(Presto prestoManager) {
		this.prestoManager = prestoManager;
	}

	protected Object doExecute() {
		List<Config> configList = prestoManager.getClusters();
		if (!configList.isEmpty())
			for (Config config : configList) {
				System.out.println(config.getClusterName());
			}
		else System.out.println("No Presto cluster");

		return null;
	}
}
