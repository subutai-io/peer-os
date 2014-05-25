package org.safehaus.subutai.ui.storm;

import com.vaadin.ui.Component;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.storm.Config;
import org.safehaus.subutai.api.storm.Storm;
import org.safehaus.subutai.api.tracker.Tracker;
import org.safehaus.kiskis.mgmt.api.zookeeper.Zookeeper;
import org.safehaus.subutai.server.ui.services.Module;

public class StormUI implements Module {

    private static AgentManager agentManager;
    private static Tracker tracker;
    private static Storm manager;
    private static Zookeeper zookeeper;

    private static ExecutorService executor;

    public static AgentManager getAgentManager() {
        return agentManager;
    }

    public void setAgentManager(AgentManager agentManager) {
        StormUI.agentManager = agentManager;
    }

    public static Tracker getTracker() {
        return tracker;
    }

    public void setTracker(Tracker tracker) {
        StormUI.tracker = tracker;
    }

    public static Storm getManager() {
        return manager;
    }

    public void setManager(Storm manager) {
        StormUI.manager = manager;
    }

    public static Zookeeper getZookeeper() {
        return zookeeper;
    }

    public void setZookeeper(Zookeeper zookeeper) {
        StormUI.zookeeper = zookeeper;
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
        zookeeper = null;
        executor.shutdown();
    }

    public String getName() {
        return Config.PRODUCT_NAME;
    }

    public Component createComponent() {
        return new StormForm();
    }

}
