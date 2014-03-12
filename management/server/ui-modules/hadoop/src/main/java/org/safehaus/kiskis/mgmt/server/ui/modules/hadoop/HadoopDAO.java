/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.hadoop;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.shared.protocol.HadoopClusterInfo;

/**
 * @author dilshat
 */
public class HadoopDAO {

    private static final Logger LOG = Logger.getLogger(HadoopDAO.class.getName());

    public static boolean saveHadoopClusterInfo(HadoopClusterInfo cluster) {
        try {
            HadoopModule.getDbManager().saveInfo(HadoopClusterInfo.SOURCE, cluster.getClusterName(), cluster);
            return true;
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in saveHadoopClusterInfo", ex);
            return false;
        }
    }

    public static List<HadoopClusterInfo> getHadoopClusterInfo() {
        List<HadoopClusterInfo> list = new ArrayList<HadoopClusterInfo>();
        try {

            return HadoopModule.getDbManager().getInfo(HadoopClusterInfo.SOURCE, HadoopClusterInfo.class);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getHadoopClusterInfo", ex);
        }
        return list;
    }

    public static HadoopClusterInfo getHadoopClusterInfo(String clusterName) {
        HadoopClusterInfo hadoopClusterInfo = null;
        try {
            return HadoopModule.getDbManager().getInfo(HadoopClusterInfo.SOURCE, clusterName, HadoopClusterInfo.class);

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getHadoopClusterInfo(name)", ex);
        }
        return hadoopClusterInfo;
    }

    public static boolean deleteHadoopClusterInfo(String clusterName) {
        try {
            HadoopModule.getDbManager().deleteInfo(HadoopClusterInfo.SOURCE, clusterName);
            return true;
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in deleteHadoopClusterInfo", ex);
        }
        return false;
    }
}
