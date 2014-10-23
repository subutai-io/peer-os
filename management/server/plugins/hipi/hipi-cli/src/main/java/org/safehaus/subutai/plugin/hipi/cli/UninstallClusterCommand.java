package org.safehaus.subutai.plugin.hipi.cli;


import java.util.UUID;

import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.hipi.api.Hipi;
import org.safehaus.subutai.plugin.hipi.api.HipiConfig;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


@Command(scope = "hipi", name = "uninstall-cluster", description = "Command to uninstall Lucene cluster")
public class UninstallClusterCommand extends OsgiCommandSupport
{

    @Argument(index = 0, name = "clusterName", description = "The name of the cluster.", required = true,
            multiValued = false)
    String clusterName = null;
    private Hipi hipiManager;
    private Tracker tracker;


    public Tracker getTracker()
    {
        return tracker;
    }


    public void setTracker( Tracker tracker )
    {
        this.tracker = tracker;
    }


    public Hipi getHipiManager()
    {
        return hipiManager;
    }


    public void setHipiManager( Hipi hipiManager )
    {
        this.hipiManager = hipiManager;
    }


    protected Object doExecute()
    {
        UUID uuid = hipiManager.uninstallCluster( clusterName );

        tracker.printOperationLog( HipiConfig.PRODUCT_KEY, uuid, 10 * 60 * 1000 );

        return null;
    }
}
