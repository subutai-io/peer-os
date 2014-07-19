package org.safehaus.subutai.cli.elasticsearch;

import org.safehaus.subutai.api.elasticsearch.Elasticsearch;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


@Command( scope = "elasticsearch", name = "install" )
public class InstallCommand extends OsgiCommandSupport {

    private Elasticsearch elasticsearch;


    public void setElasticsearch( Elasticsearch elasticsearch ) {
        this.elasticsearch = elasticsearch;
    }


    protected Object doExecute() {

        System.out.println( "elasticsearch: " + elasticsearch );

        return null;
    }
}
