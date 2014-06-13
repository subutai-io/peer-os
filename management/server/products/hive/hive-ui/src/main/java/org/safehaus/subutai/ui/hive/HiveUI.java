package org.safehaus.subutai.ui.hive;

import com.vaadin.ui.Component;
import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.commandrunner.CommandRunner;
import org.safehaus.subutai.api.hadoop.Hadoop;
import org.safehaus.subutai.api.hive.Config;
import org.safehaus.subutai.api.hive.Hive;
import org.safehaus.subutai.api.tracker.Tracker;
import org.safehaus.subutai.server.ui.api.PortalModule;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HiveUI implements PortalModule {

    private static AgentManager agentManager;
    private static Tracker tracker;
    private static Hive manager;
    private static Hadoop hadoopManager;
    private static CommandRunner commandRunner;
    private static ExecutorService executor;

    public HiveUI(
            AgentManager agentManager,
            Tracker tracker,
            Hive manager,
            CommandRunner commandRunner,
            Hadoop hadoopManager
    ) {
        HiveUI.agentManager = agentManager;
        HiveUI.tracker = tracker;
        HiveUI.manager = manager;
        HiveUI.hadoopManager = hadoopManager;
        HiveUI.commandRunner = commandRunner;
    }

    public static AgentManager getAgentManager() {
        return agentManager;
    }

    public static Tracker getTracker() {
        return tracker;
    }

    public static Hive getManager() {
        return manager;
    }

    public static Hadoop getHadoopManager() {
        return hadoopManager;
    }

    public static ExecutorService getExecutor() {
        return executor;
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
        hadoopManager = null;
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
        return new HiveForm();
    }

}
