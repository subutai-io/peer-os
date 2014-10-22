package org.safehaus.subutai.plugin.mahout.cli;


import java.util.UUID;

import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.common.tracker.TrackerOperationView;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.mahout.api.Mahout;
import org.safehaus.subutai.plugin.mahout.api.MahoutClusterConfig;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * Displays the last log entries
 */
@Command(scope = "mahout", name = "uninstall-cluster", description = "Command to uninstall Mahout cluster")
public class UninstallClusterCommand extends OsgiCommandSupport
{

    @Argument(index = 0, name = "clusterName", description = "The name of the cluster.", required = true,
            multiValued = false)
    String clusterName = null;
    private Mahout mahoutManager;
    private Tracker tracker;


    public Tracker getTracker()
    {
        return tracker;
    }


    public void setTracker( Tracker tracker )
    {
        this.tracker = tracker;
    }


    public Mahout getMahoutManager()
    {
        return mahoutManager;
    }


    public void setMahoutManager( Mahout mahoutManager )
    {
        this.mahoutManager = mahoutManager;
    }


    protected Object doExecute()
    {
        UUID uuid = mahoutManager.uninstallCluster( clusterName );
        int logSize = 0;
        while ( !Thread.interrupted() )
        {
            TrackerOperationView po = tracker.getTrackerOperation( MahoutClusterConfig.PRODUCT_KEY, uuid );
            if ( po != null )
            {
                if ( logSize != po.getLog().length() )
                {
                    System.out.print( po.getLog().substring( logSize, po.getLog().length() ) );
                    System.out.flush();
                    logSize = po.getLog().length();
                }
                if ( po.getState() != ProductOperationState.RUNNING )
                {
                    break;
                }
            }
            else
            {
                System.out.println( "Product operation not found. Check logs" );
                break;
            }
            try
            {
                Thread.sleep( 1000 );
            }
            catch ( InterruptedException ex )
            {
                break;
            }
        }
        return null;
    }
}
