package org.safehaus.subutai.cli.commands;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.api.sqoop.Config;
import org.safehaus.subutai.api.sqoop.Sqoop;

import java.util.List;


/**
 * Displays the last log entries
 */
@Command (scope = "sqoop", name = "list-clusters", description = "mydescription")
public class ListClustersCommand extends OsgiCommandSupport {

	private Sqoop sqoopManager;

	public Sqoop getSqoopManager() {
		return sqoopManager;
	}

	public void setSqoopManager(Sqoop sqoopManager) {
		this.sqoopManager = sqoopManager;
	}

	protected Object doExecute() {
		List<Config> configList = sqoopManager.getClusters();
		if (!configList.isEmpty())
			for (Config config : configList) {
				System.out.println(config.getClusterName());
			}
		else System.out.println("No Sqoop cluster");

		return null;
	}
}
