package org.safehaus.kiskis.mgmt;

import com.vaadin.ui.*;
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

    private ModuleService service;
    private BundleContext context;
    private static final String name = "Cassandra";

    public static final class ModuleComponent extends CustomComponent implements
            Button.ClickListener, CommandListener {

        private TextArea textAreaCommand;
        private TextArea textAreaOutput;
        private Button buttonSend;
        private BundleContext context;
        VerticalLayout verticalLayout;
        Panel panel;
        int step = 1;

        public ModuleComponent(BundleContext context) {
            this.context = context;
            panel = new Panel();
            panel.setWidth("400px");
            panel.setHeight("500px");
            verticalLayout = new VerticalLayout();
            verticalLayout.setSpacing(true);
            putForm();
            panel.setContent(verticalLayout);
            setCompositionRoot(panel);

            try {
                System.out.println("~~~~~~~~~~~~~~~~~~~~");
                System.out.println("Adding " + getName());
                getCommandManager().addListener(this);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
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
            System.out.println(step);
            switch (step) {
                case 1: {
                    Step1 step1 = new Step1(this);
                    verticalLayout.addComponent(step1);
                    break;
                }
                case 2: {
                    Step2 step2 = new Step2(this);
                    verticalLayout.addComponent(step2);
                    break;
                }
                case 3: {
                    Step3 step3 = new Step3(this);
                    verticalLayout.addComponent(step3);
                    break;
                }
                case 4: {
                    Step41 step41 = new Step41(this);
                    verticalLayout.addComponent(step41);
                    break;
                }
                case 5: {
                    Step42 step42 = new Step42(this);
                    verticalLayout.addComponent(step42);
                    break;
                }
                case 6: {
                    Step43 step43 = new Step43(this);
                    verticalLayout.addComponent(step43);
                    break;
                }
                case 7: {
                    Step5 step5 = new Step5(this);
                    verticalLayout.addComponent(step5);
                    break;
                }
                default: {
                    step = 1;
                    Step1 step1 = new Step1(this);
                    verticalLayout.addComponent(step1);
                    break;
                }
            }
        }

        @Override
        public void buttonClick(Button.ClickEvent event) {
            Set<String> agents = AppData.getAgentList();
            if (agents != null && agents.size() > 0) {
                for (String agent : agents) {
                    Request r = CommandJson.getRequest(textAreaCommand.getValue().toString());
                    r.setUuid(agent);
                    r.setSource(name);

                    Command command = new Command(r);
                    try {
                        getCommandManager().executeCommand(command);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            } else {
                getWindow().showNotification("Select agent!");
            }
        }

        @Override
        public synchronized void outputCommand(Response response) {
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

        private CommandManagerInterface getCommandManager() {
            ServiceReference reference = context
                    .getServiceReference(CommandManagerInterface.class.getName());
            return (CommandManagerInterface) context.getService(reference);
        }
    }

    public String getName() {
        return name;
    }

    public Component createComponent() {
        return new ModuleComponent(context);
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
