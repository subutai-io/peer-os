package org.safehaus.subutai.plugin.cassandra.cli;


import java.util.List;

import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.cassandra.api.Cassandra;
import org.safehaus.subutai.plugin.cassandra.api.CassandraClusterConfig;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * Displays the last log entries
 */
@Command(scope = "cassandra", name = "list-clusters", description = "Gets the list of Cassandra clusters")
public class ListClustersCommand extends OsgiCommandSupport {

    private Cassandra cassandraManager;
    private Tracker tracker;


    public Cassandra getCassandraManager() {
        return cassandraManager;
    }


    public void setCassandraManager( Cassandra cassandraManager ) {
        this.cassandraManager = cassandraManager;
    }


    public Tracker getTracker() {
        return tracker;
    }


    public void setTracker( Tracker tracker ) {
        this.tracker = tracker;
    }


    protected Object doExecute() {
        List<CassandraClusterConfig> list = cassandraManager.getClusters();
        if ( list.size() > 0 ) {
            StringBuilder sb = new StringBuilder();

            for ( CassandraClusterConfig config : list ) {
                sb.append( config.getClusterName() ).append( "\n" );
            }
            System.out.println( sb.toString() );
        }
        else {
            System.out.println( "No clusters found..." );
        }

        return null;
    }
}
