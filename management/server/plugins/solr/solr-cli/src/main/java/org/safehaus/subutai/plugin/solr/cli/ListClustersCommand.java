package org.safehaus.subutai.plugin.solr.cli;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.plugin.solr.api.SolrClusterConfig;
import org.safehaus.subutai.plugin.solr.api.Solr;

import java.util.List;


/**
 * Displays the last log entries
 */
@Command(scope = "solr", name = "list-clusters", description = "mydescription")
public class ListClustersCommand extends OsgiCommandSupport {

    private Solr solrManager;

    public Solr getSolrManager() {
        return solrManager;
    }

    public void setSolrManager(Solr solrManager) {
        this.solrManager = solrManager;
    }

    protected Object doExecute() {
        List<SolrClusterConfig> solrClusterConfigList = solrManager.getClusters();
        if (!solrClusterConfigList.isEmpty())
            for (SolrClusterConfig solrClusterConfig : solrClusterConfigList ) {
                System.out.println( solrClusterConfig.getClusterName());
            }
        else System.out.println("No Solr cluster");

        return null;
    }
}
