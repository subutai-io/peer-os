/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.hbase;

//import org.safehaus.kiskis.mgmt.server.ui.modules.hbase.wizard.HBaseConfig;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
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

    public static boolean saveClusterInfo(HBaseConfig cluster) {
        try {

            byte[] data = serialize(cluster);

            String cql = "insert into hbase_info (uid, info) values (?,?)";
            dbManager.executeUpdate(cql, cluster.getUuid(), ByteBuffer.wrap(data));

        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Error in saveHBaseClusterInfo", ex);
            return false;
        }
        return true;
    }

    public static List<HBaseConfig> getClusterInfo() {
        List<HBaseConfig> list = new ArrayList<HBaseConfig>();
        try {
            String cql = "select * from hbase_info";
            ResultSet results = dbManager.executeQuery(cql);
            for (Row row : results) {

                ByteBuffer data = row.getBytes("info");

                byte[] result = new byte[data.remaining()];
                data.get(result);
                HBaseConfig config = (HBaseConfig) deserialize(result);
                list.add(config);
            }
        } catch (ClassNotFoundException ex) {
            LOG.log(Level.SEVERE, "Error in getHBaseClusterInfo", ex);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Error in getHBaseClusterInfo", ex);
        }
        return list;
    }

    public static boolean deleteClusterInfo(UUID uuid) {
        try {
            String cql = "delete from hbase_info where uid = ?";
            dbManager.executeUpdate(cql, uuid);
            return true;
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in deleteHBaseClusterInfo(name)", ex);
        }
        return false;
    }

    private static byte[] serialize(Object o) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(o);
        oos.flush();
        oos.close();
        return baos.toByteArray();
    }

    private static Object deserialize(byte[] bytes) throws ClassNotFoundException, IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object o = ois.readObject();
        ois.close();
        return o;
    }

}
