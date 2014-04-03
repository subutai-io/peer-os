/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.ui.spark;

import com.vaadin.ui.Component;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.dbmanager.DbManager;
import org.safehaus.kiskis.mgmt.api.spark.Spark;
import org.safehaus.kiskis.mgmt.api.tracker.Tracker;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;

/**
 *
 * @author dilshat
 */
public class SparkUI implements Module {

    public static final String MODULE_NAME = "Spark";
    private static Spark sparkManager;
    private static AgentManager agentManager;
    private static Tracker tracker;
    private static DbManager dbManager;
    private static ExecutorService executor;

    public static Tracker getTracker() {
        return tracker;
    }

    public static Spark getSparkManager() {
        return sparkManager;
    }

    public void setSparkManager(Spark sparkManager) {
        SparkUI.sparkManager = sparkManager;
    }

    public void setTracker(Tracker tracker) {
        SparkUI.tracker = tracker;
    }

    public static DbManager getDbManager() {
        return dbManager;
    }

    public void setDbManager(DbManager dbManager) {
        SparkUI.dbManager = dbManager;
    }

    public static ExecutorService getExecutor() {
        return executor;
    }

    public static AgentManager getAgentManager() {
        return agentManager;
    }

    public void setAgentManager(AgentManager agentManager) {
        SparkUI.agentManager = agentManager;
    }

    public void init() {
        executor = Executors.newCachedThreadPool();
    }

    public void destroy() {
        sparkManager = null;
        agentManager = null;
        dbManager = null;
        tracker = null;
        executor.shutdown();
    }

    public String getName() {
        return MODULE_NAME;
    }

    public Component createComponent() {
        return new SparkForm();
    }

}
