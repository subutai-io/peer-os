package org.safehaus.subutai.plugin.hive.cli;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.plugin.hive.api.HiveConfig;
import org.safehaus.subutai.plugin.hive.api.Hive;

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
		List<HiveConfig> configList = hiveManager.getClusters();
		if (!configList.isEmpty())
			for (HiveConfig config : configList) {
				System.out.println(config.getClusterName());
			}
		else System.out.println("No Hive cluster");

		return null;
	}

}
