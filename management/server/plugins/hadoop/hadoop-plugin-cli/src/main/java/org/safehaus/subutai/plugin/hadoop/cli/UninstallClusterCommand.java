package org.safehaus.subutai.plugin.hadoop.cli;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.api.tracker.Tracker;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.shared.operation.ProductOperationState;
import org.safehaus.subutai.shared.operation.ProductOperationView;

import java.util.UUID;


/**
 * Displays the last log entries
 */
@Command (scope = "hadoop", name = "uninstall-cluster", description = "Command to uninstall Hadoop cluster")
public class UninstallClusterCommand extends OsgiCommandSupport {

	private Hadoop hadoopManager;
	private Tracker tracker;

	public Tracker getTracker() {
		return tracker;
	}

	public void setTracker(Tracker tracker) {
		this.tracker = tracker;
	}

	public void setHadoopManager(Hadoop hadoopManager) {
		this.hadoopManager = hadoopManager;
	}

	public Hadoop getHadoopManager() {
		return hadoopManager;
	}


	@Argument (index = 0, name = "clusterName", description = "The name of the cluster.", required = true, multiValued = false)
	String clusterName = null;

	protected Object doExecute() {
		UUID uuid = hadoopManager.uninstallCluster(clusterName);
		int logSize = 0;
		while (!Thread.interrupted()) {
			ProductOperationView po = tracker.getProductOperation( HadoopClusterConfig.PRODUCT_KEY, uuid);
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
