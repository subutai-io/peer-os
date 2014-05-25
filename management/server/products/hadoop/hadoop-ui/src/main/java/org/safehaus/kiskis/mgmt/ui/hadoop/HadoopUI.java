package org.safehaus.kiskis.mgmt.ui.hadoop;

import com.vaadin.ui.Component;
import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.hadoop.Config;
import org.safehaus.kiskis.mgmt.api.hadoop.Hadoop;
import org.safehaus.kiskis.mgmt.api.tracker.Tracker;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by daralbaev on 08.04.14.
 */
public class HadoopUI implements Module {

    private static Hadoop hadoopManager;
    private static AgentManager agentManager;
    private static ExecutorService executor;
    private static Tracker tracker;

    public static Hadoop getHadoopManager() {
        return hadoopManager;
    }

    public void setHadoopManager(Hadoop hadoopManager) {
        HadoopUI.hadoopManager = hadoopManager;
    }

    public static AgentManager getAgentManager() {
        return agentManager;
    }

    public void setAgentManager(AgentManager agentManager) {
        HadoopUI.agentManager = agentManager;
    }

    public static ExecutorService getExecutor() {
        return executor;
    }

    public void setExecutor(ExecutorService executor) {
        HadoopUI.executor = executor;
    }

    public static Tracker getTracker() {
        return tracker;
    }

    public void setTracker(Tracker tracker) {
        HadoopUI.tracker = tracker;
    }

    public void init() {
        executor = Executors.newCachedThreadPool();
    }

    public void destroy() {
        tracker = null;
        hadoopManager = null;
        agentManager = null;
        executor.shutdown();
    }

    @Override
    public String getName() {
        return Config.PRODUCT_KEY;
    }

    @Override
    public Component createComponent() {
        return new HadoopForm();
    }
}
