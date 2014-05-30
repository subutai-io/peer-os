package org.safehaus.subutai.zookeeper.services;

import org.safehaus.subutai.api.zookeeper.Zookeeper;

/**
 * Created by bahadyr on 5/6/14.
 */

public class RestServiceImpl implements RestService {

    private Zookeeper zookeeperManager;

    public Zookeeper getZookeeperManager() {
        return zookeeperManager;
    }

    public void setZookeeperManager(Zookeeper zookeeperManager) {
        this.zookeeperManager = zookeeperManager;
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
