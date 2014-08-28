package org.safehaus.subutai.plugin.sqoop.cli;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.plugin.sqoop.api.SqoopConfig;
import org.safehaus.subutai.plugin.sqoop.api.Sqoop;
import org.safehaus.subutai.api.tracker.Tracker;
import org.safehaus.subutai.shared.operation.ProductOperationState;
import org.safehaus.subutai.shared.operation.ProductOperationView;

import java.util.UUID;


/**
 * Displays the last log entries
 */
@Command (scope = "sqoop", name = "uninstall-cluster", description = "Command to uninstall Sqoop cluster")
public class UninstallClusterCommand extends OsgiCommandSupport {

	@Argument (index = 0, name = "clusterName", description = "The name of the cluster.", required = true, multiValued = false)
	String clusterName = null;
	private Sqoop sqoopManager;
	private Tracker tracker;

	public Tracker getTracker() {
		return tracker;
	}

	public void setTracker(Tracker tracker) {
		this.tracker = tracker;
	}

	public Sqoop getSqoopManager() {
		return sqoopManager;
	}

	public void setSqoopManager(Sqoop sqoopManager) {
		this.sqoopManager = sqoopManager;
	}

	protected Object doExecute() {
		UUID uuid = sqoopManager.uninstallCluster(clusterName);
		int logSize = 0;
		while (!Thread.interrupted()) {
			ProductOperationView po = tracker.getProductOperation(SqoopConfig.PRODUCT_KEY, uuid);
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
