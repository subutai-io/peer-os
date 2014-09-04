package org.safehaus.subutai.plugin.cassandra.cli;


import java.util.UUID;

import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.cassandra.api.Cassandra;
import org.safehaus.subutai.plugin.cassandra.api.CassandraClusterConfig;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * Displays the last log entries
 */
@Command( scope = "cassandra", name = "uninstall-cluster", description = "Command to uninstall Cassandra cluster" )
public class UninstallClusterCommand extends OsgiCommandSupport {

    @Argument( index = 0, name = "clusterName", description = "The name of the cluster.", required = true,
            multiValued = false )
    String clusterName = null;
    private Cassandra cassandraManager;
    private Tracker tracker;


    public Tracker getTracker() {
        return tracker;
    }


    public void setTracker( Tracker tracker ) {
        this.tracker = tracker;
    }


    public Cassandra getCassandraManager() {
        return cassandraManager;
    }


    public void setCassandraManager( Cassandra cassandraManager ) {
        this.cassandraManager = cassandraManager;
    }


    protected Object doExecute() {
        UUID uuid = cassandraManager.uninstallCluster( clusterName );
        tracker.printOperationLog( CassandraClusterConfig.PRODUCT_KEY, uuid, 30000 );
        return null;
    }
}
