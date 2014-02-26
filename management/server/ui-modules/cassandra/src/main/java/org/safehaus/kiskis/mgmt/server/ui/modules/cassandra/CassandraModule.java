package org.safehaus.kiskis.mgmt.server.ui.modules.cassandra;

import com.vaadin.ui.*;
import com.vaadin.ui.themes.Runo;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskRunner;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.management.CassandraManager;
import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.wizard.CassandraWizard;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

public class CassandraModule implements Module {

    public static final String MODULE_NAME = "Cassandra";
    private static final Logger LOG = Logger.getLogger(CassandraModule.class.getName());
    private static TaskRunner taskRunner;

    public static TaskRunner getTaskRunner() {
        return taskRunner;
    }

    public static void setTaskRunner(TaskRunner taskRunner) {
        CassandraModule.taskRunner = taskRunner;
    }

    public static class ModuleComponent extends CustomComponent {

        CassandraWizard cassandraWizard;
        CassandraManager cassandraManager;

        public ModuleComponent() {
            setSizeFull();
            VerticalLayout verticalLayout = new VerticalLayout();
            verticalLayout.setSpacing(true);
            verticalLayout.setSizeFull();

            TabSheet sheet = new TabSheet();
            sheet.setStyleName(Runo.TABSHEET_SMALL);
            sheet.setSizeFull();

            cassandraWizard = new CassandraWizard();
            cassandraManager = new CassandraManager();
            sheet.addTab(cassandraWizard.getContent(), "Install");
            sheet.addTab(cassandraManager.getContent(), "Manage");

            verticalLayout.addComponent(sheet);

            setCompositionRoot(verticalLayout);
        }

        public Iterable<Agent> getLxcList() {
            return MgmtApplication.getSelectedAgents();
        }

    }

    @Override
    public String getName() {
        return CassandraModule.MODULE_NAME;
    }

    @Override
    public Component createComponent() {
        return new ModuleComponent();
    }

}
