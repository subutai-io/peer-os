package org.safehaus.kiskis.mgmt.server.ui.modules.hbase;

import org.safehaus.kiskis.mgmt.server.ui.modules.hbase.wizard.Wizard;
import org.safehaus.kiskis.mgmt.server.ui.modules.hbase.management.Manager;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.Runo;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.CommandListener;
import java.util.logging.Logger;

public class HBaseModule implements Module {

    public static final String MODULE_NAME = "HBase";
    private static final Logger LOG = Logger.getLogger(HBaseModule.class.getName());

    public static class ModuleComponent extends CustomComponent implements
            CommandListener {

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

        @Override
        public void onCommand(Response response) {
            if (wizard != null) {
                wizard.setOutput(response);
            }
            if (manager != null) {
                manager.setOutput(response);
            }
        }

        @Override
        public String getName() {
            return MODULE_NAME;
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
