package org.safehaus.kiskis.mgmt.server.ui.modules.cassandra;

import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.wizard.CassandraWizard;
import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.management.CassandraManager;
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
import org.osgi.framework.FrameworkUtil;

public class CassandraModule implements Module {

    public static final String MODULE_NAME = "Cassandra";

    private static final Logger LOG = Logger.getLogger(CassandraModule.class.getName());
//    private BundleContext context;
    private static ModuleComponent component;

    public static class ModuleComponent extends CustomComponent implements
            CommandListener {

//        private Queue<ResponseListener> listeners = new ConcurrentLinkedQueue<ResponseListener>();
//        private final BundleContext context;
        CassandraWizard cassandraWizard;
        CassandraManager cassandraManager;

        public ModuleComponent() {
//            this.context = context;

//            ThemeResource resource = new ThemeResource("icons/32/document.png");
//            Embedded image = new Embedded("", resource);
//            verticalLayout.addComponent(image);
            VerticalLayout verticalLayout = new VerticalLayout();
            verticalLayout.setSpacing(true);
            verticalLayout.setSizeFull();

            TabSheet sheet = new TabSheet();
            sheet.setStyleName(Runo.TABSHEET_SMALL);
            sheet.setSizeFull();

            cassandraWizard = new CassandraWizard();
            cassandraManager = new CassandraManager();
            sheet.addTab(cassandraWizard.getContent(), "Install!");
            sheet.addTab(cassandraManager.getContent(), "Manage");

            verticalLayout.addComponent(sheet);

            setCompositionRoot(verticalLayout);
            CassandraModule.getCommandManager().addListener(this);
        }

        @Override
        public void onCommand(Response response) {
            System.out.println("555 " + response.getStdOut());
            LOG.log(Level.INFO, "555 ", response);
            cassandraWizard.setOutput(response);
//            for (Iterator<ResponseListener> it = listeners.iterator(); it.hasNext();) {
//                ResponseListener l = it.next();
//                try {
//                    if (l != null) {
//                        l.onResponse(response);
//                    } else {
//                        it.remove();
//                    }
//                } catch (Exception e) {
//                }
//            }

        }

        @Override
        public String getName() {
            return MODULE_NAME;
        }

//        private CommandManagerInterface getCommandManager() {
//            ServiceReference reference = context
//                    .getServiceReference(CommandManagerInterface.class.getName());
//            return (CommandManagerInterface) context.getService(reference);
//        }
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

//        @Override
//        public void registerListener(ResponseListener listener) {
//            listeners.add(listener);
//        }
    }

    @Override
    public String getName() {
        return CassandraModule.MODULE_NAME;
    }

    @Override
    public Component createComponent() {
        component = new ModuleComponent();
        return component;
    }

    public void setModuleService(ModuleService service) {
        LOG.log(Level.INFO, "{0} registering with ModuleService", MODULE_NAME);
        service.registerModule(this);
    }

    public void unsetModuleService(ModuleService service) {
        if (getCommandManager() != null) {
            getCommandManager().removeListener(component);
        }
        service.unregisterModule(this);
        LOG.log(Level.INFO, "{0} Unregistering with ModuleService", MODULE_NAME);
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
