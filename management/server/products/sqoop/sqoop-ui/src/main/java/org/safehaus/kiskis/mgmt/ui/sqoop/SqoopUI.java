package org.safehaus.kiskis.mgmt.ui.sqoop;

import com.vaadin.ui.Component;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.dbmanager.DbManager;
import org.safehaus.kiskis.mgmt.api.sqoop.Sqoop;
import org.safehaus.kiskis.mgmt.api.tracker.Tracker;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;

public class SqoopUI implements Module {

    public static final String MODULE_NAME = "Sqoop v2";
    private static AgentManager agentManager;
    private static Tracker tracker;
    private static Sqoop manager;
    private static DbManager dbManager;

    private static ExecutorService executor;
    private static SqoopForm form;

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
        SqoopUI.agentManager = agentManager;
    }

    public static Tracker getTracker() {
        return tracker;
    }

    public void setTracker(Tracker tracker) {
        SqoopUI.tracker = tracker;
    }

    public static Sqoop getManager() {
        return manager;
    }

    public void setManager(Sqoop manager) {
        SqoopUI.manager = manager;
    }

    public static DbManager getDbManager() {
        return dbManager;
    }

    public void setDbManager(DbManager dbManager) {
        SqoopUI.dbManager = dbManager;
    }

    public static ExecutorService getExecutor() {
        return executor;
    }

    public String getName() {
        return MODULE_NAME;
    }

    public Component createComponent() {
        SqoopUI.form = new SqoopForm();
        return SqoopUI.form;
    }

    public static SqoopForm getForm() {
        return form;
    }

}
