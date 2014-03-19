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
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.Config;

/**
 *
 * @author dilshat
 */
public class MongoDAO {

    private static final Logger LOG = Logger.getLogger(MongoDAO.class.getName());

    public static boolean saveMongoClusterInfo(Config config) {
        try {

            MongoModule.getDbManager().saveInfo(MongoModule.MODULE_NAME, config.getClusterName(), config);

            return true;
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in saveMongoClusterInfo", ex);
        }
        return false;
    }

    public static List<Config> getMongoClustersInfo() {
        try {

            return MongoModule.getDbManager().getInfo(MongoModule.MODULE_NAME, Config.class);

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getMongoClustersInfo", ex);
        }
        return new ArrayList<Config>();
    }

    public static Config getMongoClusterInfo(String clusterName) {
        Config mongoClusterInfo = null;
        try {
            mongoClusterInfo = MongoModule.getDbManager().getInfo(MongoModule.MODULE_NAME, clusterName, Config.class);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getMongoClusterInfo", ex);
        }
        return mongoClusterInfo;
    }

    public static boolean deleteMongoClusterInfo(String clusterName) {
        try {

            MongoModule.getDbManager().deleteInfo(MongoModule.MODULE_NAME, clusterName);
            return true;
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in deleteMongoClusterInfo", ex);
        }
        return false;
    }

}
