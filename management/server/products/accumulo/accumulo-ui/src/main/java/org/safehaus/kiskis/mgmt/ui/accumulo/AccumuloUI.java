/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.ui.accumulo;

import com.vaadin.ui.Component;
import org.safehaus.kiskis.mgmt.api.accumulo.Accumulo;
import org.safehaus.kiskis.mgmt.api.accumulo.Config;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.hadoop.Hadoop;
import org.safehaus.kiskis.mgmt.api.tracker.Tracker;
import org.safehaus.kiskis.mgmt.api.zookeeper.Api;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author dilshat
 */
public class AccumuloUI implements Module {

    private static Accumulo accumuloManager;
    private static Hadoop hadoopManager;
    private static Api zookeeperManager;
    private static AgentManager agentManager;
    private static Tracker tracker;
    //    private static DbManager dbManager;
    private static ExecutorService executor;

    public static Api getZookeeperManager() {
        return zookeeperManager;
    }

    public static void setZookeeperManager(Api zookeeperManager) {
        AccumuloUI.zookeeperManager = zookeeperManager;
    }

    public static Hadoop getHadoopManager() {
        return hadoopManager;
    }

    public static void setHadoopManager(Hadoop hadoopManager) {
        AccumuloUI.hadoopManager = hadoopManager;
    }

//    public static DbManager getDbManager() {
//        return dbManager;
//    }
//
//    public void setDbManager(DbManager dbManager) {
//        AccumuloUI.dbManager = dbManager;
//    }

    public static Tracker getTracker() {
        return tracker;
    }

    public void setTracker(Tracker tracker) {
        AccumuloUI.tracker = tracker;
    }

    public static Accumulo getAccumuloManager() {
        return accumuloManager;
    }

    public void setAccumuloManager(Accumulo accumuloManager) {
        AccumuloUI.accumuloManager = accumuloManager;
    }

    public static ExecutorService getExecutor() {
        return executor;
    }

    public static AgentManager getAgentManager() {
        return agentManager;
    }

    public void setAgentManager(AgentManager agentManager) {
        AccumuloUI.agentManager = agentManager;
    }

    public void init() {
        executor = Executors.newCachedThreadPool();
    }

    public void destroy() {
        accumuloManager = null;
        agentManager = null;
        tracker = null;
//        dbManager = null;
        hadoopManager = null;
        zookeeperManager = null;
        executor.shutdown();
    }

    public String getName() {
        return Config.PRODUCT_KEY;
    }

    public Component createComponent() {
        return new AccumuloForm();
    }

}
