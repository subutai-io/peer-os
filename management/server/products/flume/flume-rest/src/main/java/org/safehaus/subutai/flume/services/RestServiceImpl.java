package org.safehaus.subutai.flume.services;

import org.safehaus.subutai.api.flume.Flume;

/**
 * Created by bahadyr on 5/6/14.
 */

public class RestServiceImpl implements RestService {

    private Flume flumeManager;

    public Flume getFlumeManager() {
        return flumeManager;
    }

    public void setFlumeManager(Flume flumeManager) {
        this.flumeManager = flumeManager;
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
