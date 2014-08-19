package org.safehaus.subutai.cli.commands;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.api.hbase.HBase;
import org.safehaus.subutai.api.hbase.HBaseConfig;
import org.safehaus.subutai.api.tracker.Tracker;

import java.util.UUID;


/**
 * Displays the last log entries
 */
@Command (scope = "hbase", name = "uninstall-cluster", description = "Command to uninstall HBase cluster")
public class UninstallHBaseClusterCommand extends OsgiCommandSupport {

	@Argument (index = 0, name = "clusterName", required = true, multiValued = false, description = "Delete cluster")
	String clusterName;
	private HBase hbaseManager;
	private Tracker tracker;

	public Tracker getTracker() {
		return tracker;
	}

	public void setTracker(Tracker tracker) {
		this.tracker = tracker;
	}

	public HBase getHbaseManager() {
		return hbaseManager;
	}

	public void setHbaseManager(HBase hbaseManager) {
		this.hbaseManager = hbaseManager;
	}

	protected Object doExecute() {

		UUID uuid = hbaseManager.uninstallCluster(clusterName);
		tracker.printOperationLog(HBaseConfig.PRODUCT_KEY, uuid, 30000);
		return null;

	}
}
