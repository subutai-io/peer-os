package org.safehaus.kiskis.mgmt.hive.services;

import org.safehaus.kiskis.mgmt.api.hive.Hive;

/**
 * Created by bahadyr on 5/6/14.
 */

public class RestServiceImpl implements RestService {

    private Hive hiveManager;

    public Hive getHiveManager() {
        return hiveManager;
    }

    public void setHiveManager(Hive hiveManager) {
        this.hiveManager = hiveManager;
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
