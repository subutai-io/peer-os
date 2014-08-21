package org.safehaus.subutai.cli.commands;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.api.spark.Config;
import org.safehaus.subutai.api.spark.Spark;

import java.util.List;


/**
 * Displays the last log entries
 */
@Command (scope = "spark", name = "list-clusters", description = "mydescription")
public class ListClustersCommand extends OsgiCommandSupport {

	private Spark sparkManager;

	public Spark getSparkManager() {
		return sparkManager;
	}

	public void setSparkManager(Spark sparkManager) {
		this.sparkManager = sparkManager;
	}

	protected Object doExecute() {
		List<Config> configList = sparkManager.getClusters();
		if (!configList.isEmpty())
			for (Config config : configList) {
				System.out.println(config.getClusterName());
			}
		else System.out.println("No Spark cluster");

		return null;
	}
}
