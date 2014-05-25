package org.safehaus.subutai.sqoop.services;

import org.safehaus.subutai.api.sqoop.Sqoop;

/**
 * Created by bahadyr on 5/6/14.
 */

public class RestServiceImpl implements RestService {

    private Sqoop sqoopManager;

    public Sqoop getSqoopManager() {
        return sqoopManager;
    }

    public void setSqoopManager(Sqoop sqoopManager) {
        this.sqoopManager = sqoopManager;
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
