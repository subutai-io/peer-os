package org.safehaus.subutai.cli.commands;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.api.cassandra.Cassandra;
import org.safehaus.subutai.api.cassandra.Config;
import org.safehaus.subutai.api.tracker.Tracker;

import java.io.IOException;
import java.util.UUID;


/**
 * Displays the last log entries
 */
@Command (scope = "cassandra", name = "install-cluster", description = "Command to install Cassandra cluster")
public class InstallClusterCommand extends OsgiCommandSupport {

	private static Cassandra cassandraManager;
	private static Tracker tracker;
	@Argument (index = 0, name = "clusterName", description = "The name of the cluster.", required = true, multiValued = false)
	String clusterName = null;
	@Argument (index = 1, name = "domainName", description = "The domain name of the cluster.", required = true, multiValued = false)
	String domainName = null;
	@Argument (index = 2, name = "numberOfNodes", description = "Number of nodes in cluster.", required = true, multiValued = false)
	String numberOfNodes = null;
	@Argument (index = 3, name = "numberOfSeeds", description = "Number of seeds in cluster.", required = true, multiValued = false)
	String numberOfSeeds = null;

	public static Cassandra getCassandraManager() {
		return cassandraManager;
	}

	public void setCassandraManager(Cassandra cassandraManager) {
		InstallClusterCommand.cassandraManager = cassandraManager;
	}

	public Tracker getTracker() {
		return tracker;
	}

	public void setTracker(Tracker tracker) {
		InstallClusterCommand.tracker = tracker;
	}

	protected Object doExecute() throws IOException {
		Config config = new Config();
		config.setClusterName(clusterName);
		config.setDomainName(domainName);
		config.setNumberOfNodes(Integer.parseInt(numberOfNodes));
		config.setNumberOfSeeds(Integer.parseInt(numberOfSeeds));

		UUID uuid = cassandraManager.installCluster(config);
		tracker.printOperationLog(Config.PRODUCT_KEY, uuid, 30000);
		return null;
	}
}
