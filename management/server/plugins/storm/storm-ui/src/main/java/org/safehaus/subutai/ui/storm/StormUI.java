package org.safehaus.subutai.ui.storm;

import com.vaadin.ui.Component;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.safehaus.subutai.api.zookeeper.Zookeeper;
import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.storm.api.Storm;
import org.safehaus.subutai.plugin.storm.api.StormConfig;
import org.safehaus.subutai.server.ui.api.PortalModule;

public class StormUI implements PortalModule {

    public static final String MODULE_IMAGE = "storm.png";

    private static AgentManager agentManager;
    private static Tracker tracker;
    private static Storm manager;
    private static Zookeeper zookeeper;
    private static CommandRunner commandRunner;
    private static ExecutorService executor;

    public StormUI(AgentManager agentManager, Tracker tracker, Zookeeper zookeeper, Storm manager, CommandRunner commandRunner) {
        StormUI.agentManager = agentManager;
        StormUI.tracker = tracker;
        StormUI.zookeeper = zookeeper;
        StormUI.manager = manager;
        StormUI.commandRunner = commandRunner;
    }

    public static AgentManager getAgentManager() {
        return agentManager;
    }

    public static Tracker getTracker() {
        return tracker;
    }

    public static Storm getManager() {
        return manager;
    }

    public static Zookeeper getZookeeper() {
        return zookeeper;
    }

    public static CommandRunner getCommandRunner() {
        return commandRunner;
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

    @Override
    public String getId() {
        return StormConfig.PRODUCT_NAME;
    }

    @Override
    public String getName() {
        return StormConfig.PRODUCT_NAME;
    }

    @Override
    public File getImage() {
        return FileUtil.getFile(StormUI.MODULE_IMAGE, this);
    }

    @Override
    public Component createComponent() {
        return new StormForm();
    }
}
