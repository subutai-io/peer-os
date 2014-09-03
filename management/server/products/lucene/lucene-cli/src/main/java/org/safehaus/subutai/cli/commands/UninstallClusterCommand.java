package org.safehaus.subutai.cli.commands;


import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.api.lucene.Config;
import org.safehaus.subutai.api.lucene.Lucene;
import org.safehaus.subutai.core.tracker.api.Tracker;

import java.util.UUID;


/**
 * Displays the last log entries
 */
@Command (scope = "lucene", name = "uninstall-cluster", description = "Command to uninstall Lucene cluster")
public class UninstallClusterCommand extends OsgiCommandSupport {

	@Argument (index = 0, name = "clusterName", description = "The name of the cluster.", required = true,
			multiValued = false)
	String clusterName = null;
	private Lucene luceneManager;
	private Tracker tracker;

	public Tracker getTracker() {
		return tracker;
	}

	public void setTracker(Tracker tracker) {
		this.tracker = tracker;
	}

	public Lucene getLuceneManager() {
		return luceneManager;
	}

	public void setLuceneManager(Lucene luceneManager) {
		this.luceneManager = luceneManager;
	}

	protected Object doExecute() {
		UUID uuid = luceneManager.uninstallCluster(clusterName);

		tracker.printOperationLog(Config.PRODUCT_KEY, uuid, 10 * 60 * 1000);

		return null;
	}
}
