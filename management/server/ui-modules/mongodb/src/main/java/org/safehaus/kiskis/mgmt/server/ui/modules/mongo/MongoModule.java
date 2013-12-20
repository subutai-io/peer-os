package org.safehaus.kiskis.mgmt.server.ui.modules.mongo;

import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.wizard.MongoWizard;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.management.MongoManager;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.Runo;
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

    private static final Logger LOG = Logger.getLogger(MongoModule.class.getName());

    private BundleContext context;
    private static final String MODULE_NAME = "MongoDB";

    public static class ModuleComponent extends CustomComponent implements
            CommandListener {

        private final BundleContext context;

        public ModuleComponent(BundleContext context) {
            this.context = context;

//            ThemeResource resource = new ThemeResource("icons/32/document.png");
//            Embedded image = new Embedded("", resource);
//            verticalLayout.addComponent(image);
            VerticalLayout verticalLayout = new VerticalLayout();
            verticalLayout.setSpacing(true);
            verticalLayout.setSizeFull();

            TabSheet mongoSheet = new TabSheet();
            mongoSheet.setStyleName(Runo.TABSHEET_SMALL);
            mongoSheet.setSizeFull();

            mongoSheet.addTab(new MongoWizard().getContent(), "Install");
            mongoSheet.addTab(new MongoManager().getContent(), "Manage");

            verticalLayout.addComponent(mongoSheet);

            setCompositionRoot(verticalLayout);

            try {
                LOG.log(Level.INFO, "{0}: Registering with Command Manager", MODULE_NAME);
                getCommandManager().addListener(this);
            } catch (Exception ex) {
                LOG.log(Level.SEVERE, "Error in addListener", ex);
            }
        }

        @Override
        public void onCommand(Response response) {
        }

        @Override
        public String getName() {
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
            LOG.log(Level.INFO, "{0}: registering with ModuleService", MODULE_NAME);
            service.registerModule(this);
        }
    }

    public void unsetModuleService(ModuleService service) {
        if (service != null) {
            service.unregisterModule(this);
            LOG.log(Level.INFO, "{0}: Unregistering with ModuleService", MODULE_NAME);
        }
    }

    public void setContext(BundleContext context) {
        this.context = context;
    }
}
