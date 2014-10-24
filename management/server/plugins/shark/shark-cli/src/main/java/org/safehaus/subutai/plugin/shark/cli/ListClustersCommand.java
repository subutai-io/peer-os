package org.safehaus.subutai.plugin.shark.cli;


import java.util.List;

import org.safehaus.subutai.plugin.shark.api.Shark;
import org.safehaus.subutai.plugin.shark.api.SharkClusterConfig;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * Displays the last log entries
 */
@Command( scope = "shark", name = "list-clusters", description = "mydescription" )
public class ListClustersCommand extends OsgiCommandSupport
{

    private Shark sharkManager;


    public Shark getSharkManager()
    {
        return sharkManager;
    }


    public void setSharkManager( Shark sharkManager )
    {
        this.sharkManager = sharkManager;
    }


    protected Object doExecute()
    {
        List<SharkClusterConfig> configList = sharkManager.getClusters();
        if ( !configList.isEmpty() )
        {
            for ( SharkClusterConfig config : configList )
            {
                System.out.println( config.getClusterName() );
            }
        }
        else
        {
            System.out.println( "No Shark cluster" );
        }

        return null;
    }
}
