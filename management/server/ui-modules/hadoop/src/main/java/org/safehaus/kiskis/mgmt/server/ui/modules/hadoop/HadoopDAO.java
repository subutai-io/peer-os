/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.hadoop;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.api.dbmanager.DbManager;

import org.safehaus.kiskis.mgmt.shared.protocol.HadoopClusterInfo;
import org.safehaus.kiskis.mgmt.shared.protocol.ServiceLocator;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;

/**
 * @author dilshat
 */
public class HadoopDAO {

    private static final Logger LOG = Logger.getLogger(HadoopDAO.class.getName());
    private static final DbManager dbManager;

    static {
        dbManager = ServiceLocator.getService(DbManager.class);
    }

    private static Object deserialize(byte[] bytes) throws ClassNotFoundException, IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object o = ois.readObject();
        ois.close();
        return o;
    }

    public static boolean saveHadoopClusterInfo(HadoopClusterInfo cluster) {
        try {
            byte[] data = Util.serialize(cluster);

            String cql = "insert into hadoop_cluster_info (uid, cluster_name, info) values (?,?,?)";
            dbManager.executeUpdate(cql, cluster.getUuid(), cluster.getClusterName(), ByteBuffer.wrap(data));

            return true;
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Error in saveHadoopClusterInfo", ex);
            return false;
        }
    }

    public static List<HadoopClusterInfo> getHadoopClusterInfo() {
        List<HadoopClusterInfo> list = new ArrayList<HadoopClusterInfo>();
        try {
            String cql = "select * from hadoop_cluster_info";
            ResultSet rs = dbManager.executeQuery(cql);
            for (Row row : rs) {
                ByteBuffer data = row.getBytes("info");
                byte[] result = new byte[data.remaining()];
                data.get(result);

                HadoopClusterInfo cd = (HadoopClusterInfo) deserialize(result);
                list.add(cd);
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
                ByteBuffer data = row.getBytes("info");
                byte[] result = new byte[data.remaining()];
                data.get(result);

                hadoopClusterInfo = (HadoopClusterInfo) deserialize(result);
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
