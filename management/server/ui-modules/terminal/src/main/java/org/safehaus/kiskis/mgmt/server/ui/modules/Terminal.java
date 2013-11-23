package org.safehaus.kiskis.mgmt.server.ui.modules;

import com.vaadin.ui.*;
import java.util.List;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;
import org.safehaus.kiskis.mgmt.server.ui.services.ModuleService;
import org.safehaus.kiskis.mgmt.server.ui.util.AppData;
import org.safehaus.kiskis.mgmt.shared.protocol.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.CommandJson;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.CommandListener;

import java.util.Set;

public class Terminal implements Module {

    public static final String MODULE_NAME = "Terminal";
    private BundleContext context;
    private static ModuleComponent component;

    public static class ModuleComponent extends CustomComponent implements
            CommandListener {

        private final TextArea textAreaCommand;
        private final TextArea textAreaOutput;
        private final Button buttonSend;
        private final Button getRequests;
        private final Button getResponses;
        private Set<String> agents;
        private final CommandManagerInterface commandManagerInterface;

        public ModuleComponent(final CommandManagerInterface commandManagerInterface) {
            this.commandManagerInterface = commandManagerInterface;

            VerticalLayout verticalLayout = new VerticalLayout();
            verticalLayout.setSpacing(true);

            Label labelText = new Label("Enter command:");
            textAreaCommand = new TextArea();
            textAreaCommand.setRows(20);
            textAreaCommand.setColumns(80);
            textAreaCommand.setImmediate(true);
            textAreaCommand.setWordwrap(true);

            verticalLayout.addComponent(labelText);
            verticalLayout.addComponent(textAreaCommand);

            buttonSend = new Button("Send");
            buttonSend.setDescription("Sends command to agent");
            buttonSend.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    try {
                        agents = AppData.getSelectedAgentList();
                        if (agents != null && agents.size() > 0) {
                            for (String agent : agents) {
                                String json = textAreaCommand.getValue().toString().trim();

                                Request r = CommandJson.getRequest(json);

                                if (r != null) {
                                    r.setUuid(agent);
                                    r.setSource(Terminal.MODULE_NAME);

                                    Command command = new Command(r);
                                    commandManagerInterface.executeCommand(command);
                                }
                            }
                        } else {
                            getWindow().showNotification("Select agent!");
                        }
                    } catch (Exception ex) {
                        getWindow().showNotification(ex.toString());
                        System.out.println("buttonClick event Exception");
                    }
                }
            });

            verticalLayout.addComponent(buttonSend);

            getRequests = new Button("Get requests");
            getRequests.setDescription("Gets requests from Cassandra");
            getRequests.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    List<Request> listofrequest = commandManagerInterface.getCommands();
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < listofrequest.size(); i++) {
                        Request request = listofrequest.get(i);
                        sb.append(request.getProgram()).append("\n");
                    }
                    textAreaOutput.setValue(sb.toString());
                }
            }); // react to clicks
            verticalLayout.addComponent(getRequests);

            getResponses = new Button("Get responses");
            getResponses.setDescription("Gets requests from Cassandra");
            getResponses.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    List<Response> list = commandManagerInterface.getResponses();
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < list.size(); i++) {
                        Response response = list.get(i);
                        sb.append("Task UUID: ").append(response.getTaskUuid()).append("\n");
                        sb.append(response.getUuid()).append(" ").append(response.getType()).append("\n");
                        sb.append(response.getExitCode()).append("\n");
                    }
                    textAreaOutput.setValue(sb.toString());
                }
            }); // react to clicks
            verticalLayout.addComponent(getResponses);

            Label labelOutput = new Label("Commands output");
            textAreaOutput = new TextArea();
            textAreaOutput.setRows(20);
            textAreaOutput.setColumns(80);
            textAreaOutput.setImmediate(true);
            textAreaOutput.setWordwrap(false);

            verticalLayout.addComponent(labelOutput);
            verticalLayout.addComponent(textAreaOutput);

            setCompositionRoot(verticalLayout);
        }

        @Override
        public void outputCommand(Response response) {
            commandManagerInterface.saveResponse(response);
            try {
                if (response != null && agents != null && agents.contains(response.getUuid())) {
                    System.out.println("TERMINAL outputCommand(Response response) called");

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
            } catch (Exception ex) {
                System.out.println("outputCommand event Exception");
            }

        }

        @Override
        public String getName() {
            return Terminal.MODULE_NAME;
        }
    }

    @Override
    public String getName() {
        return Terminal.MODULE_NAME;
    }

    @Override
    public Component createComponent() {
        CommandManagerInterface commandManagerInterface = getCommandManager();
        component = new ModuleComponent(commandManagerInterface);
        commandManagerInterface.addListener(component);

        return component;
//        return new ModuleComponent(context);
    }

    public void setModuleService(ModuleService service) {
        System.out.println("Terminal: registering with ModuleService");
        service.registerModule(this);
    }

    public void unsetModuleService(ModuleService service) {
        getCommandManager().removeListener(component);
        service.unregisterModule(this);
    }

    public void setContext(BundleContext context) {
        this.context = context;
    }

    private CommandManagerInterface getCommandManager() {
        ServiceReference reference = context
                .getServiceReference(CommandManagerInterface.class.getName());
        return (CommandManagerInterface) context.getService(reference);
    }
}
