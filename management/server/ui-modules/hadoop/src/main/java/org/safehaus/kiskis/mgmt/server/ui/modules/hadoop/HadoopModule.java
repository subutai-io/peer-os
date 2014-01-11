package org.safehaus.kiskis.mgmt.server.ui.modules.hadoop;

import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.config.ClusterForm;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.wizard.HadoopWizard;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.CommandListener;

import java.util.logging.Logger;

public class HadoopModule implements Module {

    public static final String MODULE_NAME = "HadoopModule";
    private static final Logger LOG = Logger.getLogger(HadoopModule.class.getName());

    @Override
    public String getName() {
        return HadoopModule.MODULE_NAME;
    }

    @Override
    public Component createComponent() {
        return new ModuleComponent();
    }

    public static class ModuleComponent extends CustomComponent implements
            CommandListener {

        private HadoopWizard hadoopWizard;
        private ClusterForm hadoopManager;

        public ModuleComponent() {

            VerticalLayout verticalLayout = new VerticalLayout();
            verticalLayout.setSpacing(true);

            TabSheet sheet = new TabSheet();
            sheet.setStyleName(Runo.TABSHEET_SMALL);
            sheet.setSizeFull();

            hadoopWizard = new HadoopWizard();
            hadoopManager = new ClusterForm();

            sheet.addTab(hadoopWizard.getContent(), "Install");
            sheet.addTab(hadoopManager, "Manage");

            verticalLayout.addComponent(sheet);

            setCompositionRoot(verticalLayout);
        }

        @Override
        public void onCommand(Response response) {
            if (hadoopWizard != null) {
                hadoopWizard.setOutput(response);
            }

            if (hadoopManager != null) {
                hadoopManager.getClusterTable().onCommand(response);
            }
        }

        @Override
        public String getName() {
            return HadoopModule.MODULE_NAME;
        }
    }

}
