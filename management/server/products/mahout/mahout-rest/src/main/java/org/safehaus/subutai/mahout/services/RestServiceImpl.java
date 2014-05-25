package org.safehaus.subutai.mahout.services;

import org.safehaus.subutai.api.mahout.Mahout;

/**
 * Created by bahadyr on 5/6/14.
 */

public class RestServiceImpl implements RestService {

    private Mahout mahoutManager;

    public Mahout getMahoutManager() {
        return mahoutManager;
    }

    public void setMahoutManager(Mahout mahoutManager) {
        this.mahoutManager = mahoutManager;
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
