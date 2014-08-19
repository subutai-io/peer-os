package org.safehaus.subutai.cli.commands;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.api.cassandra.Cassandra;
import org.safehaus.subutai.api.cassandra.Config;
import org.safehaus.subutai.api.tracker.Tracker;
import org.safehaus.subutai.shared.operation.ProductOperationState;
import org.safehaus.subutai.shared.operation.ProductOperationView;

import java.io.IOException;
import java.util.UUID;


/**
 * Displays the last log entries
 */
@Command (scope = "cassandra", name = "stop-cluster", description = "Command to stop Cassandra cluster")
public class StopAllNodesCommand extends OsgiCommandSupport {

	private static Cassandra cassandraManager;
	private static Tracker tracker;
	@Argument (index = 0, name = "clusterName", description = "The name of the cluster.", required = true, multiValued = false)
	String clusterName = null;

	public static Cassandra getCassandraManager() {
		return cassandraManager;
	}

	public void setCassandraManager(Cassandra cassandraManager) {
		StopAllNodesCommand.cassandraManager = cassandraManager;
	}

	public Tracker getTracker() {
		return tracker;
	}

	public void setTracker(Tracker tracker) {
		StopAllNodesCommand.tracker = tracker;
	}

	protected Object doExecute() throws IOException {

		UUID uuid = cassandraManager.stopAllNodes(clusterName);
		int logSize = 0;
		while (!Thread.interrupted()) {
			ProductOperationView po = tracker.getProductOperation(Config.PRODUCT_KEY, uuid);
			if (po != null) {
				if (logSize != po.getLog().length()) {
					System.out.print(po.getLog().substring(logSize, po.getLog().length()));
					System.out.flush();
					logSize = po.getLog().length();
				}
				if (po.getState() != ProductOperationState.RUNNING) {
					break;
				}
			} else {
				System.out.println("Product operation not found. Check logs");
				break;
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ex) {
				break;
			}
		}

		return null;
	}
}
