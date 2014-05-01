package org.safehaus.kiskis.mgmt.ui.flume;

import com.vaadin.ui.Component;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.flume.Config;
import org.safehaus.kiskis.mgmt.api.flume.Flume;
import org.safehaus.kiskis.mgmt.api.hadoop.Hadoop;
import org.safehaus.kiskis.mgmt.api.tracker.Tracker;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FlumeUI implements Module {

    private static AgentManager agentManager;
    private static Tracker tracker;
    private static Flume manager;
    private static Hadoop hadoopManager;
    private static ExecutorService executor;

    public static AgentManager getAgentManager() {
        return agentManager;
    }

    public void setAgentManager(AgentManager agentManager) {
        FlumeUI.agentManager = agentManager;
    }

    public static Tracker getTracker() {
        return tracker;
    }

    public void setTracker(Tracker tracker) {
        FlumeUI.tracker = tracker;
    }

    public static Flume getManager() {
        return manager;
    }

    public void setManager(Flume manager) {
        FlumeUI.manager = manager;
    }

    public static Hadoop getHadoopManager() {
        return hadoopManager;
    }

    public void setHadoopManager(Hadoop hadoopManager) {
        FlumeUI.hadoopManager = hadoopManager;
    }

    public static ExecutorService getExecutor() {
        return executor;
    }

    public void init() {
        executor = Executors.newCachedThreadPool();
    }

    public void destroy() {
        agentManager = null;
        tracker = null;
        manager = null;
        hadoopManager = null;
        executor.shutdown();
    }

    public String getName() {
        return Config.PRODUCT_KEY;
    }

    public Component createComponent() {
        return new FlumeForm();
    }

}
