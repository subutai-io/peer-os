package org.safehaus.kiskis.mgmt.server.ui.modules;


import com.vaadin.ui.*;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;
import org.safehaus.kiskis.mgmt.server.ui.services.ModuleService;
import org.safehaus.kiskis.mgmt.server.ui.util.AppData;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;

public class Terminal implements Module {

    private ModuleService service;
    private CommandManagerInterface commandManagerService;

    public static class ModuleComponent extends CustomComponent implements
            Button.ClickListener {

        private TextField textFieldCommand;
        private TextArea textAreaOutput;
        private Button buttonSend;

        public ModuleComponent() {
            VerticalLayout verticalLayout = new VerticalLayout();
            verticalLayout.setSpacing(true);

            textFieldCommand = new TextField("Enter command:");
            verticalLayout.addComponent(textFieldCommand);

            Label labelOutput = new Label("Commands output");
            textAreaOutput = new TextArea();
            textAreaOutput.setRows(20);
            textAreaOutput.setColumns(20);
            textAreaOutput.setImmediate(true);
            verticalLayout.addComponent(labelOutput);
            verticalLayout.addComponent(textAreaOutput);

            buttonSend = new Button("Send");
            buttonSend.setDescription("Sends command to agent");
            buttonSend.addListener(this); // react to clicks
            verticalLayout.addComponent(buttonSend);

            setCompositionRoot(verticalLayout);
        }

        /*
     * Shows a notification when a button is clicked.
     */
        public void buttonClick(Button.ClickEvent event) {
            getWindow().showNotification(textFieldCommand.getValue().toString());
            textAreaOutput.setValue(AppData.getAgentList());
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
        System.out.println("Terminal: unregistering with ModuleService");
        this.service.unregisterModule(this);
    }

    public void setCommandManagerService(CommandManagerInterface commandManagerService) {
        if (commandManagerService != null) {
            System.out.println("Terminal: commandManagerService injected");
        }
        this.commandManagerService = commandManagerService;
    }
}