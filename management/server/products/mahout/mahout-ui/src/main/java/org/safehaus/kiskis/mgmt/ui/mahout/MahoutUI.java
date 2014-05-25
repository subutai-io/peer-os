/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.ui.mahout;

import com.vaadin.ui.Component;
import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.hadoop.Hadoop;
import org.safehaus.kiskis.mgmt.api.mahout.Config;
import org.safehaus.kiskis.mgmt.api.mahout.Mahout;
import org.safehaus.subutai.api.tracker.Tracker;
import org.safehaus.subutai.server.ui.services.Module;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author dilshat
 */
public class MahoutUI implements Module {

    private static Mahout mahoutManager;
    private static AgentManager agentManager;
    private static Tracker tracker;
    private static Hadoop hadoopManager;
    private static ExecutorService executor;

    public MahoutUI(AgentManager agentManager, Tracker tracker, Hadoop hadoopManager, Mahout mahoutManager) {
        MahoutUI.agentManager = agentManager;
        MahoutUI.tracker = tracker;
        MahoutUI.hadoopManager = hadoopManager;
        MahoutUI.mahoutManager = mahoutManager;
    }

    public static Tracker getTracker() {
        return tracker;
    }

    public static Mahout getMahoutManager() {
        return mahoutManager;
    }

    public static Hadoop getHadoopManager() {
        return hadoopManager;
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
        mahoutManager = null;
        agentManager = null;
        hadoopManager = null;
        tracker = null;
        executor.shutdown();
    }

    public String getName() {
        return Config.PRODUCT_KEY;
    }

    public Component createComponent() {
        return new MahoutForm();
    }

}
