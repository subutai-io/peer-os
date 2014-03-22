package org.safehaus.kiskis.mgmt.server.ui.modules.mongo;

import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.wizard.Wizard;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.Runo;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.dbmanager.DbManager;
import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcManager;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskRunner;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.manager.Manager;

public class MongoModule implements Module {

    public static final String MODULE_NAME = "MongoDB";
    private static TaskRunner taskRunner;
    private static AgentManager agentManager;
    private static DbManager dbManager;
    private static LxcManager lxcManager;
    private static ExecutorService executor;

    public void setLxcManager(LxcManager lxcManager) {
        MongoModule.lxcManager = lxcManager;
    }

    public void setAgentManager(AgentManager agentManager) {
        MongoModule.agentManager = agentManager;
    }

    public void setDbManager(DbManager dbManager) {
        MongoModule.dbManager = dbManager;
    }

    public void setTaskRunner(TaskRunner taskRunner) {
        MongoModule.taskRunner = taskRunner;
    }

    public static TaskRunner getTaskRunner() {
        return taskRunner;
    }

    public static AgentManager getAgentManager() {
        return agentManager;
    }

    public static DbManager getDbManager() {
        return dbManager;
    }

    public static LxcManager getLxcManager() {
        return lxcManager;
    }

    public static ExecutorService getExecutor() {
        return executor;
    }

    public void init() {
        executor = Executors.newCachedThreadPool();
    }

    public void destroy() {
        MongoModule.taskRunner = null;
        MongoModule.agentManager = null;
        MongoModule.dbManager = null;
        MongoModule.lxcManager = null;
        executor.shutdown();
    }

    public static class ModuleComponent extends CustomComponent {

        private final Wizard wizard;
        private final Manager manager;

        public ModuleComponent() {
            setSizeFull();
            VerticalLayout verticalLayout = new VerticalLayout();
            verticalLayout.setSpacing(true);
            verticalLayout.setSizeFull();

            TabSheet mongoSheet = new TabSheet();
            mongoSheet.setStyleName(Runo.TABSHEET_SMALL);
            mongoSheet.setSizeFull();
            wizard = new Wizard();
            manager = new Manager();
            mongoSheet.addTab(wizard.getContent(), "Install");
            mongoSheet.addTab(manager.getContent(), "Manage");

            verticalLayout.addComponent(mongoSheet);

            setCompositionRoot(verticalLayout);

        }

    }

    @Override
    public String getName() {
        return MODULE_NAME;
    }

    @Override
    public Component createComponent() {
        return new ModuleComponent();
    }

}
