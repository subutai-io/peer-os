/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.ui.zookeeper;

import com.vaadin.ui.Component;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.zookeeper.Api;
import org.safehaus.kiskis.mgmt.api.tracker.Tracker;
import org.safehaus.kiskis.mgmt.api.zookeeper.Config;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;

/**
 *
 * @author dilshat
 */
public class UI implements Module {

    private static Api manager;
    private static AgentManager agentManager;
    private static Tracker tracker;
    private static ExecutorService executor;

    public static Tracker getTracker() {
        return tracker;
    }

    public void setTracker(Tracker tracker) {
        UI.tracker = tracker;
    }

    public static Api getManager() {
        return manager;
    }

    public static ExecutorService getExecutor() {
        return executor;
    }

    public void setManager(Api manager) {
        UI.manager = manager;
    }

    public static AgentManager getAgentManager() {
        return agentManager;
    }

    public void setAgentManager(AgentManager agentManager) {
        UI.agentManager = agentManager;
    }

    public void init() {
        executor = Executors.newCachedThreadPool();
    }

    public void destroy() {
        manager = null;
        agentManager = null;
        tracker = null;
        executor.shutdown();
    }

    public String getName() {
        return Config.PRODUCT_KEY;
    }

    public Component createComponent() {
        return new Form();
    }

}
