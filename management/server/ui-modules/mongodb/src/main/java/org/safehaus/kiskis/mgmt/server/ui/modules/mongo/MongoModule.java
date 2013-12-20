package org.safehaus.kiskis.mgmt.server.ui.modules.mongo;

import com.vaadin.terminal.Sizeable;
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
    private static final int MAX_STEPS = 2;

    public static class ModuleComponent extends CustomComponent implements
            CommandListener {

        private BundleContext context;
        private final ProgressIndicator progressBar;
        private final VerticalLayout verticalLayout;

        int step = 1;

        public ModuleComponent(BundleContext context) {
            this.context = context;

//            ThemeResource resource = new ThemeResource("icons/32/document.png");
//            Embedded image = new Embedded("", resource);
//            verticalLayout.addComponent(image);
            VerticalLayout verticalLayoutRoot = new VerticalLayout();
            verticalLayoutRoot.setSpacing(true);
            verticalLayoutRoot.setWidth(90, Sizeable.UNITS_PERCENTAGE);
            verticalLayoutRoot.setHeight(100, Sizeable.UNITS_PERCENTAGE);

            GridLayout gridLayout = new GridLayout(1, 15);
            gridLayout.setSpacing(true);
//            gridLayout.setMargin(false, true, true, true);
            gridLayout.setHeight(100, Sizeable.UNITS_PERCENTAGE);
            gridLayout.setWidth(900, Sizeable.UNITS_PIXELS);

            progressBar = new ProgressIndicator();
            progressBar.setIndeterminate(false);
            progressBar.setEnabled(false);
            progressBar.setValue(0f);
            progressBar.setWidth(100, Sizeable.UNITS_PERCENTAGE);
            gridLayout.addComponent(progressBar, 0, 0);
            gridLayout.setComponentAlignment(progressBar, Alignment.MIDDLE_CENTER);

            verticalLayout = new VerticalLayout();
            verticalLayout.setSpacing(true);
            verticalLayout.setWidth(100, Sizeable.UNITS_PERCENTAGE);
            verticalLayout.setHeight(100, Sizeable.UNITS_PERCENTAGE);
            gridLayout.addComponent(verticalLayout, 0, 1, 0, 14);
            gridLayout.setComponentAlignment(verticalLayout, Alignment.MIDDLE_CENTER);

            verticalLayoutRoot.addComponent(gridLayout);
            verticalLayoutRoot.setMargin(true);
            verticalLayoutRoot.setComponentAlignment(gridLayout, Alignment.MIDDLE_CENTER);

            putForm();

            setCompositionRoot(verticalLayoutRoot);

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

        public void showNext() {
            step++;
            putForm();
        }

        public void showBack() {
            step--;
            putForm();
        }

        private void putForm() {
            verticalLayout.removeAllComponents();
            switch (step) {
                case 1: {
                    progressBar.setValue(0f);
                    verticalLayout.addComponent(new Step1(this));
                    break;
                }
                case 2: {
                    progressBar.setValue((float) (step - 1) / MAX_STEPS);
                    verticalLayout.addComponent(new Step2(this));
                    break;
                }
                default: {
                    break;
                }
            }
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
