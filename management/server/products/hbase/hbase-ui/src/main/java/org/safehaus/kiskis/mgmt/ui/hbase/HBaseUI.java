/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.ui.hbase;

import com.vaadin.ui.Component;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.dbmanager.DbManager;
import org.safehaus.kiskis.mgmt.api.hbase.HBase;
import org.safehaus.kiskis.mgmt.api.tracker.Tracker;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author dilshat
 */
public class HBaseUI implements Module {

    public static final String MODULE_NAME = "HBase";
    private static HBase hbaseManager;
    private static AgentManager agentManager;
    private static Tracker tracker;
    private static ExecutorService executor;
    private static DbManager dbManager;

    public static Tracker getTracker() {
        return tracker;
    }

    public void setTracker(Tracker tracker) {
        HBaseUI.tracker = tracker;
    }

    public static HBase getHbaseManager() {
        return hbaseManager;
    }

    public static ExecutorService getExecutor() {
        return executor;
    }

    public void setHbaseManager(HBase hbaseManager) {
        HBaseUI.hbaseManager = hbaseManager;
    }

    public static AgentManager getAgentManager() {
        return agentManager;
    }

    public void setAgentManager(AgentManager agentManager) {
        HBaseUI.agentManager = agentManager;
    }

    public void init() {
        executor = Executors.newCachedThreadPool();
    }

    public static DbManager getDbManager() {
        return dbManager;
    }

    public void setDbManager(DbManager dbManager) {
        HBaseUI.dbManager = dbManager;
    }

    public void destroy() {
        hbaseManager = null;
        agentManager = null;
        tracker = null;
        executor.shutdown();
    }

    public String getName() {
        return MODULE_NAME;
    }

    public Component createComponent() {
        return new HBaseForm();
    }

}
