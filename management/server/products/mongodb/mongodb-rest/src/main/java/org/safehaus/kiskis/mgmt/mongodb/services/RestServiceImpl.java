package org.safehaus.kiskis.mgmt.mongodb.services;

import org.safehaus.kiskis.mgmt.api.mongodb.Mongo;

/**
 * Created by bahadyr on 5/6/14.
 */

public class RestServiceImpl implements RestService {

    private Mongo mongodbManager;

    public Mongo getMongodbManager() {
        return mongodbManager;
    }

    public void setMongodbManager(Mongo mongodbManager) {
        this.mongodbManager = mongodbManager;
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
