package org.safehaus.kiskis.mgmt.server.ui.modules.oozie;

import org.safehaus.kiskis.mgmt.api.dbmanager.DbManager;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.HadoopClusterInfo;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dilshat
 */
public class OozieDAO {

    private DbManager dbManager;

    public OozieDAO(DbManager dbManager) {
        this.dbManager = dbManager;
    }

    private static final Logger LOG = Logger.getLogger(OozieDAO.class.getName());

    public boolean saveClusterInfo(OozieConfig cluster) {
        try {

            dbManager.saveInfo(OozieModule.MODULE_NAME, cluster.getUuid().toString(), cluster);

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in saveClusterInfo", ex);
            return false;
        }
        return true;
    }

    public  List<OozieConfig> getClusterInfo() {
        List<OozieConfig> list = new ArrayList<OozieConfig>();
        try {
            dbManager.getInfo(OozieModule.MODULE_NAME, OozieConfig.class);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getClusterInfo", ex);
        }
        return list;
    }

    public  boolean deleteClusterInfo(UUID uuid) {
        try {
            dbManager.deleteInfo(OozieModule.MODULE_NAME, uuid.toString());
            return true;
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in deleteClusterInfo", ex);
        }
        return false;
    }

    public  Set<Agent> getAgents(Set<UUID> uuids) {
        Set<Agent> list = new HashSet<Agent>();
        for (UUID uuid : uuids) {
            Agent agent = OozieModule.getAgentManager().getAgentByUUIDFromDB(uuid);
            list.add(agent);
        }
        return list;
    }

    public HadoopClusterInfo getHadoopClusterInfo(String clusterName) {
        HadoopClusterInfo hadoopClusterInfo = null;
        try {
            dbManager.getInfo(HadoopClusterInfo.SOURCE, clusterName, HadoopClusterInfo.class);

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getHadoopClusterInfo(name)", ex);
        }
        return hadoopClusterInfo;
    }
}
