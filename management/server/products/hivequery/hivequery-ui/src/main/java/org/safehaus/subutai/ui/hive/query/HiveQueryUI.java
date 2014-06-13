package org.safehaus.subutai.ui.hive.query;

import com.vaadin.ui.Component;
import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.commandrunner.CommandRunner;
import org.safehaus.subutai.api.hadoop.Hadoop;
import org.safehaus.subutai.api.hive.query.Config;
import org.safehaus.subutai.api.hive.query.HiveQuery;
import org.safehaus.subutai.api.tracker.Tracker;
import org.safehaus.subutai.server.ui.api.PortalModule;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HiveQueryUI implements PortalModule {

    private static AgentManager agentManager;
    private static Tracker tracker;
    private static HiveQuery manager;
    private static Hadoop hadoopManager;
    private static CommandRunner commandRunner;
    private static ExecutorService executor;

    public HiveQueryUI(
            AgentManager agentManager,
            Tracker tracker,
            HiveQuery manager,
            Hadoop hadoopManager,
            CommandRunner commandRunner
    ) {
        HiveQueryUI.agentManager = agentManager;
        HiveQueryUI.tracker = tracker;
        HiveQueryUI.manager = manager;
        HiveQueryUI.hadoopManager = hadoopManager;
        HiveQueryUI.commandRunner = commandRunner;
    }

    public static AgentManager getAgentManager() {
        return agentManager;
    }

    public static Tracker getTracker() {
        return tracker;
    }

    public static HiveQuery getManager() {
        return manager;
    }

    public static ExecutorService getExecutor() {
        return executor;
    }

    public static Hadoop getHadoopManager() {
        return hadoopManager;
    }

    public static CommandRunner getCommandRunner() {
        return commandRunner;
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

    @Override
    public String getId() {
        return Config.PRODUCT_KEY;
    }

    public String getName() {
        return Config.PRODUCT_KEY;
    }

    public Component createComponent() {
        return new HiveQueryForm();
    }

}
