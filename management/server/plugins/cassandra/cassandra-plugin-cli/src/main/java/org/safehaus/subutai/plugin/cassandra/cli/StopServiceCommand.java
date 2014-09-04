package org.safehaus.subutai.plugin.cassandra.cli;


import java.io.IOException;
import java.util.UUID;

import org.safehaus.subutai.plugin.cassandra.api.Cassandra;
import org.safehaus.subutai.plugin.cassandra.api.CassandraConfig;
import org.safehaus.subutai.core.tracker.api.Tracker;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * Displays the last log entries
 */
@Command(scope = "cassandra", name = "service-cassandra-stop", description = "Command to stop Cassandra service")
public class StopServiceCommand extends OsgiCommandSupport {

    private static Cassandra cassandraManager;
    private static Tracker tracker;


    public Tracker getTracker() {
        return tracker;
    }


    public void setTracker( Tracker tracker ) {
        StopServiceCommand.tracker = tracker;
    }


    public void setCassandraManager( Cassandra cassandraManager ) {
        StopServiceCommand.cassandraManager = cassandraManager;
    }


    public static Cassandra getCassandraManager() {
        return cassandraManager;
    }


    @Argument( index = 0, name = "clusterName", description = "Name of the cluster.", required = true,
            multiValued = false )
    String clusterName = null;
    @Argument(index = 1, name = "agentUUID", description = "UUID of the agent.", required = true, multiValued = false)
    String agentUUID = null;


    protected Object doExecute() throws IOException {

        UUID uuid = cassandraManager.stopService( clusterName, agentUUID );
        tracker.printOperationLog( CassandraConfig.PRODUCT_KEY, uuid, 30000 );

        return null;
    }
}
