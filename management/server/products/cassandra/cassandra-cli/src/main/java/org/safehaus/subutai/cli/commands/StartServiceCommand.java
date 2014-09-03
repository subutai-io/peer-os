package org.safehaus.subutai.cli.commands;


import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.api.cassandra.Cassandra;
import org.safehaus.subutai.api.cassandra.Config;
import org.safehaus.subutai.core.tracker.api.Tracker;

import java.io.IOException;
import java.util.UUID;


/**
 * Displays the last log entries
 */
@Command(scope = "cassandra", name = "service-cassandra-start", description = "Command to start Cassandra service")
public class StartServiceCommand extends OsgiCommandSupport
{

    private static Cassandra cassandraManager;
    private static Tracker tracker;


    public Tracker getTracker()
    {
        return tracker;
    }


    public void setTracker( Tracker tracker )
    {
        StartServiceCommand.tracker = tracker;
    }


    public void setCassandraManager( Cassandra cassandraManager )
    {
        StartServiceCommand.cassandraManager = cassandraManager;
    }


    public static Cassandra getCassandraManager()
    {
        return cassandraManager;
    }


    @Argument(index = 0, name = "agentUUID", description = "UUID of the agent.", required = true, multiValued = false)
    String agentUUID = null;


    protected Object doExecute() throws IOException
    {

        UUID uuid = cassandraManager.startCassandraService( agentUUID );
        tracker.printOperationLog( Config.PRODUCT_KEY, uuid, 30000 );

        return null;
    }
}
