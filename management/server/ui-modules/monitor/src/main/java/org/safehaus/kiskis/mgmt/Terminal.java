package org.safehaus.kiskis.mgmt;

import com.vaadin.ui.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;
import org.safehaus.kiskis.mgmt.server.ui.services.ModuleService;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.CommandListener;

public class Terminal implements Module {

    private static final Logger LOG = Logger.getLogger(Terminal.class.getName());

    public static final String MODULE_NAME = "Monitor";
    private ModuleComponent component;

    public static class ModuleComponent extends CustomComponent implements
            CommandListener {

        private TextArea textAreaOutput;

        public ModuleComponent() {

            VerticalLayout verticalLayout = new JSApi();
            verticalLayout.setSpacing(true);

            setCompositionRoot(verticalLayout);

        }

        @Override
        public synchronized void onCommand(Response response) {
            System.out.println("");
            System.out.println(response);
            System.out.println("");

            StringBuilder output = new StringBuilder();
            output.append(textAreaOutput.getValue());

            if (response.getStdErr() != null && response.getStdErr().trim().length() != 0) {
                output.append("\n");
                output.append("ERROR\n");
                output.append(response.getStdErr().trim());
                output.append("\n");
            }
            if (response.getStdOut() != null && response.getStdOut().trim().length() != 0) {
                output.append("\n");
                output.append("OK\n");
                output.append(response.getStdOut().trim());
                output.append("\n");
            }
            textAreaOutput.setValue(output);
            textAreaOutput.setCursorPosition(output.length() - 1);
        }

        @Override
        public synchronized String getName() {
            return MODULE_NAME;
        }

    }

    @Override
    public String getName() {
        return MODULE_NAME;
    }

    @Override
    public Component createComponent() {
        try {
            component = new ModuleComponent();
            ServiceLocator.getService(CommandManagerInterface.class).addListener(component);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error in createComponent", e);
        }
        return component;
    }

    @Override
    public void dispose() {
        try {
            ServiceLocator.getService(CommandManagerInterface.class).removeListener(component);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error in dispose", e);
        }
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

}
