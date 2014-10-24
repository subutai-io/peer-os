package org.safehaus.subutai.plugin.hipi.cli;


import java.util.List;

import org.safehaus.subutai.plugin.hipi.api.Hipi;
import org.safehaus.subutai.plugin.hipi.api.HipiConfig;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


@Command( scope = "hipi", name = "list-clusters", description = "mydescription" )
public class ListClustersCommand extends OsgiCommandSupport
{

    private Hipi hipiManager;


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
        List<HipiConfig> configList = hipiManager.getClusters();
        if ( !configList.isEmpty() )
        {
            for ( HipiConfig config : configList )
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
