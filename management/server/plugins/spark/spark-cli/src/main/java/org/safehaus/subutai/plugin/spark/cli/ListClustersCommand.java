package org.safehaus.subutai.plugin.spark.cli;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;
import org.safehaus.subutai.plugin.spark.api.Spark;

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
		List<SparkClusterConfig> configList = sparkManager.getClusters();
		if (!configList.isEmpty())
			for (SparkClusterConfig config : configList) {
				System.out.println(config.getClusterName());
			}
		else System.out.println("No Spark cluster");

		return null;
	}
}
