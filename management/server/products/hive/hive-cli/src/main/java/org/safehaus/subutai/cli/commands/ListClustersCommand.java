package org.safehaus.subutai.cli.commands;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.api.hive.Config;
import org.safehaus.subutai.api.hive.Hive;

import java.util.List;


/**
 * Displays the last log entries
 */
@Command (scope = "hive", name = "list-clusters", description = "mydescription")
public class ListClustersCommand extends OsgiCommandSupport {


	private Hive hiveManager;

	public Hive getHiveManager() {
		return hiveManager;
	}

	public void setHiveManager(Hive hiveManager) {
		this.hiveManager = hiveManager;
	}

	protected Object doExecute() {
		List<Config> configList = hiveManager.getClusters();
		if (!configList.isEmpty())
			for (Config config : configList) {
				System.out.println(config.getClusterName());
			}
		else System.out.println("No Hive cluster");

		return null;
	}

}
