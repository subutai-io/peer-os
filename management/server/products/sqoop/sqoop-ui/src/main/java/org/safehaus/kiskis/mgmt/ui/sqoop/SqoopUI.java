package org.safehaus.kiskis.mgmt.ui.sqoop;

import com.vaadin.ui.Component;
import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.hadoop.Hadoop;
import org.safehaus.kiskis.mgmt.api.sqoop.Config;
import org.safehaus.kiskis.mgmt.api.sqoop.Sqoop;
import org.safehaus.subutai.api.tracker.Tracker;
import org.safehaus.subutai.server.ui.services.Module;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SqoopUI implements Module {

    private static AgentManager agentManager;
    private static Tracker tracker;
    private static Sqoop manager;
    private static Hadoop hadoopManager;

    private static ExecutorService executor;
    private static SqoopForm form;

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

    public static Hadoop getHadoopManager() {
        return hadoopManager;
    }

    public void setHadoopManager(Hadoop hadoopManager) {
        SqoopUI.hadoopManager = hadoopManager;
    }

    public static ExecutorService getExecutor() {
        return executor;
    }

    public static SqoopForm getForm() {
        return form;
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
        SqoopUI.form = new SqoopForm();
        return SqoopUI.form;
    }

}
