package org.safehaus.kiskis.mgmt.server.ui;

import com.vaadin.Application;
import com.vaadin.Application.SystemMessages;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.safehaus.kiskis.mgmt.server.ui.bridge.ApplicationFactory;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskRunner;
import org.safehaus.kiskis.mgmt.api.tracker.Tracker;
import org.safehaus.kiskis.mgmt.server.ui.services.ModuleNotifier;

public class MgmtAppFactory implements ApplicationFactory {

    private final String title;
    private AgentManager agentManager;
    private TaskRunner taskRunner;
    private Tracker tracker;
    private ModuleNotifier moduleNotifier;
    private static ExecutorService executor;

    public MgmtAppFactory(String title) {
        this.title = title;
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
        return new MgmtApplication(title, agentManager, taskRunner, tracker, moduleNotifier);
    }

    public void setAgentManager(AgentManager agentManager) {
        this.agentManager = agentManager;
    }

    public void setTaskRunner(TaskRunner taskRunner) {
        this.taskRunner = taskRunner;
    }

    public void setTracker(Tracker tracker) {
        this.tracker = tracker;
    }

    public void setModuleNotifier(ModuleNotifier moduleNotifier) {
        this.moduleNotifier = moduleNotifier;
    }

    public void init() {
        executor = Executors.newCachedThreadPool();
    }

    public void destroy() {
        executor.shutdown();
    }

}
