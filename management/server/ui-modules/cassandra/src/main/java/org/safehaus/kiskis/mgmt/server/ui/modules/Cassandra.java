package org.safehaus.kiskis.mgmt.server.ui.modules;

import com.vaadin.ui.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.safehaus.kiskis.mgmt.server.ui.modules.wizzard.CassandraWizard;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;
import org.safehaus.kiskis.mgmt.server.ui.services.ModuleService;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.CommandListener;

public class Cassandra implements Module {

    public static final String MODULE_NAME = "Cassandra";
    private BundleContext context;

    private static ModuleComponent component;

    public static class ModuleComponent extends CustomComponent implements
            CommandListener {

        private final Button buttonInstallWizard;
        private CassandraWizard subwindow;

        public ModuleComponent(CommandManagerInterface commandManagerInterface) {

            VerticalLayout verticalLayout = new VerticalLayout();
            verticalLayout.setSpacing(true);

            buttonInstallWizard = new Button("Cassandra Installation Wizard");
            buttonInstallWizard.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    subwindow = new CassandraWizard();
                    getApplication().getMainWindow().addWindow(subwindow);
                }
            });
            verticalLayout.addComponent(buttonInstallWizard);

            setCompositionRoot(verticalLayout);
        }

        @Override
        public void outputCommand(Response response) {
            try {
                if(response != null && subwindow != null && subwindow.isVisible()){
                    subwindow.setOutput(response);
                }
            } catch (Exception ex) {
                System.out.println("outputCommand event Exception");
                ex.printStackTrace();
            }

        }

        @Override
        public String getName() {
            return Cassandra.MODULE_NAME;
        }
    }

    @Override
    public String getName() {
        return Cassandra.MODULE_NAME;
    }

    @Override
    public Component createComponent() {
        CommandManagerInterface commandManagerInterface = getCommandManager();
        component = new ModuleComponent(commandManagerInterface);
        commandManagerInterface.addListener(component);

        return component;
    }

    public void setModuleService(ModuleService service) {
        System.out.println("Cassandra: registering with ModuleService");
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
        BundleContext ctx = FrameworkUtil.getBundle(Cassandra.class).getBundleContext();
        if (ctx != null) {
            ServiceReference serviceReference = ctx.getServiceReference(CommandManagerInterface.class.getName());
            if (serviceReference != null) {
                return CommandManagerInterface.class.cast(ctx.getService(serviceReference));
            }
        }

        return null;
    }
}
