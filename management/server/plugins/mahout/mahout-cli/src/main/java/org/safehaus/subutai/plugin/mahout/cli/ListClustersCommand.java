package org.safehaus.subutai.plugin.mahout.cli;


import java.util.List;

import org.safehaus.subutai.plugin.mahout.api.Mahout;
import org.safehaus.subutai.plugin.mahout.api.MahoutClusterConfig;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * Displays the last log entries
 */
@Command(scope = "mahout", name = "list-clusters", description = "mydescription")
public class ListClustersCommand extends OsgiCommandSupport
{

    private Mahout mahoutManager;


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
        List<MahoutClusterConfig> configList = mahoutManager.getClusters();
        if ( !configList.isEmpty() )
        {
            for ( MahoutClusterConfig config : configList )
            {
                System.out.println( config.getClusterName() );
            }
        }
        else
        {
            System.out.println( "No Mahout cluster" );
        }

        return null;
    }
}
