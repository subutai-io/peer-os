package org.safehaus.subutai.plugin.lucene.cli;


import java.util.List;

import org.safehaus.subutai.plugin.lucene.api.Lucene;
import org.safehaus.subutai.plugin.lucene.api.LuceneConfig;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


@Command(scope = "lucene", name = "list-clusters", description = "mydescription")
public class ListClustersCommand extends OsgiCommandSupport
{

    private Lucene luceneManager;


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
        List<LuceneConfig> configList = luceneManager.getClusters();
        if ( !configList.isEmpty() )
        {
            for ( LuceneConfig config : configList )
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
