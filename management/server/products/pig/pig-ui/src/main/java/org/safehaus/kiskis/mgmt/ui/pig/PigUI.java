/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.ui.pig;

import com.vaadin.ui.Component;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.dbmanager.DbManager;
import org.safehaus.kiskis.mgmt.api.pig.Pig;
import org.safehaus.kiskis.mgmt.api.tracker.Tracker;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;

/**
 *
 * @author dilshat
 */
public class PigUI implements Module {

    public static final String MODULE_NAME = "Pig";
    private static Pig pigManager;
    private static AgentManager agentManager;
    private static Tracker tracker;
    private static DbManager dbManager;
    private static ExecutorService executor;

    public static Tracker getTracker() {
        return tracker;
    }

    public static Pig getPigManager() {
        return pigManager;
    }

    public void setPigManager(Pig pigManager) {
        PigUI.pigManager = pigManager;
    }

    public void setTracker(Tracker tracker) {
        PigUI.tracker = tracker;
    }

    public static DbManager getDbManager() {
        return dbManager;
    }

    public void setDbManager(DbManager dbManager) {
        PigUI.dbManager = dbManager;
    }

    public static ExecutorService getExecutor() {
        return executor;
    }

    public static AgentManager getAgentManager() {
        return agentManager;
    }

    public void setAgentManager(AgentManager agentManager) {
        PigUI.agentManager = agentManager;
    }

    public void init() {
        executor = Executors.newCachedThreadPool();
    }

    public void destroy() {
        pigManager = null;
        agentManager = null;
        dbManager = null;
        tracker = null;
        executor.shutdown();
    }

    public String getName() {
        return MODULE_NAME;
    }

    public Component createComponent() {
        return new PigForm();
    }

}
