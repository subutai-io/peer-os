package org.safehaus.kiskis.mgmt.server.ui;

import com.vaadin.Application;
import com.vaadin.Application.SystemMessages;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.commandrunner.CommandRunner;
import org.safehaus.kiskis.mgmt.api.tracker.Tracker;
import org.safehaus.kiskis.mgmt.server.ui.bridge.ApplicationFactory;
import org.safehaus.kiskis.mgmt.server.ui.services.ModuleNotifier;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MgmtAppFactory implements ApplicationFactory {

    private static ExecutorService executor;
    private final String title;
    private AgentManager agentManager;
    private CommandRunner commandRunner;
    private Tracker tracker;
    private ModuleNotifier moduleNotifier;

    public MgmtAppFactory(String title, AgentManager agentManager, CommandRunner commandRunner, Tracker tracker, ModuleNotifier moduleNotifier) {
        this.title = title;
        this.agentManager = agentManager;
        this.commandRunner = commandRunner;
        this.tracker = tracker;
        this.moduleNotifier = moduleNotifier;
    }

    public static ExecutorService getExecutor() {
        return executor;
    }

    @Override
    public String getApplicationCSSClassName() {
        return "MgmtApplication";
    }

    @Override
    public SystemMessages getSystemMessages() {
        return null;
    }

    @Override
    public Application newInstance() {
        return new MgmtApplication(title, agentManager, commandRunner, tracker, moduleNotifier);
    }

    public void init() {
        executor = Executors.newCachedThreadPool();
    }

    public void destroy() {
        executor.shutdown();
    }

}
