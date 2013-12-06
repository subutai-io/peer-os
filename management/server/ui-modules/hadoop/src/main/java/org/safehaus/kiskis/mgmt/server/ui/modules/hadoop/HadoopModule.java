package org.safehaus.kiskis.mgmt.server.ui.modules.hadoop;

import com.google.common.collect.EvictingQueue;
import com.google.common.collect.Queues;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.VerticalLayout;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.wizard.HadoopWizard;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;
import org.safehaus.kiskis.mgmt.server.ui.services.ModuleService;
import org.safehaus.kiskis.mgmt.server.ui.util.AppData;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.CommandListener;
import org.safehaus.kiskis.mgmt.shared.protocol.settings.Common;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HadoopModule implements Module {

    private static final Logger LOG = Logger.getLogger(HadoopModule.class.getName());
    public static final String MODULE_NAME = "HadoopModule";

    private static ModuleComponent component;

    public static class ModuleComponent extends CustomComponent implements
            CommandListener {

        private final Button buttonInstallWizard;
        private HadoopWizard subwindow;
        //messages queue
        private final EvictingQueue<Response> queue = EvictingQueue.create(Common.MAX_MODULE_MESSAGE_QUEUE_LENGTH);
        private final Queue<Response> messagesQueue = Queues.synchronizedQueue(queue);
        private final ExecutorService executor = Executors.newSingleThreadExecutor();
        //messages queue
        public ModuleComponent() {

            VerticalLayout verticalLayout = new VerticalLayout();
            verticalLayout.setSpacing(true);

            buttonInstallWizard = new Button("HadoopModule Installation Wizard");
            buttonInstallWizard.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    if(getLxcAgents().size() > 0){
                        subwindow = new HadoopWizard(getLxcAgents());
                        getApplication().getMainWindow().addWindow(subwindow);
                    }
                }
            });
            verticalLayout.addComponent(buttonInstallWizard);

            setCompositionRoot(verticalLayout);
            HadoopModule.getCommandManager().addListener(this);

            addListener(new ComponentDetachListener() {
                @Override
                public void componentDetachedFromContainer(ComponentDetachEvent event) {
                    System.out.println("Lxc is detached");
                    executor.shutdown();
                }
            });

            //messages queue
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    while (!Thread.interrupted()) {
                        try {
                            processAllResponses();
                            Thread.sleep(500);
                        } catch (Exception ex) {
                            LOG.log(Level.SEVERE, "Error in queue executor", ex);
                        }
                    }
                }
            });
            //messages queue
        }

        @Override
        public void onCommand(Response response) {
            messagesQueue.add(response);
        }

        //messages queue
        private void processAllResponses() {
            if (!messagesQueue.isEmpty()) {
                if(subwindow != null
                        && subwindow.isVisible()){
                    Response[] responses = messagesQueue.toArray(new Response[messagesQueue.size()]);
                    messagesQueue.clear();
                    for (Response response : responses) {
                        subwindow.setOutput(response);
                    }
                }
            }
        }
        //messages queue

        @Override
        public String getName() {
            return HadoopModule.MODULE_NAME;
        }

        private List<Agent> getLxcAgents() {
            List<Agent> list =  new ArrayList<Agent>();
            if (AppData.getSelectedAgentList() != null) {
                for(Agent agent : AppData.getSelectedAgentList()) {
                    if(agent.isIsLXC()){
                        list.add(agent);
                    }
                }
            }

            return list;
        }
    }

    @Override
    public String getName() {
        return HadoopModule.MODULE_NAME;
    }

    @Override
    public Component createComponent() {
        component = new ModuleComponent();
        return component;
    }

    public void setModuleService(ModuleService service) {
        System.out.println("HadoopModule: registering with ModuleService");
        service.registerModule(this);
    }

    public void unsetModuleService(ModuleService service) {
        if (getCommandManager() != null) {
            getCommandManager().removeListener(component);
        }
        service.unregisterModule(this);
    }

    public static CommandManagerInterface getCommandManager() {
        // get bundle instance via the OSGi Framework Util class
        BundleContext ctx = FrameworkUtil.getBundle(HadoopModule.class).getBundleContext();
        if (ctx != null) {
            ServiceReference serviceReference = ctx.getServiceReference(CommandManagerInterface.class.getName());
            if (serviceReference != null) {
                return CommandManagerInterface.class.cast(ctx.getService(serviceReference));
            }
        }

        return null;
    }
}
