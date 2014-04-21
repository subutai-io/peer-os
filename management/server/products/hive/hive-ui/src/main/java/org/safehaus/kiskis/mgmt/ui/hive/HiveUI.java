package org.safehaus.kiskis.mgmt.ui.hive;

import com.vaadin.ui.Component;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.dbmanager.DbManager;
import org.safehaus.kiskis.mgmt.api.hive.Hive;
import org.safehaus.kiskis.mgmt.api.tracker.Tracker;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;

public class HiveUI implements Module {

    public static final String MODULE_NAME = "Hive v2";
    private static AgentManager agentManager;
    private static Tracker tracker;
    private static Hive manager;
    private static DbManager dbManager;
    private static ExecutorService executor;

    public void init() {
        executor = Executors.newCachedThreadPool();
    }

    public void destroy() {
        agentManager = null;
        tracker = null;
        manager = null;
        executor.shutdown();
    }

    public static AgentManager getAgentManager() {
        return agentManager;
    }

    public void setAgentManager(AgentManager agentManager) {
        HiveUI.agentManager = agentManager;
    }

    public static Tracker getTracker() {
        return tracker;
    }

    public void setTracker(Tracker tracker) {
        HiveUI.tracker = tracker;
    }

    public static Hive getManager() {
        return manager;
    }

    public void setManager(Hive manager) {
        HiveUI.manager = manager;
    }

    public static DbManager getDbManager() {
        return dbManager;
    }

    public void setDbManager(DbManager dbManager) {
        HiveUI.dbManager = dbManager;
    }

    public static ExecutorService getExecutor() {
        return executor;
    }

    public String getName() {
        return MODULE_NAME;
    }

    public Component createComponent() {
        return new HiveForm();
    }

}
