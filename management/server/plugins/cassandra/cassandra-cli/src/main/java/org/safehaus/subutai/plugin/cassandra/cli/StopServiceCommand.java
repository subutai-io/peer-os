package org.safehaus.subutai.plugin.cassandra.cli;


import java.io.IOException;
import java.util.UUID;

import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.cassandra.api.Cassandra;
import org.safehaus.subutai.plugin.cassandra.api.CassandraClusterConfig;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * Displays the last log entries
 */
@Command(scope = "cassandra", name = "service-cassandra-stop", description = "Command to stop Cassandra service")
public class StopServiceCommand extends OsgiCommandSupport
{

    @Argument(index = 0, name = "clusterName", description = "Name of the cluster.", required = true,
            multiValued = false)
    String clusterName = null;
    @Argument(index = 1, name = "hostanme", description = "UUID of the agent.", required = true,
            multiValued = false)
    String hostname = null;
    private Cassandra cassandraManager;
    private Tracker tracker;


    public Cassandra getCassandraManager()
    {
        return cassandraManager;
    }


    public void setCassandraManager( Cassandra cassandraManager )
    {
        this.cassandraManager = cassandraManager;
    }


    public Tracker getTracker()
    {
        return tracker;
    }


    public void setTracker( Tracker tracker )
    {
        this.tracker = tracker;
    }


    protected Object doExecute() throws IOException
    {

        UUID uuid = cassandraManager.stopService( clusterName, hostname );
        tracker.printOperationLog( CassandraClusterConfig.PRODUCT_KEY, uuid, 30000 );

        return null;
    }
}
