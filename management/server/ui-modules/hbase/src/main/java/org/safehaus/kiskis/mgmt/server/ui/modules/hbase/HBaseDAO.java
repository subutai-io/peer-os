/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.hbase;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.SerializationUtils;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.server.ui.modules.hbase.wizard.HBaseClusterInfo;
import org.safehaus.kiskis.mgmt.shared.protocol.ServiceLocator;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AgentManager;
import org.safehaus.kiskis.mgmt.shared.protocol.api.DbManager;

/**
 *
 * @author dilshat
 */
public class HBaseDAO {

    private static final Logger LOG = Logger.getLogger(HBaseDAO.class.getName());
    private static final DbManager dbManager;
    private static final AgentManager agentManager;

    static {
        dbManager = ServiceLocator.getService(DbManager.class);
        agentManager = ServiceLocator.getService(AgentManager.class);
    }

    public static boolean saveHBaseClusterInfo(HBaseClusterInfo cluster) {
        try {
            String cql = "insert into hbase_cluster_info (uid, master, region, quorum, "
                    + "bmasters, domainname) "
                    + "values (?,?,?,?,?,?)";
            dbManager.executeUpdate(cql, cluster.getUuid(), cluster.getMaster(), cluster.getRegion(),
                    cluster.getQuorum(), cluster.getBmasters(), cluster.getDomainName());

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in saveHBaseClusterInfo", ex);
            return false;
        }
        return true;
    }

    public static boolean saveClusterInfo(HBaseConfig cluster) {
        try {

            byte[] data = SerializationUtils.serialize(cluster);

            String cql = "insert into hbase_info (uid, info) values (?,?)";
            dbManager.executeUpdate(cql, cluster.getUuid(), ByteBuffer.wrap(data));

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in saveHBaseClusterInfo", ex);
            return false;
        }
        return true;
    }

    public static List<HBaseClusterInfo> getHBaseClusterInfo() {
        List<HBaseClusterInfo> list = new ArrayList<HBaseClusterInfo>();
        try {
            String cql = "select * from hbase_cluster_info";
            ResultSet rs = dbManager.executeQuery(cql);
            for (Row row : rs) {
                HBaseClusterInfo cd = new HBaseClusterInfo();
                cd.setUuid(row.getUUID("uid"));
                cd.setDomainName(row.getString("domainname"));
                cd.setMaster(row.getSet("master", UUID.class));
                cd.setRegion(row.getSet("region", UUID.class));
                cd.setQuorum(row.getSet("quorum", UUID.class));
                cd.setBmasters(row.getSet("bmasters", UUID.class));
                list.add(cd);
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getHBaseClusterInfo", ex);
        }
        return list;
    }

    public static List<HBaseConfig> getClusterInfo() {
        List<HBaseConfig> list = new ArrayList<HBaseConfig>();
        try {
            String cql = "select * from hbase_info";
            ResultSet results = dbManager.executeQuery(cql);
            for (Row row : results) {

                ByteBuffer data = row.getBytes("info");
                byte[] result = new byte[data.remaining()];
                ByteBuffer newdata = data.get(data.array(), 0, result.length);
                System.out.println(newdata.array());
                Object config = (Object) SerializationUtils.deserialize(newdata.array());
                System.out.println(config);
                list.add((HBaseConfig)config);
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getHBaseClusterInfo", ex);
        }
        return list;
    }

//    public byte[] readFromTable(String key) {
//        String q1 = "SELECT * FROM test_serialization.test_table WHERE id = '" + key + "';";
//
//        ResultSet results = dbManager.executeQuery(q1);
//        for (Row row : results) {
//            ByteBuffer data = row.getBytes("data");
//            return data.array();
//        }
//        return null;
//    }

//    public static HBaseClusterInfo getHBaseClusterInfo(String clusterName) {
//        HBaseClusterInfo info = null;
//        try {
//            String cql = "select * from cassandra_cluster_info where name = ? limit 1 allow filtering";
//            ResultSet rs = dbManager.executeQuery(cql, clusterName);
//            Row row = rs.one();
//            if (row != null) {
//                info = new HBaseClusterInfo();
//                info.setUuid(row.getUUID("uid"));
//                info.setDomainName(row.getString("domainname"));
//                info.setMaster(row.getSet("master", UUID.class));
//                info.setRegion(row.getSet("region", UUID.class));
//                info.setQuorum(row.getSet("quorum", UUID.class));
//                info.setBmasters(row.getSet("bmasters", UUID.class));
//            }
//
//        } catch (Exception ex) {
//            LOG.log(Level.SEVERE, "Error in getHBaseClusterInfo(name)", ex);
//        }
//        return info;
//    }

    public static HBaseClusterInfo getHBaseClusterInfoByUUID(UUID uuid) {
        HBaseClusterInfo info = null;
        try {
            String cql = "select * from cassandra_cluster_info where uid = ? limit 1 allow filtering";
            ResultSet rs = dbManager.executeQuery(cql, uuid);
            Row row = rs.one();
            if (row != null) {
                info = new HBaseClusterInfo();
                info.setUuid(row.getUUID("uid"));
                info.setDomainName(row.getString("domainname"));
                info.setMaster(row.getSet("master", UUID.class));
                info.setRegion(row.getSet("region", UUID.class));
                info.setQuorum(row.getSet("quorum", UUID.class));
                info.setBmasters(row.getSet("bmasters", UUID.class));
            }

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getHBaseClusterInfo(uid)", ex);
        }
        return info;
    }

    public static boolean deleteHBaseClusterInfo(UUID uuid) {
        try {
            String cql = "delete from hbase_cluster_info where uid = ?";
            dbManager.executeUpdate(cql, uuid);
            return true;
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in deleteHBaseClusterInfo(name)", ex);
        }
        return false;
    }

    public static Set<Agent> getAgents(Set<UUID> uuids) {
        Set<Agent> list = new HashSet<Agent>();
        for (UUID uuid : uuids) {
            Agent agent = agentManager.getAgentByUUIDFromDB(uuid);
            list.add(agent);
        }
        return list;
    }

}
