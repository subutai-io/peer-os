package org.safehaus.kiskis.mgmt.ui.flume;

import com.vaadin.ui.Component;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.flume.Config;
import org.safehaus.kiskis.mgmt.api.flume.Flume;
import org.safehaus.kiskis.mgmt.api.tracker.Tracker;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;

public class FlumeUI implements Module {

    private static AgentManager agentManager;
    private static Tracker tracker;
    private static Flume flumeManager;
    private static ExecutorService executor;

    public void init() {
        executor = Executors.newCachedThreadPool();
    }

    public void destroy() {
        agentManager = null;
        tracker = null;
        flumeManager = null;
        executor.shutdown();
    }

    public static AgentManager getAgentManager() {
        return agentManager;
    }

    public static void setAgentManager(AgentManager agentManager) {
        FlumeUI.agentManager = agentManager;
    }

    public static Tracker getTracker() {
        return tracker;
    }

    public static void setTracker(Tracker tracker) {
        FlumeUI.tracker = tracker;
    }

    public static Flume getFlumeManager() {
        return flumeManager;
    }

    public static void setFlumeManager(Flume flumeManager) {
        FlumeUI.flumeManager = flumeManager;
    }

    public static ExecutorService getExecutor() {
        return executor;
    }

    public String getName() {
        return Config.PRODUCT_KEY;
    }

    public Component createComponent() {
        return new FlumeForm();
    }

}
