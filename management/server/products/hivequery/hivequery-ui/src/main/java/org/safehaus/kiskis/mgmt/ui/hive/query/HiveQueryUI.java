package org.safehaus.kiskis.mgmt.ui.hive.query;

import com.vaadin.ui.Component;
import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.hive.query.Config;
import org.safehaus.kiskis.mgmt.api.hive.query.HiveQuery;
import org.safehaus.subutai.api.tracker.Tracker;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HiveQueryUI implements Module {

    private static AgentManager agentManager;
    private static Tracker tracker;
    private static HiveQuery manager;
    private static ExecutorService executor;

    public static AgentManager getAgentManager() {
        return agentManager;
    }

    public void setAgentManager(AgentManager agentManager) {
        HiveQueryUI.agentManager = agentManager;
    }

    public static Tracker getTracker() {
        return tracker;
    }

    public void setTracker(Tracker tracker) {
        HiveQueryUI.tracker = tracker;
    }

    public static HiveQuery getManager() {
        return manager;
    }

    public void setManager(HiveQuery manager) {
        HiveQueryUI.manager = manager;
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
        executor.shutdown();
    }

    public String getName() {
        return Config.PRODUCT_KEY;
    }

    public Component createComponent() {
        return new HiveQueryForm();
    }

}
