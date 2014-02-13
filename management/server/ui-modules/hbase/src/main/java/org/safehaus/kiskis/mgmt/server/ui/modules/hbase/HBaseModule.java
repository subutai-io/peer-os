package org.safehaus.kiskis.mgmt.server.ui.modules.hbase;

import org.safehaus.kiskis.mgmt.server.ui.modules.hbase.wizard.Wizard;
import org.safehaus.kiskis.mgmt.server.ui.modules.hbase.management.Manager;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.Runo;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AsyncTaskRunner;

public class HBaseModule implements Module {

    public static final String MODULE_NAME = "HBase";
    private static final Logger LOG = Logger.getLogger(HBaseModule.class.getName());
    private AsyncTaskRunner asyncTaskRunner;

    public void setAsyncTaskRunner(AsyncTaskRunner asyncTaskRunner) {
        this.asyncTaskRunner = asyncTaskRunner;
    }

    public static class ModuleComponent extends CustomComponent {

        Wizard wizard;
        Manager manager;

        public ModuleComponent(AsyncTaskRunner asyncTaskRunner) {
            setSizeFull();

            VerticalLayout verticalLayout = new VerticalLayout();
            verticalLayout.setSpacing(true);
            verticalLayout.setSizeFull();

            TabSheet sheet = new TabSheet();
            sheet.setStyleName(Runo.TABSHEET_SMALL);
            sheet.setSizeFull();

            wizard = new Wizard(asyncTaskRunner);
            manager = new Manager(asyncTaskRunner);
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
        return new ModuleComponent(asyncTaskRunner);
    }

}
