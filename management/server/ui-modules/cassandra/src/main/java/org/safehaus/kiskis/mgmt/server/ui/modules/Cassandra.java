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

import java.util.Set;

public class Cassandra implements Module {

    public static final String MODULE_NAME = "Cassandra";
    private BundleContext context;
    private static ModuleComponent component;

    public static class ModuleComponent extends CustomComponent implements
            Button.ClickListener, CommandListener {

        private Button buttonInstallWizard;
        private Set<String> agents;
        private Window subwindow;
        private CommandManagerInterface commandManagerInterface;

        public ModuleComponent(CommandManagerInterface commandManagerInterface) {
            this.commandManagerInterface = commandManagerInterface;

            subwindow = new CassandraWizard();
            VerticalLayout verticalLayout = new VerticalLayout();
            verticalLayout.setSpacing(true);

            buttonInstallWizard = new Button("Cassandra Installation Wizard");
            buttonInstallWizard.addListener(this); // react to clicks
            verticalLayout.addComponent(buttonInstallWizard);

            setCompositionRoot(verticalLayout);
        }

        public void buttonClick(Button.ClickEvent event) {
            getApplication().getMainWindow().addWindow(subwindow);
        }

        @Override
        public void outputCommand(Response response) {
            try {
                System.out.println(response);
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

    public String getName() {
        return Cassandra.MODULE_NAME;
    }

    public Component createComponent() {
        CommandManagerInterface commandManagerInterface = getService();
        component = new ModuleComponent(commandManagerInterface);
        commandManagerInterface.addListener(component);

        return component;
//        return new ModuleComponent(context);
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

    private CommandManagerInterface getCommandManager() {
        if (context != null) {
            ServiceReference reference = context
                    .getServiceReference(CommandManagerInterface.class.getName());
            if (reference != null) {
                return (CommandManagerInterface) context.getService(reference);
            }
        }

        return null;
    }

    public CommandManagerInterface getService() {
        // get bundle instance via the OSGi Framework Util class
        BundleContext ctx = FrameworkUtil.getBundle(Cassandra.class).getBundleContext();
        ServiceReference serviceReference = ctx.getServiceReference(CommandManagerInterface.class.getName());
        return CommandManagerInterface.class.cast(ctx.getService(serviceReference));
    }
}