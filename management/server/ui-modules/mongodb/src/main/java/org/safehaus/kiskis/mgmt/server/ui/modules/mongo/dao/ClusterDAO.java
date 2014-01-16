/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.dao;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.shared.protocol.MongoClusterInfo;
import org.safehaus.kiskis.mgmt.shared.protocol.ServiceLocator;
import org.safehaus.kiskis.mgmt.shared.protocol.api.DbManager;

/**
 *
 * @author dilshat
 */
public class ClusterDAO {

    private static final Logger LOG = Logger.getLogger(ClusterDAO.class.getName());

    private static final DbManager dbManager;

    static {
        dbManager = ServiceLocator.getService(DbManager.class);
    }

    public static boolean saveMongoClusterInfo(MongoClusterInfo clusterInfo) {
        try {
            String cql = String.format(
                    "insert into %s"
                    + "(%s, %s, %s, %s, %s) "
                    + "values (?, ?, ?, ?, ?)",
                    MongoClusterInfo.TABLE_NAME, MongoClusterInfo.CLUSTER_NAME,
                    MongoClusterInfo.REPLICA_SET_NAME, MongoClusterInfo.CONFIG_SERVERS_NAME,
                    MongoClusterInfo.ROUTERS_NAME, MongoClusterInfo.DATA_NODES_NAME);
            dbManager.executeUpdate(cql, clusterInfo.getClusterName(),
                    clusterInfo.getReplicaSetName(), clusterInfo.getConfigServers(),
                    clusterInfo.getRouters(), clusterInfo.getDataNodes());

            return true;
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in saveMongoClusterInfo", ex);
        }
        return false;
    }

    public static List<MongoClusterInfo> getMongoClustersInfo() {
        List<MongoClusterInfo> list = new ArrayList<MongoClusterInfo>();
        try {
            String cql = String.format("select * from %s", MongoClusterInfo.TABLE_NAME);
            ResultSet rs = dbManager.executeQuery(cql);
            for (Row row : rs) {
                MongoClusterInfo mongoClusterInfo = new MongoClusterInfo();
                mongoClusterInfo.setClusterName(row.getString(MongoClusterInfo.CLUSTER_NAME));
                mongoClusterInfo.setReplicaSetName(row.getString(MongoClusterInfo.REPLICA_SET_NAME));
                mongoClusterInfo.setConfigServers(row.getList(MongoClusterInfo.CONFIG_SERVERS_NAME, UUID.class));
                mongoClusterInfo.setRouters(row.getList(MongoClusterInfo.ROUTERS_NAME, UUID.class));
                mongoClusterInfo.setDataNodes(row.getList(MongoClusterInfo.DATA_NODES_NAME, UUID.class));
                list.add(mongoClusterInfo);
            }

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getMongoClustersInfo", ex);
        }
        return list;
    }

    public static MongoClusterInfo getMongoClusterInfo(String clusterName) {
        MongoClusterInfo mongoClusterInfo = null;
        try {
            String cql = String.format(
                    "select * from %s where %s = ? limit 1 allow filtering",
                    MongoClusterInfo.TABLE_NAME, MongoClusterInfo.CLUSTER_NAME);
            ResultSet rs = dbManager.executeQuery(cql, clusterName);
            Row row = rs.one();
            if (row != null) {
                mongoClusterInfo = new MongoClusterInfo();
                mongoClusterInfo.setClusterName(row.getString(MongoClusterInfo.CLUSTER_NAME));
                mongoClusterInfo.setReplicaSetName(row.getString(MongoClusterInfo.REPLICA_SET_NAME));
                mongoClusterInfo.setConfigServers(row.getList(MongoClusterInfo.CONFIG_SERVERS_NAME, UUID.class));
                mongoClusterInfo.setRouters(row.getList(MongoClusterInfo.ROUTERS_NAME, UUID.class));
                mongoClusterInfo.setDataNodes(row.getList(MongoClusterInfo.DATA_NODES_NAME, UUID.class));
            }

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getMongoClusterInfo", ex);
        }
        return mongoClusterInfo;
    }

    public static boolean deleteMongoClusterInfo(String clusterName) {
        try {
            String cql = String.format("delete from %s where %s = ?",
                    MongoClusterInfo.TABLE_NAME, MongoClusterInfo.CLUSTER_NAME);
            dbManager.executeUpdate(cql, clusterName);
            return true;
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in deleteMongoClusterInfo", ex);
        }
        return false;
    }

}
