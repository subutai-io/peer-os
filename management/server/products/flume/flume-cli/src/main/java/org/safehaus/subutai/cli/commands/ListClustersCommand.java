package org.safehaus.subutai.cli.commands;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.api.flume.Config;
import org.safehaus.subutai.api.flume.Flume;

import java.util.List;


/**
 * Displays the last log entries
 */
@Command (scope = "flume", name = "list-clusters", description = "mydescription")
public class ListClustersCommand extends OsgiCommandSupport {

	private Flume flumeManager;

	public Flume getFlumeManager() {
		return flumeManager;
	}

	public void setFlumeManager(Flume flumeManager) {
		this.flumeManager = flumeManager;
	}

	protected Object doExecute() {

		List<Config> configList = flumeManager.getClusters();
		if (!configList.isEmpty())
			for (Config config : configList) {
				System.out.println(config.getClusterName());
			}
		else System.out.println("No Flume clusters");

		return null;
	}
}
