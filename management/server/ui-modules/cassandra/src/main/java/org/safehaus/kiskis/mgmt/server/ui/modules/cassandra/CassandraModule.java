package org.safehaus.kiskis.mgmt.server.ui.modules.cassandra;

import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.wizzard.CassandraManage;
import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.wizzard.CassandraWizard;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;
import org.safehaus.kiskis.mgmt.server.ui.services.ModuleService;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.ServiceLocator;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.CommandListener;

public class CassandraModule implements Module {

    private static final Logger LOG = Logger.getLogger(CassandraModule.class.getName());

    public static final String MODULE_NAME = "CassandraModule";
    private static ModuleComponent component;

    public static class ModuleComponent extends CustomComponent implements
            CommandListener {

//        private final Button buttonInstallWizard;
//        private final Button getClusters;
        private CassandraWizard cassandraWizard;
//        private CassandraTable cassandraTable;
//        private final TextArea terminal;

        public ModuleComponent() {

            VerticalLayout verticalLayout = new VerticalLayout();
            verticalLayout.setSpacing(true);
            verticalLayout.setSizeFull();

            // Create table
//            cassandraTable = new CassandraTable(getCommandManager(), this);
//            cassandraTable.setPageLength(6);
            TabSheet cassandraSheet = new TabSheet();
            cassandraSheet.setStyleName(Runo.TABSHEET_SMALL);
            cassandraSheet.setSizeFull();

            cassandraSheet.addTab(new CassandraWizard(this).getContent(), "Install!");
            cassandraSheet.addTab(new CassandraManage(this).getContent(), "Manage");
//            mongoSheet.addTab(new MongoManager().getContent(), "Manage");

            verticalLayout.addComponent(cassandraSheet);

//            buttonInstallWizard = new Button("Cassandra Cluster Installation Wizard");
//            buttonInstallWizard.addListener(new Button.ClickListener() {
//                @Override
//                public void buttonClick(Button.ClickEvent event) {
//                    if (getLxcAgents().size() > 0) {
//                        cassandraWizard = new CassandraWizard(getLxcAgents());
//                        getApplication().getMainWindow().addWindow(cassandraWizard);
//                    }
//                }
//
//                
//            });
//            verticalLayout.addComponent(buttonInstallWizard);
//            getClusters = new Button("Get Cassandra clusters" + System.currentTimeMillis());
//            getClusters.addListener(new Button.ClickListener() {
//                @Override
//                public void buttonClick(Button.ClickEvent event) {
//                    cassandraTable.refreshDatasource();
//                }
//            });
//
//            verticalLayout.addComponent(getClusters);
//            verticalLayout.addComponent(cassandraTable);
//            terminal = new TextArea();
//            terminal.setRows(10);
//            terminal.setColumns(65);
//            terminal.setImmediate(true);
//            terminal.setWordwrap(true);
//            verticalLayout.addComponent(terminal);
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
            try {
                if (response != null && response.getSource().equals(MODULE_NAME)) {
                    StringBuilder sb = new StringBuilder();
//                    sb.append(terminal.getValue());
//                    sb.append(response.getStdOut());
//                    terminal.setValue(sb);
//                    terminal.setCursorPosition(sb.length() - 1);
                    if (cassandraWizard != null
                            && response.getTaskUuid().toString().equals(cassandraWizard.getTask().getUuid().toString())) {
                        cassandraWizard.setOutput(response);
                    }

//                    else if (cassandraTable != null & cassandraTable.getTask() != null 
//                            & response.getTaskUuid().toString().equals(cassandraTable.getTask().getUuid().toString())) {
//                        cassandraTable.setOutput(response);
//                    } else if (cassandraTable != null & cassandraTable.getNodesWindow().getTask() != null 
//                            & response.getTaskUuid().toString().equals(cassandraTable.getNodesWindow().getTask().getUuid().toString())) {
//                        cassandraTable.getNodesWindow().setOutput(response);
//                    }
                }
            } catch (Exception ex) {
                LOG.log(Level.SEVERE, "Error in onCommand", ex);
            }
        }

        @Override
        public String getName() {
            return CassandraModule.MODULE_NAME;
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
