package org.safehaus.subutai.cli.commands;


import java.util.UUID;

import org.safehaus.subutai.api.lucene.Config;
import org.safehaus.subutai.api.lucene.Lucene;
import org.safehaus.subutai.api.tracker.Tracker;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * Displays the last log entries
 */
@Command( scope = "lucene", name = "uninstall-cluster", description = "Command to uninstall Lucene cluster" )
public class UninstallClusterCommand extends OsgiCommandSupport {

    private Lucene luceneManager;
    private Tracker tracker;


    public Tracker getTracker() {
        return tracker;
    }


    public void setTracker( Tracker tracker ) {
        this.tracker = tracker;
    }


    public void setLuceneManager( Lucene luceneManager ) {
        this.luceneManager = luceneManager;
    }


    public Lucene getLuceneManager() {
        return luceneManager;
    }


    @Argument( index = 0, name = "clusterName", description = "The name of the cluster.", required = true,
            multiValued = false )
    String clusterName = null;


    protected Object doExecute() {
        UUID uuid = luceneManager.uninstallCluster( clusterName );

        tracker.printOperationLog( Config.PRODUCT_KEY, uuid, 10 * 60 * 1000 );

        return null;
    }
}
