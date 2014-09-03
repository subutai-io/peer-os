package org.safehaus.subutai.cli.commands;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.api.cassandra.Cassandra;
import org.safehaus.subutai.api.cassandra.Config;
import org.safehaus.subutai.core.tracker.api.Tracker;

import java.util.List;


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
        List<Config> list = cassandraManager.getClusters();
        if (list.size() > 0) {
            StringBuilder sb = new StringBuilder();

            for (Config config : list) {
                sb.append(config.getClusterName()).append("\n");
            }
            System.out.println(sb.toString());
        } else System.out.println("No clusters found...");

        return null;
    }
}
