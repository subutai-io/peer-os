package org.safehaus.subutai.plugin.nutch.cli;


import java.util.List;

import org.safehaus.subutai.plugin.nutch.api.Nutch;
import org.safehaus.subutai.plugin.nutch.api.NutchConfig;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


@Command(scope = "nutch", name = "list-clusters", description = "mydescription")
public class ListClustersCommand extends OsgiCommandSupport
{

    private Nutch nutchManager;


    public Nutch getNutchManager()
    {
        return nutchManager;
    }


    public void setNutchManager( Nutch nutchManager )
    {
        this.nutchManager = nutchManager;
    }


    protected Object doExecute()
    {
        List<NutchConfig> configList = nutchManager.getClusters();
        if ( !configList.isEmpty() )
        {
            for ( NutchConfig config : configList )
            {
                System.out.println( config.getClusterName() );
            }
        }
        else
        {
            System.out.println( "No Lucene cluster" );
        }

        return null;
    }
}
