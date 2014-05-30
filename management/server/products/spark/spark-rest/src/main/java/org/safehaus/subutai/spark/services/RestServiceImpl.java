package org.safehaus.subutai.spark.services;

import org.safehaus.subutai.api.spark.Spark;

/**
 * Created by bahadyr on 5/6/14.
 */

public class RestServiceImpl implements RestService {

    private Spark sparkManager;

    public Spark getSparkManager() {
        return sparkManager;
    }

    public void setSparkManager(Spark sparkManager) {
        this.sparkManager = sparkManager;
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
