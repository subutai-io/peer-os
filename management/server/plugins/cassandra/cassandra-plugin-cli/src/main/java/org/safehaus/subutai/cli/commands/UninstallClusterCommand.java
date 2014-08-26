package org.safehaus.subutai.cli.commands;

import java.util.UUID;

import org.safehaus.subutai.api.cassandra.Cassandra;
import org.safehaus.subutai.api.cassandra.CassandraConfig;
import org.safehaus.subutai.api.tracker.Tracker;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * Displays the last log entries
 */
@Command(scope = "cassandra", name = "uninstall-cluster", description = "Command to uninstall Cassandra cluster")
public class UninstallClusterCommand extends OsgiCommandSupport {

    private Cassandra cassandraManager;
    private Tracker tracker;

    public Tracker getTracker() {
        return tracker;
    }

    public void setTracker(Tracker tracker) {
        this.tracker = tracker;
    }

    public void setCassandraManager(Cassandra cassandraManager) {
        this.cassandraManager = cassandraManager;
    }

    public Cassandra getCassandraManager() {
        return cassandraManager;
    }


    @Argument(index = 0, name = "clusterName", description = "The name of the cluster.", required = true, multiValued = false)
    String clusterName = null;

    protected Object doExecute() {
        UUID uuid = cassandraManager.uninstallCluster(clusterName);
        tracker.printOperationLog(CassandraConfig.PRODUCT_KEY, uuid, 30000);
        return null;
    }
}
