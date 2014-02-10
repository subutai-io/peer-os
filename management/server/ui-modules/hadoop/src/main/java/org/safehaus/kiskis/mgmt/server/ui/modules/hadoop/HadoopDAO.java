/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.hadoop;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.HadoopClusterInfo;
import org.safehaus.kiskis.mgmt.shared.protocol.ServiceLocator;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AgentManager;
import org.safehaus.kiskis.mgmt.shared.protocol.api.DbManager;

/**
 *
 * @author dilshat
 */
public class HadoopDAO {

    private static final Logger LOG = Logger.getLogger(HadoopDAO.class.getName());
    private static final DbManager dbManager;
    private static final AgentManager agentManager;

    static {
        dbManager = ServiceLocator.getService(DbManager.class);
        agentManager = ServiceLocator.getService(AgentManager.class);
    }

    public static boolean saveHadoopClusterInfo(HadoopClusterInfo cluster) {
        try {
            String cql = "insert into hadoop_cluster_info (uid, cluster_name, name_node, secondary_name_node, "
                    + "job_tracker, replication_factor, data_nodes, task_trackers, ip_mask) "
                    + "values (?,?,?,?,?,?,?,?,?)";
            dbManager.executeUpdate(cql, cluster.getUid(),
                    cluster.getClusterName(), cluster.getNameNode(),
                    cluster.getSecondaryNameNode(), cluster.getJobTracker(),
                    cluster.getReplicationFactor(), cluster.getDataNodes(),
                    cluster.getTaskTrackers(), cluster.getIpMask());
            return true;

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in saveHadoopClusterInfo", ex);
        }
        return false;
    }

    public static List<HadoopClusterInfo> getHadoopClusterInfo() {
        List<HadoopClusterInfo> list = new ArrayList<HadoopClusterInfo>();
        try {
            String cql = "select * from hadoop_cluster_info";
            ResultSet rs = dbManager.executeQuery(cql);
            for (Row row : rs) {
                HadoopClusterInfo cd = new HadoopClusterInfo();
                cd.setUid(row.getUUID("uid"));
                cd.setClusterName(row.getString("cluster_name"));
                cd.setNameNode(row.getUUID("name_node"));
                cd.setSecondaryNameNode(row.getUUID("secondary_name_node"));
                cd.setJobTracker(row.getUUID("job_tracker"));
                cd.setReplicationFactor(row.getInt("replication_factor"));
                cd.setDataNodes(row.getList("data_nodes", UUID.class));
                cd.setTaskTrackers(row.getList("task_trackers", UUID.class));
                cd.setIpMask(row.getString("ip_mask"));
                list.add(cd);
            }

            for (HadoopClusterInfo item : list) {
                Agent master = agentManager.getAgentByUUIDFromDB(item.getNameNode());
                if (master.getUuid() == null) {
                    deleteHadoopClusterInfo(item.getUid());
                    list.remove(item);
                }
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getHadoopClusterInfo", ex);
        }
        return list;
    }

    public static HadoopClusterInfo getHadoopClusterInfo(String clusterName) {
        HadoopClusterInfo hadoopClusterInfo = null;
        try {
            String cql = "select * from hadoop_cluster_info where cluster_name = ? limit 1 allow filtering";
            ResultSet rs = dbManager.executeQuery(cql, clusterName.trim());
            Row row = rs.one();
            if (row != null) {
                hadoopClusterInfo = new HadoopClusterInfo();
                hadoopClusterInfo.setUid(row.getUUID("uid"));
                hadoopClusterInfo.setClusterName(row.getString("cluster_name"));
                hadoopClusterInfo.setNameNode(row.getUUID("name_node"));
                hadoopClusterInfo.setSecondaryNameNode(row.getUUID("secondary_name_node"));
                hadoopClusterInfo.setJobTracker(row.getUUID("job_tracker"));
                hadoopClusterInfo.setReplicationFactor(row.getInt("replication_factor"));
                hadoopClusterInfo.setDataNodes(row.getList("data_nodes", UUID.class));
                hadoopClusterInfo.setTaskTrackers(row.getList("task_trackers", UUID.class));
                hadoopClusterInfo.setIpMask(row.getString("ip_mask"));
            }

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getHadoopClusterInfo(name)", ex);
        }
        return hadoopClusterInfo;
    }

    public static boolean deleteHadoopClusterInfo(UUID uuid) {
        try {
            String cql = "delete from hadoop_cluster_info where uid = ?";
            dbManager.executeQuery(cql, uuid);
            return true;
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in deleteHadoopClusterInfo(uuid)", ex);
        }
        return false;
    }
}
