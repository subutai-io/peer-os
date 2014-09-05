package org.safehaus.subutai.cli.commands;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.api.hadoop.HadoopClusterConfig;
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

		List<HadoopClusterConfig> hadoopClusterConfigList = hadoopManager.getClusters();
		if (!hadoopClusterConfigList.isEmpty())
			for (HadoopClusterConfig hadoopClusterConfig : hadoopClusterConfigList ) {
				System.out.println( hadoopClusterConfig.getClusterName());
			}
		else System.out.println("No Hadoop cluster");

		return null;
	}
}
