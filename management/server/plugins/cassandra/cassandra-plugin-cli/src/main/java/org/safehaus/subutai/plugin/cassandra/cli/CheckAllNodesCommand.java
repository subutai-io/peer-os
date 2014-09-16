package org.safehaus.subutai.plugin.cassandra.cli;


import java.io.IOException;
import java.util.UUID;

import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.common.tracker.ProductOperationView;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.cassandra.api.Cassandra;
import org.safehaus.subutai.plugin.cassandra.api.CassandraClusterConfig;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * Displays the last log entries
 */
@Command(scope = "cassandra", name = "check-cluster", description = "Command to check Cassandra cluster")
public class CheckAllNodesCommand extends OsgiCommandSupport {

    private Cassandra cassandraManager;
    private Tracker tracker;
    @Argument(index = 0, name = "clusterName", description = "The name of the cluster.", required = true,
            multiValued = false)
    String clusterName = null;


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


    protected Object doExecute() throws IOException {

        UUID uuid = cassandraManager.checkCluster( clusterName );
        int logSize = 0;
        while ( !Thread.interrupted() ) {
            ProductOperationView po = tracker.getProductOperation( CassandraClusterConfig.PRODUCT_KEY, uuid );
            if ( po != null ) {
                if ( logSize != po.getLog().length() ) {
                    System.out.print( po.getLog().substring( logSize, po.getLog().length() ) );
                    System.out.flush();
                    logSize = po.getLog().length();
                }
                if ( po.getState() != ProductOperationState.RUNNING ) {
                    break;
                }
            }
            else {
                System.out.println( "Product operation not found. Check logs" );
                break;
            }
            try {
                Thread.sleep( 1000 );
            }
            catch ( InterruptedException ex ) {
                break;
            }
        }

        return null;
    }
}
