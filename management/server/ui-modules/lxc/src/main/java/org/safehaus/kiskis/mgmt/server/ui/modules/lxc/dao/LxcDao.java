/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.lxc.dao;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.shared.protocol.ServiceLocator;
import org.safehaus.kiskis.mgmt.shared.protocol.api.DbManager;

/**
 *
 * @author dilshat
 */
public class LxcDao {

    private static final Logger LOG = Logger.getLogger(LxcDao.class.getName());

    private static final DbManager dbManager;

    static {
        dbManager = ServiceLocator.getService(DbManager.class);
    }

    public static boolean saveLxcCloneInfo(LxcCloneInfo cloneInfo) {
        try {
            String cql = String.format(
                    "insert into %s"
                    + "( %s, %s, %s, %s) "
                    + "values (?, ?, ?, ?)",
                    LxcCloneInfo.TABLE_NAME, LxcCloneInfo.TASK_UUID_NAME,
                    LxcCloneInfo.PHYSICAL_HOSTS_NAME, LxcCloneInfo.STATUS_NAME,
                    LxcCloneInfo.DATE_IN_NAME);
            dbManager.executeUpdate(cql, cloneInfo.getTaskUUID(),
                    cloneInfo.getPhysicalHosts(), cloneInfo.getCloneStatus().toString(),
                    cloneInfo.getDateIn());

            return true;
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in saveLxcCloneInfo", ex);
        }
        return false;
    }

    public static boolean truncateLxcInfos() {
        try {
            String cql = String.format(
                    "truncate %s",
                    LxcCloneInfo.TABLE_NAME);
            dbManager.executeUpdate(cql);

            return true;
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in truncateLxcInfo", ex);
        }
        return false;
    }

    public static List<LxcCloneInfo> getLxcCloneInfos() {
        List<LxcCloneInfo> list = new ArrayList<LxcCloneInfo>();
        try {
            String cql = String.format("select * from %s limit 10", LxcCloneInfo.TABLE_NAME);
            ResultSet rs = dbManager.executeQuery(cql);
            for (Row row : rs) {
                LxcCloneInfo cloneInfo
                        = new LxcCloneInfo(
                                row.getUUID(LxcCloneInfo.TASK_UUID_NAME),
                                row.getList(LxcCloneInfo.PHYSICAL_HOSTS_NAME, String.class),
                                row.getDate(LxcCloneInfo.DATE_IN_NAME),
                                LxcCloneStatus.valueOf(row.getString(LxcCloneInfo.STATUS_NAME)));
                list.add(cloneInfo);
            }

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getLxcCloneInfos", ex);
        }
        return list;
    }

}
