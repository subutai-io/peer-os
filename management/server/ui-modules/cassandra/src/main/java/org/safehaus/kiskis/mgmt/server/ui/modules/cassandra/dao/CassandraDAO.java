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
import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.CassandraModule;

/**
 *
 * @author dilshat
 */
public class CassandraDAO {

    private static final Logger LOG = Logger.getLogger(CassandraDAO.class.getName());

    public static boolean saveCassandraClusterInfo(CassandraClusterInfo cluster) {
        try {

            CassandraModule.getDbManager().saveInfo(CassandraModule.MODULE_NAME, cluster.getUuid().toString(), cluster);

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in saveCassandraClusterInfo", ex);
            return false;
        }
        return true;
    }

    public static List<CassandraClusterInfo> getCassandraClusterInfo() {
        List<CassandraClusterInfo> list = new ArrayList<CassandraClusterInfo>();
        try {

            return CassandraModule.getDbManager().getInfo(CassandraModule.MODULE_NAME, CassandraClusterInfo.class);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getCassandraClusterInfo", ex);
        }
        return list;
    }

    public static CassandraClusterInfo getCassandraClusterInfoByUUID(UUID uuid) {
        CassandraClusterInfo cassandraClusterInfo = null;
        try {

            return CassandraModule.getDbManager().getInfo(CassandraModule.MODULE_NAME, uuid.toString(), CassandraClusterInfo.class);

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getCassandraClusterInfo(uid)", ex);
        }
        return cassandraClusterInfo;
    }

    public static boolean deleteCassandraClusterInfo(UUID uuid) {
        try {
            CassandraModule.getDbManager().deleteInfo(CassandraModule.MODULE_NAME, uuid.toString());
            return true;
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in deleteCassandraClusterInfo(name)", ex);
        }
        return false;
    }

}
