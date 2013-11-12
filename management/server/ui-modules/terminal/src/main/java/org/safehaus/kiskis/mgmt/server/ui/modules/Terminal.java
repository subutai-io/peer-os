package org.safehaus.kiskis.mgmt.server.ui.modules;


import com.vaadin.ui.*;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;
import org.safehaus.kiskis.mgmt.server.ui.services.ModuleService;
import org.safehaus.kiskis.mgmt.server.ui.util.AppData;
import org.safehaus.kiskis.mgmt.shared.protocol.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.CommandJson;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;

public class Terminal implements Module {

    private ModuleService service;
    private CommandManagerInterface commandManagerService;

    public class ModuleComponent extends CustomComponent implements
            Button.ClickListener {

        private TextArea textAreaCommand;
        private TextArea textAreaOutput;
        private Button buttonSend;

        public ModuleComponent() {
            VerticalLayout verticalLayout = new VerticalLayout();
            verticalLayout.setSpacing(true);

            Label labelText = new Label("Enter command:");
            textAreaCommand = new TextArea();
            textAreaCommand.setRows(20);
            textAreaCommand.setColumns(60);
            textAreaCommand.setImmediate(true);
            verticalLayout.addComponent(labelText);
            verticalLayout.addComponent(textAreaCommand);

            buttonSend = new Button("Send");
            buttonSend.setDescription("Sends command to agent");
            buttonSend.addListener(this); // react to clicks
            verticalLayout.addComponent(buttonSend);

            Label labelOutput = new Label("Commands output");
            textAreaOutput = new TextArea();
            textAreaOutput.setRows(20);
            textAreaOutput.setColumns(60);
            textAreaOutput.setImmediate(true);
            verticalLayout.addComponent(labelOutput);
            verticalLayout.addComponent(textAreaOutput);

            setCompositionRoot(verticalLayout);
        }

        /*
     * Shows a notification when a button is clicked.
     */
        public void buttonClick(Button.ClickEvent event) {
            String result = "";
            for (String agent : AppData.getAgentList()) {
                Request r = CommandJson.getRequest(textAreaCommand.getValue().toString());
                r.setUuid(agent);
                r.setSource(Terminal.this.getName());

                Command command = new Command(r);
                boolean isOk = Terminal.this.commandManagerService.executeCommand(command);
                result += "Agent: " + agent + " executeCommand: " + isOk + "\n";
            }

            textAreaOutput.setValue(result);
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