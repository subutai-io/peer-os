package org.safehaus.kiskis.mgmt.server.ui.modules.oozie;

import org.safehaus.kiskis.mgmt.api.dbmanager.DbManager;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dilshat
 */
public class OozieDAO {

    private final DbManager dbManager;

    public OozieDAO(DbManager dbManager) {
        this.dbManager = dbManager;
    }

    private static final Logger LOG = Logger.getLogger(OozieDAO.class.getName());

    public boolean saveInfo(OozieConfig cluster) {
        LOG.log(Level.INFO, "dbmanager state 1 {0}", dbManager.toString());
        try {
            dbManager.saveInfo(OozieModule.MODULE_NAME, cluster.getUuid().toString(), cluster);
        } catch (Exception ex) {
            LOG.log(Level.INFO, "Error in saveClusterInfo", ex);
            return false;
        }
        return true;
    }

    public static List<OozieConfig> getClusterInfo() {
        try {
            return OozieModule.dbManager.getInfo(OozieModule.MODULE_NAME, OozieConfig.class);
        } catch (Exception ex) {
            LOG.log(Level.INFO, "Error in getClusterInfo", ex);
            return null;
        }
    }

    public static boolean deleteClusterInfo(UUID uuid) {
        try {
            OozieModule.dbManager.deleteInfo(OozieModule.MODULE_NAME, uuid.toString());
            return true;
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in deleteClusterInfo", ex);
        }
        return false;
    }

}
