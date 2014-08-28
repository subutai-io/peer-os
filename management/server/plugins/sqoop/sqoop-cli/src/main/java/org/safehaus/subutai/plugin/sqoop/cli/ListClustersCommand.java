package org.safehaus.subutai.plugin.sqoop.cli;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.plugin.sqoop.api.SqoopConfig;
import org.safehaus.subutai.plugin.sqoop.api.Sqoop;

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
		List<SqoopConfig> configList = sqoopManager.getClusters();
		if (!configList.isEmpty())
			for (SqoopConfig config : configList) {
				System.out.println(config.getClusterName());
			}
		else System.out.println("No Sqoop cluster");

		return null;
	}
}
