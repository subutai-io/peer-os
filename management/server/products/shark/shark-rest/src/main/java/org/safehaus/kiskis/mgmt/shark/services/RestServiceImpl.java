package org.safehaus.kiskis.mgmt.shark.services;

import org.safehaus.kiskis.mgmt.api.shark.Shark;

/**
 * Created by bahadyr on 5/6/14.
 */

public class RestServiceImpl implements RestService {

    private Shark sharkManager;

    public Shark getSharkManager() {
        return sharkManager;
    }

    public void setSharkManager(Shark sharkManager) {
        this.sharkManager = sharkManager;
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
