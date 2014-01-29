package org.safehaus.kiskis.mgmt.server.ui.modules.lxc;

import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;
import org.safehaus.kiskis.mgmt.server.ui.modules.lxc.clone.Cloner;
import org.safehaus.kiskis.mgmt.server.ui.modules.lxc.manage.Manager;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.CommandListener;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.shared.protocol.TaskRunner;

public class LxcModule implements Module {

    private static final Logger LOG = Logger.getLogger(LxcModule.class.getName());
    public static final String MODULE_NAME = "LXC";

    public static class ModuleComponent extends CustomComponent implements CommandListener {

        private final TabSheet commandsSheet;
        private final Cloner cloneForm;
        private final Manager manageForm;
        private final TaskRunner taskRunner = new TaskRunner();

        public ModuleComponent() {

            VerticalLayout verticalLayout = new VerticalLayout();
            verticalLayout.setSpacing(true);
            verticalLayout.setSizeFull();

            commandsSheet = new TabSheet();
            commandsSheet.setStyleName(Runo.TABSHEET_SMALL);
            commandsSheet.setSizeFull();

            cloneForm = new Cloner(taskRunner);
            commandsSheet.addTab(cloneForm, "Clone");
            manageForm = new Manager(taskRunner);
            commandsSheet.addTab(manageForm, "Manage");

            verticalLayout.addComponent(commandsSheet);

            setCompositionRoot(verticalLayout);

        }

        @Override
        public void onCommand(Response response) {
            taskRunner.feedResponse(response);
        }

        @Override
        public String getName() {
            return MODULE_NAME;
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
