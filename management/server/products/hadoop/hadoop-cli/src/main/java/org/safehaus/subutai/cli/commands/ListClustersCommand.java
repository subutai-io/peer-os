package org.safehaus.subutai.cli.commands;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.api.hadoop.Config;
import org.safehaus.subutai.api.hadoop.Hadoop;

import java.util.List;


/**
 * Displays the last log entries
 */
@Command (scope = "hadoop", name = "list-clusters", description = "Shows the list of Hadoop clusters")
public class ListClustersCommand extends OsgiCommandSupport {

	private Hadoop hadoopManager;

	public Hadoop getHadoopManager() {
		return hadoopManager;
	}

	public void setHadoopManager(Hadoop hadoopManager) {
		this.hadoopManager = hadoopManager;
	}

	protected Object doExecute() {

		List<Config> configList = hadoopManager.getClusters();
		if (!configList.isEmpty())
			for (Config config : configList) {
				System.out.println(config.getClusterName());
			}
		else System.out.println("No Hadoop cluster");

		return null;
	}
}
