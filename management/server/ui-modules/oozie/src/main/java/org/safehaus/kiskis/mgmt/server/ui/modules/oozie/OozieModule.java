package org.safehaus.kiskis.mgmt.server.ui.modules.oozie;

import com.vaadin.ui.*;
import com.vaadin.ui.themes.Runo;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskRunner;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.server.ui.modules.oozie.management.Manager;
import org.safehaus.kiskis.mgmt.server.ui.modules.oozie.wizard.Wizard;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

public class OozieModule implements Module {

    public static final String MODULE_NAME = "Oozie";
    private static final Logger LOG = Logger.getLogger(OozieModule.class.getName());
    private static TaskRunner taskRunner;

    public void setTaskRunner(TaskRunner taskRunner) {
        OozieModule.taskRunner = taskRunner;
    }

    public static TaskRunner getTaskRunner() {
        return taskRunner;
    }

    public static class ModuleComponent extends CustomComponent {

        Wizard wizard;
        Manager manager;

        public ModuleComponent(TaskRunner taskRunner) {
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

//        @Override
//        public void onCommand(Response response) {
//            if (wizard != null) {
//                wizard.setOutput(response);
//            }
//            if (manager != null) {
//                manager.setOutput(response);
//            }
//        }
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
        return new ModuleComponent(taskRunner);
    }

}
