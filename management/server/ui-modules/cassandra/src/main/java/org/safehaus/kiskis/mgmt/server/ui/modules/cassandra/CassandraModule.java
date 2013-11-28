package org.safehaus.kiskis.mgmt.server.ui.modules.cassandra;

import com.vaadin.ui.*;
import java.util.List;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.wizzard.CassandraWizard;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;
import org.safehaus.kiskis.mgmt.server.ui.services.ModuleService;
import org.safehaus.kiskis.mgmt.shared.protocol.ClusterData;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.CommandListener;

public class CassandraModule implements Module {

    public static final String MODULE_NAME = "CassandraModule";
    private BundleContext context;

    private static ModuleComponent component;

    public static class ModuleComponent extends CustomComponent implements
            CommandListener {

        private final Button buttonInstallWizard;
        private final Button getClusters;
        private CassandraWizard subwindow;
        private TextArea textArea;

        public ModuleComponent(final CommandManagerInterface commandManagerInterface) {

            VerticalLayout verticalLayout = new VerticalLayout();
            verticalLayout.setSpacing(true);

            textArea = new TextArea();
            textArea.setRows(10);
            textArea.setColumns(65);
            textArea.setImmediate(true);
            textArea.setWordwrap(true);
            
            
            buttonInstallWizard = new Button("CassandraModule Installation Wizard");
            buttonInstallWizard.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    subwindow = new CassandraWizard();
                    getApplication().getMainWindow().addWindow(subwindow);
                }
            });
            verticalLayout.addComponent(buttonInstallWizard);

            getClusters = new Button("Get Cassandra clusters");
            getClusters.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    List<ClusterData> cdList = commandManagerInterface.getClusterData();
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < cdList.size(); i++) {
                        ClusterData clusterData = cdList.get(i);
                        sb.append(clusterData.getName()).append("\n");
                    }
                    textArea.setValue(sb.toString());
                }
            });

            verticalLayout.addComponent(getClusters);

            
            verticalLayout.addComponent(textArea);

            setCompositionRoot(verticalLayout);
        }

        @Override
        public void outputCommand(Response response) {
            try {
                if (response != null && subwindow != null && subwindow.isVisible()) {
                    subwindow.setOutput(response);
                }
            } catch (Exception ex) {
                System.out.println("outputCommand event Exception");
                ex.printStackTrace();
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
        System.out.println("CassandraModule: registering with ModuleService");
        service.registerModule(this);
    }

    public void unsetModuleService(ModuleService service) {
        if (getCommandManager() != null) {
            getCommandManager().removeListener(component);
        }
        service.unregisterModule(this);
    }

    public void setContext(BundleContext context) {
        this.context = context;
    }

    public static CommandManagerInterface getCommandManager() {
        // get bundle instance via the OSGi Framework Util class
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
