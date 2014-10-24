package org.safehaus.subutai.plugin.lucene.cli;


import java.util.UUID;

import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.lucene.api.Lucene;
import org.safehaus.subutai.plugin.lucene.api.LuceneConfig;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


@Command( scope = "lucene", name = "uninstall-cluster", description = "Command to uninstall Lucene cluster" )
public class UninstallClusterCommand extends OsgiCommandSupport
{

    @Argument( index = 0, name = "clusterName", description = "The name of the cluster.", required = true,
            multiValued = false )
    String clusterName = null;
    private Lucene luceneManager;
    private Tracker tracker;


    public Tracker getTracker()
    {
        return tracker;
    }


    public void setTracker( Tracker tracker )
    {
        this.tracker = tracker;
    }


    public Lucene getLuceneManager()
    {
        return luceneManager;
    }


    public void setLuceneManager( Lucene luceneManager )
    {
        this.luceneManager = luceneManager;
    }


    protected Object doExecute()
    {
        UUID uuid = luceneManager.uninstallCluster( clusterName );

        tracker.printOperationLog( LuceneConfig.PRODUCT_KEY, uuid, 10 * 60 * 1000 );

        return null;
    }
}
