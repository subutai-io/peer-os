/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.ui.shark;

import com.vaadin.ui.Component;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.dbmanager.DbManager;
import org.safehaus.kiskis.mgmt.api.shark.Shark;
import org.safehaus.kiskis.mgmt.api.tracker.Tracker;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;

/**
 *
 * @author dilshat
 */
public class SharkUI implements Module {

    public static final String MODULE_NAME = "Shark";
    private static Shark sharkManager;
    private static AgentManager agentManager;
    private static Tracker tracker;
    private static DbManager dbManager;
    private static ExecutorService executor;

    public static Tracker getTracker() {
        return tracker;
    }

    public static Shark getSharkManager() {
        return sharkManager;
    }

    public void setSharkManager(Shark sharkManager) {
        SharkUI.sharkManager = sharkManager;
    }

    public void setTracker(Tracker tracker) {
        SharkUI.tracker = tracker;
    }

    public static DbManager getDbManager() {
        return dbManager;
    }

    public void setDbManager(DbManager dbManager) {
        SharkUI.dbManager = dbManager;
    }

    public static ExecutorService getExecutor() {
        return executor;
    }

    public static AgentManager getAgentManager() {
        return agentManager;
    }

    public void setAgentManager(AgentManager agentManager) {
        SharkUI.agentManager = agentManager;
    }

    public void init() {
        executor = Executors.newCachedThreadPool();
    }

    public void destroy() {
        sharkManager = null;
        agentManager = null;
        dbManager = null;
        tracker = null;
        executor.shutdown();
    }

    public String getName() {
        return MODULE_NAME;
    }

    public Component createComponent() {
        return new SharkForm();
    }

}
