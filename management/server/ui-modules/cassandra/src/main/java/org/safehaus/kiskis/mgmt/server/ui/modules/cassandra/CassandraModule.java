package org.safehaus.kiskis.mgmt.server.ui.modules.cassandra;

import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.wizzard.CassandraManage;
import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.wizzard.CassandraWizard;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;
import org.safehaus.kiskis.mgmt.server.ui.services.ModuleService;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
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
        private CassandraManage cassandraManage;
//        private CassandraTable cassandraTable;
//        private final TextArea terminal;

        public ModuleComponent(final CommandManagerInterface commandManagerInterface) {

            VerticalLayout verticalLayout = new VerticalLayout();
            verticalLayout.setSpacing(true);
            verticalLayout.setSizeFull();

            // Create table
//            cassandraTable = new CassandraTable(getCommandManager(), this);
//            cassandraTable.setPageLength(6);
            TabSheet cassandraSheet = new TabSheet();
            cassandraSheet.setStyleName(Runo.TABSHEET_SMALL);
            cassandraSheet.setSizeFull();

            cassandraWizard = new CassandraWizard();
            cassandraManage = new CassandraManage();
            cassandraSheet.addTab(cassandraWizard.getContent(), "Install");
            cassandraSheet.addTab(cassandraManage.getContent(), "Manage");
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

    }

    @Override
    public String getName() {
        return CassandraModule.MODULE_NAME;
    }

    @Override
    public Component createComponent() {
        CommandManagerInterface commandManagerInterface = getCommandManager();
        component = new ModuleComponent(commandManagerInterface);
        commandManagerInterface.addListener(component);

        return component;
    }

    public void setModuleService(ModuleService service) {
        LOG.log(Level.INFO, "CassandraModule: registering with ModuleService");
        service.registerModule(this);
    }

    public void unsetModuleService(ModuleService service) {
        if (getCommandManager() != null) {
            getCommandManager().removeListener(component);
            service.unregisterModule(this);
        }
    }

    public static CommandManagerInterface getCommandManager() {
        BundleContext ctx = FrameworkUtil.getBundle(CassandraModule.class).getBundleContext();
        if (ctx != null) {
            ServiceReference serviceReference = ctx.getServiceReference(CommandManagerInterface.class.getName());
            if (serviceReference != null) {
                return CommandManagerInterface.class.cast(ctx.getService(serviceReference));
            }
        }

        return null;
    }
}
