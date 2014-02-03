package org.safehaus.kiskis.mgmt.server.ui.modules.lxc;

import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;
import org.safehaus.kiskis.mgmt.server.ui.modules.lxc.clone.Cloner;
import org.safehaus.kiskis.mgmt.server.ui.modules.lxc.manage.Manager;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AsyncTaskRunner;

public class LxcModule implements Module {

    private static final Logger LOG = Logger.getLogger(LxcModule.class.getName());
    public static final String MODULE_NAME = "LXC";
    private AsyncTaskRunner taskRunner;

    public void setTaskRunner(AsyncTaskRunner taskRunner) {
        this.taskRunner = taskRunner;
    }

    public static class ModuleComponent extends CustomComponent {

        private final TabSheet commandsSheet;
        private final Cloner cloner;
        private final Manager manager;

        public ModuleComponent(AsyncTaskRunner taskRunner) {

            VerticalLayout verticalLayout = new VerticalLayout();
            verticalLayout.setSpacing(true);
            verticalLayout.setSizeFull();

            commandsSheet = new TabSheet();
            commandsSheet.setStyleName(Runo.TABSHEET_SMALL);
            commandsSheet.setSizeFull();

            manager = new Manager(taskRunner);
            cloner = new Cloner(commandsSheet, taskRunner, manager);
            commandsSheet.addTab(cloner, "Clone");
            commandsSheet.addTab(manager, "Manage");

            verticalLayout.addComponent(commandsSheet);

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
