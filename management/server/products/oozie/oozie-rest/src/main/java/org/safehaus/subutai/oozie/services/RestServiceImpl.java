package org.safehaus.subutai.oozie.services;

import org.safehaus.subutai.api.oozie.Oozie;

/**
 * Created by bahadyr on 5/6/14.
 */

public class RestServiceImpl implements RestService {

    private Oozie oozieManager;

    public Oozie getOozieManager() {
        return oozieManager;
    }

    public void setOozieManager(Oozie oozieManager) {
        this.oozieManager = oozieManager;
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