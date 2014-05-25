/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.ui.shark;

import com.vaadin.ui.Component;
import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.shark.Config;
import org.safehaus.kiskis.mgmt.api.shark.Shark;
import org.safehaus.kiskis.mgmt.api.spark.Spark;
import org.safehaus.subutai.api.tracker.Tracker;
import org.safehaus.subutai.server.ui.services.Module;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author dilshat
 */
public class SharkUI implements Module {

    private static Shark sharkManager;
    private static AgentManager agentManager;
    private static Tracker tracker;
    private static Spark sparkManager;
    private static ExecutorService executor;

    public SharkUI(AgentManager agentManager, Tracker tracker, Spark sparkManager, Shark sharkManager) {
        SharkUI.agentManager = agentManager;
        SharkUI.tracker = tracker;
        SharkUI.sparkManager = sparkManager;
        SharkUI.sharkManager = sharkManager;
    }

    public static Tracker getTracker() {
        return tracker;
    }

    public static Shark getSharkManager() {
        return sharkManager;
    }

    public static Spark getSparkManager() {
        return sparkManager;
    }

    public static ExecutorService getExecutor() {
        return executor;
    }

    public static AgentManager getAgentManager() {
        return agentManager;
    }

    public void init() {
        executor = Executors.newCachedThreadPool();
    }

    public void destroy() {
        sharkManager = null;
        agentManager = null;
        sparkManager = null;
        tracker = null;
        executor.shutdown();
    }

    public String getName() {
        return Config.PRODUCT_KEY;
    }

    public Component createComponent() {
        return new SharkForm();
    }

}
