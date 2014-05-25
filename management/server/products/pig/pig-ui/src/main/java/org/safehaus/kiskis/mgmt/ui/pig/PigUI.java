/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.ui.pig;

import com.vaadin.ui.Component;
import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.hadoop.Hadoop;
import org.safehaus.kiskis.mgmt.api.pig.Config;
import org.safehaus.kiskis.mgmt.api.pig.Pig;
import org.safehaus.subutai.api.tracker.Tracker;
import org.safehaus.subutai.server.ui.services.Module;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author dilshat
 */
public class PigUI implements Module {

    private static Pig pigManager;
    private static AgentManager agentManager;
    private static Tracker tracker;
    private static Hadoop hadoopManager;
    private static ExecutorService executor;

    public PigUI(AgentManager agentManager, Tracker tracker, Hadoop hadoopManager, Pig pigManager) {
        PigUI.agentManager = agentManager;
        PigUI.tracker = tracker;
        PigUI.hadoopManager = hadoopManager;
        PigUI.pigManager = pigManager;
    }

    public static Tracker getTracker() {
        return tracker;
    }

    public static Pig getPigManager() {
        return pigManager;
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
        pigManager = null;
        agentManager = null;
        hadoopManager = null;
        tracker = null;
        executor.shutdown();
    }

    public String getName() {
        return Config.PRODUCT_KEY;
    }

    public Component createComponent() {
        return new PigForm();
    }

}
