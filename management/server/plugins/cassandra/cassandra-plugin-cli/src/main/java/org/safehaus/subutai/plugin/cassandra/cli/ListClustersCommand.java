package org.safehaus.subutai.plugin.cassandra.cli;

import java.util.List;

import org.safehaus.subutai.plugin.cassandra.api.Cassandra;
import org.safehaus.subutai.plugin.cassandra.api.CassandraConfig;
import org.safehaus.subutai.api.tracker.Tracker;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * Displays the last log entries
 */
@Command(scope = "cassandra", name = "list-clusters", description = "Gets the list of Cassandra clusters")
public class ListClustersCommand extends OsgiCommandSupport {

    private static Cassandra cassandraManager;
    private static Tracker tracker;

    public Tracker getTracker() {
        return tracker;
    }

    public void setTracker(Tracker tracker) {
        ListClustersCommand.tracker = tracker;
    }

    public void setCassandraManager(Cassandra cassandraManager) {
        ListClustersCommand.cassandraManager = cassandraManager;
    }

    public static Cassandra getCassandraManager() {
        return cassandraManager;
    }

    protected Object doExecute() {
        List<CassandraConfig> list = cassandraManager.getClusters();
        if (list.size() > 0) {
            StringBuilder sb = new StringBuilder();

            for (CassandraConfig config : list) {
                sb.append(config.getClusterName()).append("\n");
            }
            System.out.println(sb.toString());
        } else System.out.println("No clusters found...");

        return null;
    }
}
