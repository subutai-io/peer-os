package org.safehaus.subutai.cli.commands;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.api.hive.Config;
import org.safehaus.subutai.api.hive.Hive;
import org.safehaus.subutai.api.tracker.Tracker;
import org.safehaus.subutai.shared.operation.ProductOperationState;
import org.safehaus.subutai.shared.operation.ProductOperationView;

import java.util.UUID;


/**
 * Displays the last log entries
 */
@Command (scope = "hive", name = "uninstall-cluster", description = "Command to uninstall Hive cluster")
public class UninstallClusterCommand extends OsgiCommandSupport {

	@Argument (index = 0, name = "clusterName", description = "The name of the cluster.", required = true, multiValued = false)
	String clusterName = null;
	private Hive hiveManager;
	private Tracker tracker;

	public Tracker getTracker() {
		return tracker;
	}

	public void setTracker(Tracker tracker) {
		this.tracker = tracker;
	}

	public Hive getHiveManager() {
		return hiveManager;
	}

	public void setHiveManager(Hive hiveManager) {
		this.hiveManager = hiveManager;
	}

	protected Object doExecute() {
		UUID uuid = hiveManager.uninstallCluster(clusterName);
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
