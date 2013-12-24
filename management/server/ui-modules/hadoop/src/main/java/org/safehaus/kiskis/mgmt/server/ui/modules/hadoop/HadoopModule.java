package org.safehaus.kiskis.mgmt.server.ui.modules.hadoop;


import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.VerticalLayout;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.config.HadoopClusterTable;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.wizard.HadoopWizard;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;
import org.safehaus.kiskis.mgmt.server.ui.services.ModuleService;
//import org.safehaus.kiskis.mgmt.server.ui.util.AppData;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.CommandListener;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;

public class HadoopModule implements Module {

    private static final Logger LOG = Logger.getLogger(HadoopModule.class.getName());
    public static final String MODULE_NAME = "HadoopModule";
    private static ModuleComponent component;

    public static class ModuleComponent extends CustomComponent implements
            CommandListener {

        private HadoopWizard subwindow;
        private HadoopClusterTable table;

        public ModuleComponent() {

            VerticalLayout verticalLayout = new VerticalLayout();
            verticalLayout.setSpacing(true);

            verticalLayout.addComponent(getButtonInstallWizard());
            verticalLayout.addComponent(getButtonRefresh());
            verticalLayout.addComponent(getHadoopClusterTable());

            setCompositionRoot(verticalLayout);
            HadoopModule.getCommandManager().addListener(this);

        }

        private HadoopClusterTable getHadoopClusterTable() {
            table = new HadoopClusterTable();
            return table;
        }

        private Button getButtonInstallWizard() {
            Button button = new Button("HadoopModule Installation Wizard");
            button.addListener(new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent event) {
                    if (getLxcAgents().size() > 0) {
                        subwindow = new HadoopWizard(getLxcAgents());
                        getApplication().getMainWindow().addWindow(subwindow);
                    }
                }
            });

            return button;
        }

        private Button getButtonRefresh() {
            Button button = new Button("Refresh Hadoop Cluster Table");
            button.addListener(new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent event) {
                    table.refreshDataSource();
                }
            });

            return button;
        }

        @Override
        public void onCommand(Response response) {
            if (subwindow != null && subwindow.isVisible()) {
                subwindow.setOutput(response);
            }

            if (table != null) {
                table.onCommand(response);
            }
        }

        @Override
        public String getName() {
            return HadoopModule.MODULE_NAME;
        }

        private List<Agent> getLxcAgents() {
            List<Agent> list = new ArrayList<Agent>();
//            if (AppData.getSelectedAgentList() != null) {
            if (MgmtApplication.getSelectedAgents() != null && !MgmtApplication.getSelectedAgents().isEmpty()) {
                for (Agent agent : MgmtApplication.getSelectedAgents()) {
                    if (agent.isIsLXC()) {
                        list.add(agent);
                    }
                }
            }

            return list;
        }

    }

    @Override
    public String getName() {
        return HadoopModule.MODULE_NAME;
    }

    @Override
    public Component createComponent() {
        component = new ModuleComponent();
        return component;
    }

    public void setModuleService(ModuleService service) {
        LOG.log(Level.INFO, "{0} registering with ModuleService", MODULE_NAME);
        service.registerModule(this);
    }

    public void unsetModuleService(ModuleService service) {
        if (getCommandManager() != null) {
            getCommandManager().removeListener(component);
        }
        service.unregisterModule(this);
        LOG.log(Level.INFO, "{0} Unregistering with ModuleService", MODULE_NAME);
    }

    public static CommandManagerInterface getCommandManager() {
        // get bundle instance via the OSGi Framework Util class
        BundleContext ctx = FrameworkUtil.getBundle(HadoopModule.class).getBundleContext();
        if (ctx != null) {
            ServiceReference serviceReference = ctx.getServiceReference(CommandManagerInterface.class.getName());
            if (serviceReference != null) {
                return CommandManagerInterface.class.cast(ctx.getService(serviceReference));
            }
        }

        return null;
    }
}
