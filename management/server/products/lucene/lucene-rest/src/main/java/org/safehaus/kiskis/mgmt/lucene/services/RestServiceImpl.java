package org.safehaus.kiskis.mgmt.lucene.services;

import org.safehaus.kiskis.mgmt.api.lucene.Lucene;

/**
 * Created by bahadyr on 5/6/14.
 */

public class RestServiceImpl implements RestService {

    private Lucene luceneManager;

    public Lucene getLuceneManager() {
        return luceneManager;
    }

    public void setLuceneManager(Lucene luceneManager) {
        this.luceneManager = luceneManager;
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
