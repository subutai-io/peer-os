package org.safehaus.kiskis.mgmt.server.ui.modules.cassandra;

import com.google.common.collect.EvictingQueue;
import com.google.common.collect.Queues;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.VerticalLayout;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.component.CassandraTable;
import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.wizzard.CassandraWizard;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;
import org.safehaus.kiskis.mgmt.server.ui.services.ModuleService;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.CommandListener;
import org.safehaus.kiskis.mgmt.shared.protocol.settings.Common;

public class CassandraModule implements Module {

    public static final String MODULE_NAME = "CassandraModule";
    private BundleContext context;

    private static ModuleComponent component;
    //messages queue
    private static final EvictingQueue<Response> queue = EvictingQueue.create(Common.MAX_MODULE_MESSAGE_QUEUE_LENGTH);
    private static final Queue messagesQueue = Queues.synchronizedQueue(queue);
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    //messages queue

    public static class ModuleComponent extends CustomComponent implements
            CommandListener {

        private final Button buttonInstallWizard;
        private final Button getClusters;
        private CassandraWizard subwindow;
        private CassandraTable cassandraTable;

        public ModuleComponent(final CommandManagerInterface commandManagerInterface) {

            VerticalLayout verticalLayout = new VerticalLayout();
            verticalLayout.setSpacing(true);
            verticalLayout.setSizeFull();

            // Create table
            cassandraTable = new CassandraTable(getCommandManager(), this);

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
                    cassandraTable.refreshDatasource();
                }
            });

            verticalLayout.addComponent(getClusters);
            verticalLayout.addComponent(cassandraTable);

            setCompositionRoot(verticalLayout);

            //messages queue
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    while (!Thread.interrupted()) {
                        try {
                            processAllResponses();
                            Thread.sleep(100);
                        } catch (Exception ex) {
                        }
                    }
                }
            });
            //messages queue
        }

        //messages queue
        private void processAllResponses() {
            if (!messagesQueue.isEmpty()) {
                Response[] responses = (Response[]) messagesQueue.toArray(new Response[0]);
                messagesQueue.clear();
                for (Response response : responses) {
                    processResponse(response);
                }
            }
        }

        private void processResponse(Response response) {
            try {
                if (response != null && response.getSource().equals(MODULE_NAME)) {
                    if (subwindow != null && subwindow.isVisible()) {
                        subwindow.setOutput(response);
                    }
                }
            } catch (Exception ex) {
                System.out.println("outputCommand event Exception");
            }
        }
        //messages queue

        @Override
        public void outputCommand(Response response) {
            //messages queue
            if (response != null && response.getSource().equals(MODULE_NAME)) //messages queue
            {
                messagesQueue.add(response);
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
            service.unregisterModule(this);
        }
    }

    public void setContext(BundleContext context) {
        this.context = context;
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
