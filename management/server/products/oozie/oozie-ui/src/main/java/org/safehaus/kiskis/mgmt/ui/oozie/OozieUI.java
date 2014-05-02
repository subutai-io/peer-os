/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.ui.oozie;

import com.vaadin.ui.Component;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.hadoop.Hadoop;
import org.safehaus.kiskis.mgmt.api.oozie.Config;
import org.safehaus.kiskis.mgmt.api.oozie.Oozie;
import org.safehaus.kiskis.mgmt.api.tracker.Tracker;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author dilshat
 */
public class OozieUI implements Module {

    private static Oozie oozieManager;
    private static AgentManager agentManager;
    private static Tracker tracker;
    private static ExecutorService executor;
    private static Hadoop hadoopManager;

    public static Tracker getTracker() {
        return tracker;
    }

    public void setTracker(Tracker tracker) {
        OozieUI.tracker = tracker;
    }

    public static Oozie getOozieManager() {
        return oozieManager;
    }

    public void setOozieManager(Oozie oozieManager) {
        OozieUI.oozieManager = oozieManager;
    }

    public static ExecutorService getExecutor() {
        return executor;
    }

    public static AgentManager getAgentManager() {
        return agentManager;
    }

    public void setAgentManager(AgentManager agentManager) {
        OozieUI.agentManager = agentManager;
    }

    public static Hadoop getHadoopManager() {
        return hadoopManager;
    }

    public void setHadoopManager(Hadoop hadoopManager) {
        OozieUI.hadoopManager = hadoopManager;
    }

    public void init() {
        executor = Executors.newCachedThreadPool();
    }

    public void destroy() {
        oozieManager = null;
        agentManager = null;
        tracker = null;
        hadoopManager = null;
        executor.shutdown();
    }

    public String getName() {
        return Config.PRODUCT_KEY;
    }

    public Component createComponent() {
        return new OozieForm();
    }

}
