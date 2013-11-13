package org.safehaus.kiskis.mgmt.server.ui.modules;


import com.vaadin.ui.*;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;
import org.safehaus.kiskis.mgmt.server.ui.services.ModuleService;
import org.safehaus.kiskis.mgmt.server.ui.util.AppData;
import org.safehaus.kiskis.mgmt.shared.protocol.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.CommandJson;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.CommandListener;

public class Terminal implements Module {

    private ModuleService service;
    private CommandManagerInterface commandManagerService;

    public class ModuleComponent extends CustomComponent implements
            Button.ClickListener, CommandListener {

        private TextArea textAreaCommand;
        private TextArea textAreaOutput;
        private Button buttonSend;

        public ModuleComponent() {

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
//            textAreaOutput.setReadOnly(true);
            textAreaOutput.setRows(20);
            textAreaOutput.setColumns(100);
            textAreaOutput.setImmediate(true);
            textAreaOutput.setWordwrap(false);

            verticalLayout.addComponent(labelOutput);
            verticalLayout.addComponent(textAreaOutput);

            setCompositionRoot(verticalLayout);

            Terminal.this.commandManagerService.addListener(this);
        }

        public void buttonClick(Button.ClickEvent event) {
            for (String agent : AppData.getAgentList()) {
                Request r = CommandJson.getRequest(textAreaCommand.getValue().toString());
                r.setUuid(agent);
                r.setSource("Terminal");

                Command command = new Command(r);
                boolean isOk = Terminal.this.commandManagerService.executeCommand(command);
            }
        }

        @Override
        public synchronized void outputCommand(Response response) {
            System.out.println("");
            System.out.println(response);
            System.out.println("");

//            textAreaOutput.setReadOnly(false);

            StringBuilder output = new StringBuilder();
            output.append(textAreaOutput.getValue());

            if (response.getStdErr() != null && response.getStdErr().trim().length() != 0) {
                output.append(response.getStdErr().trim());
            }
            if (response.getStdOut() != null && response.getStdOut().trim().length() != 0) {
                output.append(response.getStdOut().trim());
            }
            textAreaOutput.setValue(output);
//            textAreaOutput.setReadOnly(true);
        }

        @Override
        public synchronized String getName() {
            return Terminal.this.getName();
        }
    }

    public String getName() {
        return "Terminal";
    }

    public Component createComponent() {
        return new ModuleComponent();
    }

    public void setModuleService(ModuleService service) {
        System.out.println("Terminal: registering with ModuleService");
        this.service = service;
        this.service.registerModule(this);
    }

    //public void unsetModuleService(ModuleService service) {
    public void unsetModuleService(ModuleService service) {
        this.service.unregisterModule(this);
    }

    public void setCommandManagerService(CommandManagerInterface commandManagerService) {
        this.commandManagerService = commandManagerService;
    }
}