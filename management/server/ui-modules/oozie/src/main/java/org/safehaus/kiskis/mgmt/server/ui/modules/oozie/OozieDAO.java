package org.safehaus.kiskis.mgmt.server.ui.modules.oozie;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.api.dbmanager.DbManager;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.HadoopClusterInfo;

/**
 *
 * @author dilshat
 */
public class OozieDAO {

    private static final Logger LOG = Logger.getLogger(OozieDAO.class.getName());
    private final DbManager dbManager;

    public OozieDAO(DbManager dbManager) {
        this.dbManager = dbManager;
    }

    public boolean saveClusterInfo(OozieConfig cluster) {
        try {
            dbManager.saveInfo(OozieModule.MODULE_NAME, cluster.getUuid().toString(), cluster);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in saveClusterInfo", ex);
            return false;
        }
        return true;
    }

    public List<OozieConfig> getClusterInfo() {
        List<OozieConfig> list = new ArrayList<OozieConfig>();
        try {
            list = dbManager.getInfo(OozieModule.MODULE_NAME, OozieConfig.class);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getClusterInfo", ex);
            return null;
        }
        return list;
    }

    public boolean deleteClusterInfo(UUID uuid) {
        try {
            dbManager.deleteInfo(OozieModule.MODULE_NAME, uuid.toString());
            return true;
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in deleteClusterInfo", ex);
        }
        return false;
    }

    public Set<Agent> getAgents(Set<UUID> uuids) {
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
            return dbManager.getInfo(HadoopClusterInfo.SOURCE, clusterName, HadoopClusterInfo.class);

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getHadoopClusterInfo(name)", ex);
        }
        return hadoopClusterInfo;
    }
}
