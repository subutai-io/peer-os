package org.safehaus.kiskis.mgmt.pig.services;

import org.safehaus.kiskis.mgmt.api.pig.Pig;

/**
 * Created by bahadyr on 5/6/14.
 */

public class RestServiceImpl implements RestService {

    private Pig pigManager;

    public Pig getPigManager() {
        return pigManager;
    }

    public void setPigManager(Pig pigManager) {
        this.pigManager = pigManager;
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
