package org.safehaus.kiskis.mgmt.server.ui.modules.cassandra;

import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.wizard.CassandraWizard;
import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.management.CassandraManager;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.Runo;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;
import org.safehaus.kiskis.mgmt.server.ui.services.ModuleService;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.ServiceLocator;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.CommandListener;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CassandraModule implements Module {

    public static final String MODULE_NAME = "Cassandra";
    private static final Logger LOG = Logger.getLogger(CassandraModule.class.getName());
    private static ModuleComponent component;

    public static class ModuleComponent extends CustomComponent implements
            CommandListener {

        CassandraWizard cassandraWizard;
        CassandraManager cassandraManager;

        public ModuleComponent() {

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

//        private List<Agent> getLxcAgents() {
//            List<Agent> list = new ArrayList<Agent>();
////                    if (AppData.getSelectedAgentList() != null) {
//            if (MgmtApplication.getSelectedAgents() != null && !MgmtApplication.getSelectedAgents().isEmpty()) {
//                for (Agent agent : MgmtApplication.getSelectedAgents()) {
//                    if (agent.isIsLXC()) {
//                        list.add(agent);
//                    }
//                }
//            }
//
//            return list;
//        }
        @Override
        public void onCommand(Response response) {
            if (cassandraWizard != null) {
                cassandraWizard.setOutput(response);
            }
            if (cassandraManager != null) {
                cassandraManager.setOutput(response);
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
        return CassandraModule.MODULE_NAME;
    }

    @Override
    public Component createComponent() {
        try {
            component = new ModuleComponent();
            ServiceLocator.getService(CommandManagerInterface.class).addListener(component);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error in createComponent", e);
        }
        return component;
    }

    @Override
    public void dispose() {
        try {
            ServiceLocator.getService(CommandManagerInterface.class).removeListener(component);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error in dispose", e);
        }
    }

    public void setModuleService(ModuleService service) {
        if (service != null) {
            LOG.log(Level.INFO, "{0} registering with ModuleService", MODULE_NAME);
            service.registerModule(this);
        }
    }

    public void unsetModuleService(ModuleService service) {
        if (service != null) {
            service.unregisterModule(this);
            LOG.log(Level.INFO, "{0} Unregistering with ModuleService", MODULE_NAME);
        }
    }

}
