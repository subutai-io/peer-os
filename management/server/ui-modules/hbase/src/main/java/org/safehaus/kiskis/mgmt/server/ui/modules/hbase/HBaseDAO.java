/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.hbase;

import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.HadoopClusterInfo;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dilshat
 */
public class HBaseDAO {

    private static final Logger LOG = Logger.getLogger(HBaseDAO.class.getName());

    public static boolean saveClusterInfo(HBaseConfig cluster) {
        try {
            HBaseModule.getDbManager().saveInfo(HBaseModule.MODULE_NAME, cluster.getUuid().toString(), cluster);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in saveHBaseClusterInfo", ex);
            return false;
        }
        return true;
    }

    public static List<HBaseConfig> getClusterInfo() {
        List<HBaseConfig> list = new ArrayList<HBaseConfig>();
        try {
            return HBaseModule.getDbManager().getInfo(HBaseModule.MODULE_NAME, HBaseConfig.class);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getHBaseClusterInfo", ex);
        }
        return list;
    }

    public static boolean deleteClusterInfo(UUID uuid) {
        try {
            HBaseModule.getDbManager().deleteInfo(HBaseModule.MODULE_NAME, uuid.toString());
            return true;
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in deleteHBaseClusterInfo(name)", ex);
        }
        return false;
    }

    public static Set<Agent> getAgents(Set<UUID> uuids) {
        Set<Agent> list = new HashSet<Agent>();
        for (UUID uuid : uuids) {
            Agent agent = HBaseModule.getAgentManager().getAgentByUUIDFromDB(uuid);
            list.add(agent);
        }
        return list;
    }

    public static HadoopClusterInfo getHadoopClusterInfo(String clusterName) {
        HadoopClusterInfo hadoopClusterInfo = null;
        try {
            return HBaseModule.getDbManager().getInfo(HadoopClusterInfo.SOURCE, clusterName, HadoopClusterInfo.class);

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getHadoopClusterInfo(name)", ex);
        }
        return hadoopClusterInfo;
    }

}
