package org.safehaus.kiskis.mgmt.server.ui.modules.mongo;

import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.dbmanager.DbManager;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskRunner;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.manager.Manager;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.wizard.Wizard;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;

import java.util.logging.Logger;

public class MongoModule implements Module {

    private static final Logger LOG = Logger.getLogger(MongoModule.class.getName());
    public static final String MODULE_NAME = "MongoDB";
    private static TaskRunner taskRunner;
    private static AgentManager agentManager;
    private static DbManager dbManager;

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

    public static class ModuleComponent extends CustomComponent {

        private final Wizard wizard;
        private final Manager manager;

        public ModuleComponent(TaskRunner taskRunner) {
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
        return new ModuleComponent(taskRunner);
    }

}
