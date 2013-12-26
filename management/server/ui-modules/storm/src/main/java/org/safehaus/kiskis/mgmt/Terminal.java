package org.safehaus.kiskis.mgmt;

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

public class Terminal implements Module {

    private static final Logger LOG = Logger.getLogger(Terminal.class.getName());

    private ModuleService service;
    private BundleContext context;
    private static final String name = "Storm";
    private Component com;

    public static class ModuleComponent extends CustomComponent implements
            Button.ClickListener, CommandListener {

        private TextArea textAreaCommand;
        private TextArea textAreaOutput;
        private Button buttonSend;
        private BundleContext context;

        public ModuleComponent(BundleContext context) {
            this.context = context;

            VerticalLayout verticalLayout = new VerticalLayout();
            verticalLayout.setSpacing(true);

            Label labelText = new Label("Enter command:");
            textAreaCommand = new TextArea();
            textAreaCommand.setRows(20);
            textAreaCommand.setColumns(100);
            textAreaCommand.setImmediate(true);
            textAreaCommand.setWordwrap(true);

            verticalLayout.addComponent(labelText);
            verticalLayout.addComponent(textAreaCommand);

            buttonSend = new Button("Send");
            buttonSend.setDescription("Sends command to agent");
            buttonSend.addListener(this); // react to clicks
            verticalLayout.addComponent(buttonSend);

            Label labelOutput = new Label("Commands output");
            textAreaOutput = new TextArea();
            textAreaOutput.setRows(20);
            textAreaOutput.setColumns(100);
            textAreaOutput.setImmediate(true);
            textAreaOutput.setWordwrap(false);

            verticalLayout.addComponent(labelOutput);
            verticalLayout.addComponent(textAreaOutput);

            setCompositionRoot(verticalLayout);

        }

        @Override
        public void buttonClick(Button.ClickEvent event) {

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
            return name;
        }

    }

    private CommandManagerInterface getCommandManager() {
        ServiceReference reference = context
                .getServiceReference(CommandManagerInterface.class.getName());
        return (CommandManagerInterface) context.getService(reference);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Component createComponent() {
        com = new ModuleComponent(context);
        try {
            getCommandManager().addListener((CommandListener) com);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error registering with command manager: {0}", e);
        }
        return com;
    }

    @Override
    public void dispose() {
        try {
            getCommandManager().removeListener((CommandListener) com);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error unregistering with command manager: {0}", e);
        }

    }

    public void setModuleService(ModuleService service) {
        System.out.println("Terminal: registering with ModuleService");
        this.service = service;
        this.service.registerModule(this);
    }

    public void unsetModuleService(ModuleService service) {
        this.service.unregisterModule(this);
    }

    public void setContext(BundleContext context) {
        this.context = context;
    }
}
