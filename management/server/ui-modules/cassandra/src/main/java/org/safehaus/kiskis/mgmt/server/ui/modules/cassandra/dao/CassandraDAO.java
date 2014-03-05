/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.shared.protocol.ServiceLocator;
import org.safehaus.kiskis.mgmt.api.dbmanager.DbManager;
import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.CassandraModule;

/**
 *
 * @author dilshat
 */
public class CassandraDAO {

    private static final Logger LOG = Logger.getLogger(CassandraDAO.class.getName());
    private static final DbManager dbManager;

    static {
        dbManager = ServiceLocator.getService(DbManager.class);
    }

    public static boolean saveCassandraClusterInfo(CassandraClusterInfo cluster) {
        try {
//            String cql = "insert into cassandra_cluster_info (uid, name, commitlogdir, datadir, "
//                    + "nodes, savedcachedir, seeds, domainname) "
//                    + "values (?,?,?,?,?,?,?,?)";
//            dbManager.executeUpdate(cql, cluster.getUuid(), cluster.getName(),
//                    cluster.getCommitLogDir(), cluster.getDataDir(), cluster.getNodes(),
//                    cluster.getSavedCacheDir(), cluster.getSeeds(), cluster.getDomainName());

            dbManager.saveInfo(CassandraModule.MODULE_NAME, cluster.getUuid().toString(), cluster);

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in saveCassandraClusterInfo", ex);
            return false;
        }
        return true;
    }

    public static List<CassandraClusterInfo> getCassandraClusterInfo() {
        List<CassandraClusterInfo> list = new ArrayList<CassandraClusterInfo>();
        try {
//            String cql = "select * from cassandra_cluster_info";
//            ResultSet rs = dbManager.executeQuery(cql);
//            for (Row row : rs) {
//                CassandraClusterInfo cd = new CassandraClusterInfo();
//                cd.setUuid(row.getUUID("uid"));
//                cd.setName(row.getString("name"));
//                cd.setDataDir(row.getString("datadir"));
//                cd.setSavedCacheDir(row.getString("savedcachedir"));
//                cd.setCommitLogDir(row.getString("commitlogdir"));
//                cd.setDomainName(row.getString("domainname"));
//                cd.setNodes(row.getList("nodes", UUID.class));
//                cd.setSeeds(row.getList("seeds", UUID.class));
//                list.add(cd);
//            }

            return dbManager.getInfo(CassandraModule.MODULE_NAME, CassandraClusterInfo.class);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getCassandraClusterInfo", ex);
        }
        return list;
    }

//    public static CassandraClusterInfo getCassandraClusterInfo(String clusterName) {
//        CassandraClusterInfo cassandraClusterInfo = null;
//        try {
//            String cql = "select * from cassandra_cluster_info where name = ? limit 1 allow filtering";
//            ResultSet rs = dbManager.executeQuery(cql, clusterName);
//            Row row = rs.one();
//            if (row != null) {
//                cassandraClusterInfo = new CassandraClusterInfo();
//                cassandraClusterInfo.setUuid(row.getUUID("uid"));
//                cassandraClusterInfo.setName(row.getString("name"));
//                cassandraClusterInfo.setCommitLogDir(row.getString("commitlogdir"));
//                cassandraClusterInfo.setDataDir(row.getString("datadir"));
//                cassandraClusterInfo.setSavedCacheDir(row.getString("savedcachedir"));
//                cassandraClusterInfo.setNodes(row.getList("nodes", UUID.class));
//                cassandraClusterInfo.setSeeds(row.getList("seeds", UUID.class));
//            }
//
//        } catch (Exception ex) {
//            LOG.log(Level.SEVERE, "Error in getCassandraClusterInfo(name)", ex);
//        }
//        return cassandraClusterInfo;
//    }
    public static CassandraClusterInfo getCassandraClusterInfoByUUID(UUID uuid) {
        CassandraClusterInfo cassandraClusterInfo = null;
        try {
//            String cql = "select * from cassandra_cluster_info where uid = ? limit 1 allow filtering";
//            ResultSet rs = dbManager.executeQuery(cql, uuid);
//            Row row = rs.one();
//            if (row != null) {
//                cassandraClusterInfo = new CassandraClusterInfo();
//                cassandraClusterInfo.setUuid(row.getUUID("uid"));
//                cassandraClusterInfo.setName(row.getString("name"));
//                cassandraClusterInfo.setCommitLogDir(row.getString("commitlogdir"));
//                cassandraClusterInfo.setDataDir(row.getString("datadir"));
//                cassandraClusterInfo.setSavedCacheDir(row.getString("savedcachedir"));
//                cassandraClusterInfo.setDomainName(row.getString("domainname"));
//                cassandraClusterInfo.setNodes(row.getList("nodes", UUID.class));
//                cassandraClusterInfo.setSeeds(row.getList("seeds", UUID.class));
//            }

            return dbManager.getInfo(CassandraModule.MODULE_NAME, uuid.toString(), CassandraClusterInfo.class);

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getCassandraClusterInfo(uid)", ex);
        }
        return cassandraClusterInfo;
    }

    public static boolean deleteCassandraClusterInfo(UUID uuid) {
        try {
//            String cql = "delete from cassandra_cluster_info where uid = ?";
//            dbManager.executeUpdate(cql, uuid);
            dbManager.deleteInfo(CassandraModule.MODULE_NAME, uuid.toString());
            return true;
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in deleteCassandraClusterInfo(name)", ex);
        }
        return false;
    }

}
