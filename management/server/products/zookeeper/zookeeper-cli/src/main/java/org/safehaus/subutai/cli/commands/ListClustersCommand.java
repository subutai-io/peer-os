package org.safehaus.subutai.cli.commands;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.api.zookeeper.Config;
import org.safehaus.subutai.api.zookeeper.Zookeeper;

import java.util.List;


/**
 * Displays the last log entries
 */
@Command (scope = "zookeeper", name = "list-clusters", description = "mydescription")
public class ListClustersCommand extends OsgiCommandSupport {

	private Zookeeper zookeeperManager;

	public Zookeeper getZookeeperManager() {
		return zookeeperManager;
	}

	public void setZookeeperManager(Zookeeper zookeeperManager) {
		this.zookeeperManager = zookeeperManager;
	}

	protected Object doExecute() {
		List<Config> configList = zookeeperManager.getClusters();
		if (!configList.isEmpty())
			for (Config config : configList) {
				System.out.println(config.getClusterName());
			}
		else System.out.println("No Zookeeper cluster");

		return null;
	}
}
