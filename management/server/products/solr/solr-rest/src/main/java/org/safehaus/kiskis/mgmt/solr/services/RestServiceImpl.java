package org.safehaus.kiskis.mgmt.solr.services;


import org.safehaus.kiskis.mgmt.api.solr.Solr;


/**
 * Created by bahadyr on 5/6/14.
 */

public class RestServiceImpl implements RestService {

    private Solr solrManager;

    public Solr getSolrManager() {
        return solrManager;
    }

    public void setSolrManager(Solr solrManager) {
        this.solrManager = solrManager;
    }

    @Override
    public String installCluster(String clusterName) {
        return null;
    }

    @Override
    public String uninstallCluster(String clusterName) {
        return null;
    }
}
