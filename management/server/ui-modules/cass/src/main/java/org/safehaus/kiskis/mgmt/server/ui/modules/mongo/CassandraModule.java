package org.safehaus.kiskis.mgmt.server.ui.modules.mongo;

import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.wizard.CassandraWizard;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.management.CassandraManager;
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

public class CassandraModule implements Module {

    public static final String MODULE_NAME = "Cass";

    private static final Logger LOG = Logger.getLogger(CassandraModule.class.getName());
    private BundleContext context;

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

            mongoSheet.addTab(new CassandraWizard().getContent(), "Install!");
            mongoSheet.addTab(new CassandraManager().getContent(), "Manage");

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
            
            System.out.println("CASS " + response);
            
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
