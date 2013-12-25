package org.safehaus.kiskis.mgmt.server.ui.modules.cassandra;

import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.wizard.CassandraWizard;
import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.management.CassandraManager;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.Runo;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;
import org.safehaus.kiskis.mgmt.server.ui.services.ModuleService;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.CommandListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ResponseListener;

public class CassandraModule implements Module {

    public static final String MODULE_NAME = "Cassandra";

    private static final Logger LOG = Logger.getLogger(CassandraModule.class.getName());
    private BundleContext context;

    public static class ModuleComponent extends CustomComponent implements
            CommandListener, ResponseNotifier {

        private Queue<ResponseListener> listeners = new ConcurrentLinkedQueue<ResponseListener>();
        private final BundleContext context;

        public ModuleComponent(BundleContext context) {
            this.context = context;

//            ThemeResource resource = new ThemeResource("icons/32/document.png");
//            Embedded image = new Embedded("", resource);
//            verticalLayout.addComponent(image);
            VerticalLayout verticalLayout = new VerticalLayout();
            verticalLayout.setSpacing(true);
            verticalLayout.setSizeFull();

            TabSheet sheet = new TabSheet();
            sheet.setStyleName(Runo.TABSHEET_SMALL);
            sheet.setSizeFull();

            sheet.addTab(new CassandraWizard(this).getContent(), "Install!");
            sheet.addTab(new CassandraManager().getContent(), "Manage");

            verticalLayout.addComponent(sheet);

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
            for (Iterator<ResponseListener> it = listeners.iterator(); it.hasNext();) {
                ResponseListener l = it.next();
                try {
                    if (l != null) {
                        l.onResponse(response);
                    } else {
                        it.remove();
                    }
                } catch (Exception e) {
                }
            }

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

        @Override
        public void registerListener(ResponseListener listener) {
            listeners.add(listener);
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
