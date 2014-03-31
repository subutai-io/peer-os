/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.ui.mahout;

import com.vaadin.ui.Component;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.mahout.Mahout;
import org.safehaus.kiskis.mgmt.api.tracker.Tracker;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;

/**
 *
 * @author dilshat
 */
public class MahoutUI implements Module {

    public static final String MODULE_NAME = "Mahout";
    private static Mahout mahoutManager;
    private static AgentManager agentManager;
    private static Tracker tracker;
    private static ExecutorService executor;

    public static Tracker getTracker() {
        return tracker;
    }

    public static Mahout getMahoutManager() {
        return mahoutManager;
    }

    public void setMahoutManager(Mahout mahoutManager) {
        MahoutUI.mahoutManager = mahoutManager;
    }

    public void setTracker(Tracker tracker) {
        MahoutUI.tracker = tracker;
    }

    public static ExecutorService getExecutor() {
        return executor;
    }

    public static AgentManager getAgentManager() {
        return agentManager;
    }

    public void setAgentManager(AgentManager agentManager) {
        MahoutUI.agentManager = agentManager;
    }

    public void init() {
        executor = Executors.newCachedThreadPool();
    }

    public void destroy() {
        mahoutManager = null;
        agentManager = null;
        tracker = null;
        executor.shutdown();
    }

    public String getName() {
        return MODULE_NAME;
    }

    public Component createComponent() {
        return new MahoutForm();
    }

}
