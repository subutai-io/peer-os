package org.safehaus.subutai.plugin.hive.cli;


import java.util.List;

import org.safehaus.subutai.plugin.hive.api.Hive;
import org.safehaus.subutai.plugin.hive.api.HiveConfig;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * Displays the last log entries
 */
@Command(scope = "hive", name = "list-clusters", description = "mydescription")
public class ListClustersCommand extends OsgiCommandSupport
{


    private Hive hiveManager;


    public Hive getHiveManager()
    {
        return hiveManager;
    }


    public void setHiveManager( Hive hiveManager )
    {
        this.hiveManager = hiveManager;
    }


    protected Object doExecute()
    {
        List<HiveConfig> configList = hiveManager.getClusters();
        if ( !configList.isEmpty() )
        {
            for ( HiveConfig config : configList )
            {
                System.out.println( config.getClusterName() );
            }
        }
        else
        {
            System.out.println( "No Hive cluster" );
        }

        return null;
    }
}
