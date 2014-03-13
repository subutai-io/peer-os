package org.safehaus.kiskis.mgmt.server.ui.modules.oozie;

import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.dbmanager.DbManager;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskRunner;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.server.ui.modules.oozie.management.Manager;
import org.safehaus.kiskis.mgmt.server.ui.modules.oozie.wizard.Wizard;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

public class OozieModule implements Module {

    private static final Logger LOG = Logger.getLogger(OozieModule.class.getName());

    public static final String MODULE_NAME = "Oozie";
    private static TaskRunner taskRunner;
    private static AgentManager agentManager;
    private DbManager dbManager;
    private OozieDAO oozieDAO;

    public void setAgentManager(AgentManager agentManager) {
        OozieModule.agentManager = agentManager;
    }

    public void setDbManager(DbManager dbManager) {
        this.dbManager = dbManager;
    }

    public void setTaskRunner(TaskRunner taskRunner) {
        OozieModule.taskRunner = taskRunner;
    }

    public static TaskRunner getTaskRunner() {
        return taskRunner;
    }

    public static AgentManager getAgentManager() {
        return agentManager;
    }

    public DbManager getDbManager() {
        return dbManager;
    }

    public static class ModuleComponent extends CustomComponent {

        Wizard wizard;
        Manager manager;

        public ModuleComponent(TaskRunner taskRunner, OozieDAO oozieDAO) {
            setSizeFull();

            VerticalLayout verticalLayout = new VerticalLayout();
            verticalLayout.setSpacing(true);
            verticalLayout.setSizeFull();

            TabSheet sheet = new TabSheet();
            sheet.setStyleName(Runo.TABSHEET_SMALL);
            sheet.setSizeFull();
            LOG.log(Level.INFO, "OOZIEDAO: {0}", oozieDAO);
            wizard = new Wizard(oozieDAO);
            manager = new Manager(oozieDAO);
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
        return OozieModule.MODULE_NAME;
    }

    @Override
    public Component createComponent() {
        oozieDAO = new OozieDAO(dbManager);
        return new ModuleComponent(taskRunner, oozieDAO);
    }

}
