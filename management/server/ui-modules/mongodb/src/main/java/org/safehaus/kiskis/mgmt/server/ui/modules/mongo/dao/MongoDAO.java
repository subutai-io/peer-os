/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.MongoModule;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.entity.MongoClusterInfo;
import org.safehaus.kiskis.mgmt.shared.protocol.ServiceLocator;
import org.safehaus.kiskis.mgmt.api.dbmanager.DbManager;

/**
 *
 * @author dilshat
 */
public class MongoDAO {

    private static final Logger LOG = Logger.getLogger(MongoDAO.class.getName());

    private static final DbManager dbManager;

    static {
        dbManager = ServiceLocator.getService(DbManager.class);
    }

    public static boolean saveMongoClusterInfo(MongoClusterInfo clusterInfo) {
        try {

            dbManager.saveInfo(MongoModule.MODULE_NAME, clusterInfo.getClusterName(), clusterInfo);

            return true;
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in saveMongoClusterInfo", ex);
        }
        return false;
    }

    public static List<MongoClusterInfo> getMongoClustersInfo() {
        try {

            return dbManager.getInfo(MongoModule.MODULE_NAME, MongoClusterInfo.class);

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getMongoClustersInfo", ex);
        }
        return new ArrayList<MongoClusterInfo>();
    }

    public static MongoClusterInfo getMongoClusterInfo(String clusterName) {
        MongoClusterInfo mongoClusterInfo = null;
        try {
            mongoClusterInfo = dbManager.getInfo(MongoModule.MODULE_NAME, clusterName, MongoClusterInfo.class);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getMongoClusterInfo", ex);
        }
        return mongoClusterInfo;
    }

    public static boolean deleteMongoClusterInfo(String clusterName) {
        try {

            dbManager.deleteInfo(MongoModule.MODULE_NAME, clusterName);
            return true;
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in deleteMongoClusterInfo", ex);
        }
        return false;
    }

}
