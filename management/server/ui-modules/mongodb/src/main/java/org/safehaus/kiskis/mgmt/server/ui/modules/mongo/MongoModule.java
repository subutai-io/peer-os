package org.safehaus.kiskis.mgmt.server.ui.modules.mongo;

import com.vaadin.terminal.ClassResource;
import com.vaadin.terminal.FileResource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;
import org.safehaus.kiskis.mgmt.server.ui.services.ModuleService;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.CommandListener;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MongoModule implements Module {
//    private ModuleService service;

    private static final Logger LOG = Logger.getLogger(MongoModule.class.getName());

    private BundleContext context;
    private static final String MODULE_NAME = "MongoDB";

    public static class ModuleComponent extends CustomComponent implements
            CommandListener {

        private BundleContext context;

        public ModuleComponent(BundleContext context) {
            this.context = context;
            VerticalLayout verticalLayout = new VerticalLayout();
            verticalLayout.setSpacing(true);
            verticalLayout.setSizeFull();

            ThemeResource resource = new ThemeResource("icons/32/document.png");
            Embedded image = new Embedded("", resource);
            verticalLayout.addComponent(image);

            Button button = new Button("OK");
            verticalLayout.addComponent(button);

            setCompositionRoot(verticalLayout);

            try {
                getCommandManager().addListener(this);
            } catch (Exception ex) {
                LOG.log(Level.SEVERE, "Error in addListener", ex);
            }
        }

        @Override
        public void onCommand(Response response) {
        }

        @Override
        public synchronized String getName() {
            return MODULE_NAME;
        }

        private CommandManagerInterface getCommandManager() {
            ServiceReference reference = context
                    .getServiceReference(CommandManagerInterface.class.getName());
            return (CommandManagerInterface) context.getService(reference);
        }
    }

    @Override
    public String getName() {
        return MODULE_NAME;
    }

    @Override
    public Component createComponent() {
        return new ModuleComponent(context);
    }

    public void setModuleService(ModuleService service) {
        if (service != null) {
            System.out.println(MODULE_NAME + " : registering with ModuleService");
//            this.service = service;
            service.registerModule(this);
        }
    }

    public void unsetModuleService(ModuleService service) {
        if (service != null) {
            service.unregisterModule(this);
            System.out.println(MODULE_NAME + " : Unregistering with ModuleService");
        }
    }

    public void setContext(BundleContext context) {
        this.context = context;
    }
}
