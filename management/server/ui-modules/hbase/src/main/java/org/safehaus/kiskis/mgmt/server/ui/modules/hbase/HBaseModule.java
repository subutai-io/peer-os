package org.safehaus.kiskis.mgmt.server.ui.modules.hbase;

import org.safehaus.kiskis.mgmt.server.ui.modules.hbase.wizard.Wizard;
import org.safehaus.kiskis.mgmt.server.ui.modules.hbase.management.Manager;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.Runo;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.dbmanager.DbManager;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskRunner;

public class HBaseModule implements Module {

    public static final String MODULE_NAME = "HBase";
    private static TaskRunner taskRunner;
    private static AgentManager agentManager;
    private static DbManager dbManager;

    public void setAgentManager(AgentManager agentManager) {
        HBaseModule.agentManager = agentManager;
    }

    public void setDbManager(DbManager dbManager) {
        HBaseModule.dbManager = dbManager;
    }

    public void setTaskRunner(TaskRunner taskRunner) {
        HBaseModule.taskRunner = taskRunner;
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

        Wizard wizard;
        Manager manager;

        public ModuleComponent() {
            setSizeFull();

            VerticalLayout verticalLayout = new VerticalLayout();
            verticalLayout.setSpacing(true);
            verticalLayout.setSizeFull();

            TabSheet sheet = new TabSheet();
            sheet.setStyleName(Runo.TABSHEET_SMALL);
            sheet.setSizeFull();

            wizard = new Wizard();
            manager = new Manager();
            sheet.addTab(wizard.getContent(), "Install");
            sheet.addTab(manager.getContent(), "Manage");

            verticalLayout.addComponent(sheet);

            setCompositionRoot(verticalLayout);
        }

        public Iterable<Agent> getLxcList() {
            return MgmtApplication.getSelectedAgents();
        }

    }

    @Override
    public String getName() {
        return HBaseModule.MODULE_NAME;
    }

    @Override
    public Component createComponent() {
        return new ModuleComponent();
    }

}
