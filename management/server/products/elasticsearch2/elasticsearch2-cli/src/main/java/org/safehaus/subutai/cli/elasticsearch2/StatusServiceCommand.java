package org.safehaus.subutai.cli.elasticsearch2;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.api.elasticsearch2.Cassandra;
import org.safehaus.subutai.api.elasticsearch2.Config;
import org.safehaus.subutai.api.tracker.Tracker;

import java.io.IOException;
import java.util.UUID;


/**
 * Displays the last log entries
 */
@Command(scope = "cassandra", name = "service-cassandra-status", description = "Command to check Cassandra service")
public class StatusServiceCommand extends OsgiCommandSupport {

    private static Cassandra cassandraManager;
    private static Tracker tracker;

    public Tracker getTracker() {
        return tracker;
    }

    public void setTracker(Tracker tracker) {
        StatusServiceCommand.tracker = tracker;
    }

    public void setCassandraManager(Cassandra cassandraManager) {
        StatusServiceCommand.cassandraManager = cassandraManager;
    }

    public static Cassandra getCassandraManager() {
        return cassandraManager;
    }


    @Argument(index = 0, name = "agentUUID", description = "UUID of the agent.", required = true, multiValued = false)
    String agentUUID = null;


    protected Object doExecute() throws IOException {

        UUID uuid = cassandraManager.statusCassandraService(agentUUID);
        tracker.printOperationLog(Config.PRODUCT_KEY, uuid, 30000);

        return null;
    }
}
