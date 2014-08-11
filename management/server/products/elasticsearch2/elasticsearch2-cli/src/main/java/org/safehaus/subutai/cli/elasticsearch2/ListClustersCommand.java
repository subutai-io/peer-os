package org.safehaus.subutai.cli.elasticsearch2;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.api.elasticsearch2.Elasticsearch;
import org.safehaus.subutai.api.elasticsearch2.Config;
import org.safehaus.subutai.api.tracker.Tracker;

import java.util.List;


/**
 * Displays the last log entries
 */
@Command(scope = "cassandra", name = "list-clusters", description = "Gets the list of Cassandra clusters")
public class ListClustersCommand extends OsgiCommandSupport {

    private static Elasticsearch cassandraManager;
    private static Tracker tracker;

    public Tracker getTracker() {
        return tracker;
    }

    public void setTracker(Tracker tracker) {
        ListClustersCommand.tracker = tracker;
    }

    public void setCassandraManager(Elasticsearch cassandraManager) {
        ListClustersCommand.cassandraManager = cassandraManager;
    }

    public static Elasticsearch getCassandraManager() {
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
