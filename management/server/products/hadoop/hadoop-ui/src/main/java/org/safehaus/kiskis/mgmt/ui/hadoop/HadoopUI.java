package org.safehaus.kiskis.mgmt.ui.hadoop;

import com.vaadin.ui.Component;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.hadoop.Hadoop;
import org.safehaus.kiskis.mgmt.api.tracker.Tracker;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by daralbaev on 08.04.14.
 */
public class HadoopUI implements Module {

    public static final String MODULE_NAME = "Hadoop";
    private static Hadoop hadoopManager;
    private static AgentManager agentManager;
    private static ExecutorService executor;
    private static Tracker tracker;

    public void init() {
        executor = Executors.newCachedThreadPool();
    }

    public void destroy() {
        tracker = null;
        hadoopManager = null;
        agentManager = null;
        executor.shutdown();
    }

    public void setHadoopManager(Hadoop hadoopManager) {
        HadoopUI.hadoopManager = hadoopManager;
    }

    public void setAgentManager(AgentManager agentManager) {
        HadoopUI.agentManager = agentManager;
    }

    public void setExecutor(ExecutorService executor) {
        HadoopUI.executor = executor;
    }

    public void setTracker(Tracker tracker) {
        HadoopUI.tracker = tracker;
    }

    public static Hadoop getHadoopManager() {
        return hadoopManager;
    }

    public static AgentManager getAgentManager() {
        return agentManager;
    }

    public static ExecutorService getExecutor() {
        return executor;
    }

    public static Tracker getTracker() {
        return tracker;
    }

    @Override
    public String getName() {
        return MODULE_NAME;
    }

    @Override
    public Component createComponent() {
        return new HadoopForm();
    }
}
