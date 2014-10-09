package org.safehaus.subutai.plugin.accumulo.cli;


import java.util.List;

import org.safehaus.subutai.plugin.accumulo.api.Accumulo;
import org.safehaus.subutai.plugin.accumulo.api.AccumuloClusterConfig;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * Displays the last log entries
 */
@Command(scope = "accumulo", name = "list-clusters", description = "mydescription")
public class ListClustersCommand extends OsgiCommandSupport
{

    private Accumulo accumuloManager;


    public Accumulo getAccumuloManager()
    {
        return accumuloManager;
    }


    public void setAccumuloManager( Accumulo accumuloManager )
    {
        this.accumuloManager = accumuloManager;
    }


    protected Object doExecute()
    {
        List<AccumuloClusterConfig> accumuloClusterConfigList = accumuloManager.getClusters();
        if ( !accumuloClusterConfigList.isEmpty() )
        {
            for ( AccumuloClusterConfig accumuloClusterConfig : accumuloClusterConfigList )
            {
                System.out.println( accumuloClusterConfig.getClusterName() );
            }
        }
        else
        {
            System.out.println( "No Accumulo cluster" );
        }

        return null;
    }
}
