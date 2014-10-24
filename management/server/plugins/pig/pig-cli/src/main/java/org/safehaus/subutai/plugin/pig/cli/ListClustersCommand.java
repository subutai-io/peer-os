package org.safehaus.subutai.plugin.pig.cli;


import java.util.List;

import org.safehaus.subutai.plugin.pig.api.Pig;
import org.safehaus.subutai.plugin.pig.api.PigConfig;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


@Command( scope = "pig", name = "list-clusters", description = "mydescription" )
public class ListClustersCommand extends OsgiCommandSupport
{

    private Pig pigManager;


    public Pig getPigManager()
    {
        return pigManager;
    }


    public void setPigManager( Pig pigManager )
    {
        this.pigManager = pigManager;
    }


    protected Object doExecute()
    {
        List<PigConfig> configList = pigManager.getClusters();
        if ( !configList.isEmpty() )
        {
            for ( PigConfig config : configList )
            {
                System.out.println( config.getClusterName() );
            }
        }
        else
        {
            System.out.println( "No Pig cluster" );
        }

        return null;
    }
}
