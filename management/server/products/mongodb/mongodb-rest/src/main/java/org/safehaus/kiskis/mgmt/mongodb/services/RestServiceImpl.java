package org.safehaus.kiskis.mgmt.mongodb.services;

import org.safehaus.kiskis.mgmt.api.mongodb.Mongo;

/**
 * Created by bahadyr on 5/6/14.
 */

public class RestServiceImpl implements RestService {

    private Mongo mongoManager;

    public Mongo getMongoManager() {
        return mongoManager;
    }

    public void setMongoManager(Mongo mongoManager) {
        this.mongoManager = mongoManager;
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
