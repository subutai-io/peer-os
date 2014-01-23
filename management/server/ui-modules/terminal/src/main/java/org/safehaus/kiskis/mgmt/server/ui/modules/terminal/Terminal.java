package org.safehaus.kiskis.mgmt.server.ui.modules.terminal;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import java.util.Set;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AgentManager;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.CommandListener;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.shared.protocol.api.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.api.TaskCallback;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.OutputRedirection;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.RequestType;

public class Terminal implements Module {

    public static final String MODULE_NAME = "Terminal";

    public static class ModuleComponent extends CustomComponent implements
            CommandListener {

        private final TextArea commandOutputTxtArea;
        private final AgentManager agentManager;
        private final TaskRunner taskRunner = new TaskRunner();

        public ModuleComponent() {
            agentManager = ServiceLocator.getService(AgentManager.class);

            GridLayout grid = new GridLayout(20, 2);
            grid.setSizeFull();
            grid.setMargin(true);
            grid.setSpacing(true);

            commandOutputTxtArea = new TextArea("Commands output");
            commandOutputTxtArea.setRows(40);
            commandOutputTxtArea.setWidth(100, Sizeable.UNITS_PERCENTAGE);
            commandOutputTxtArea.setImmediate(true);
            commandOutputTxtArea.setWordwrap(false);
            grid.addComponent(commandOutputTxtArea, 0, 0, 19, 0);

            Label programLbl = new Label("Program");
            final TextField programTxtFld = new TextField();
            programTxtFld.setValue("pwd");
            programTxtFld.setWidth(100, Sizeable.UNITS_PERCENTAGE);
            grid.addComponent(programLbl, 0, 1, 1, 1);
            grid.addComponent(programTxtFld, 2, 1, 12, 1);
            Label workDirLbl = new Label("WorkDir");
            final TextField workDirTxtFld = new TextField();
            workDirTxtFld.setValue("/");
            grid.addComponent(workDirLbl, 13, 1, 13, 1);
            grid.addComponent(workDirTxtFld, 14, 1, 14, 1);
            Label timeoutLbl = new Label("Timeout");
            final TextField timeoutTxtFld = new TextField();
            timeoutTxtFld.setValue("30");
            grid.addComponent(timeoutLbl, 15, 1, 16, 1);
            grid.addComponent(timeoutTxtFld, 17, 1, 17, 1);
            Button clearBtn = new Button("Clear");
            grid.addComponent(clearBtn, 18, 1, 18, 1);
            Button sendBtn = new Button("Send");
            grid.addComponent(sendBtn, 19, 1, 19, 1);

            setCompositionRoot(grid);

            sendBtn.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    Set<Agent> agents = MgmtApplication.getSelectedAgents();
                    if (agents.isEmpty()) {
                        show("Please, select nodes");
                    } else if (programTxtFld.getValue() == null || Util.isStringEmpty(programTxtFld.getValue().toString())) {
                        show("Please, enter command");
                    } else {
                        Task task = new Task();
                        for (Agent agent : agents) {
                            Command cmd = getTemplate();
                            cmd.getRequest().setUuid(agent.getUuid());
                            cmd.getRequest().setProgram(programTxtFld.getValue().toString());
                            if (timeoutTxtFld.getValue() != null && Util.isNumeric(timeoutTxtFld.getValue().toString())) {
                                int timeout = Integer.valueOf(timeoutTxtFld.getValue().toString());
                                if (timeout > 0) {
                                    cmd.getRequest().setTimeout(timeout);
                                }
                            }
                            if (workDirTxtFld.getValue() != null && !Util.isStringEmpty(workDirTxtFld.getValue().toString())) {
                                cmd.getRequest().setWorkingDirectory(workDirTxtFld.getValue().toString());
                            }

                            task.addCommand(cmd);
                        }
                        taskRunner.runTask(task, new TaskCallback() {

                            @Override
                            public void onResponse(Task task, Response response) {
                                if (response != null && response.getUuid() != null) {
                                    Agent agent = agentManager.getAgentByUUID(response.getUuid());
                                    String host = agent == null
                                            ? String.format("Offline[%s]", response.getUuid()) : agent.getHostname();

                                    StringBuilder out = new StringBuilder(host).append(":\n");
                                    if (!Util.isStringEmpty(response.getStdOut())) {
                                        out.append(response.getStdOut()).append("\n");
                                    }
                                    if (!Util.isStringEmpty(response.getStdErr())) {
                                        out.append(response.getStdErr()).append("\n");
                                    }

                                    addOutput(out.toString());
                                }
                            }
                        });
                    }
                }
            });

            clearBtn.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    commandOutputTxtArea.setValue("");
                }
            });

        }

        private void addOutput(String output) {
            if (!Util.isStringEmpty(output)) {
                commandOutputTxtArea.setValue(
                        String.format("%s\n\n%s",
                                commandOutputTxtArea.getValue(),
                                output));
                commandOutputTxtArea.setCursorPosition(commandOutputTxtArea.getValue().toString().length() - 1);
            }
        }

        private void show(String notification) {
            getWindow().showNotification(notification);
        }

        public static Command getTemplate() {
            return CommandFactory.createRequest(
                    RequestType.EXECUTE_REQUEST, // type
                    null, //                        !! agent uuid
                    MODULE_NAME, //     source
                    null, //                        !! task uuid 
                    1, //                           !! request sequence number
                    "/", //                         cwd
                    "pwd", //                        program
                    OutputRedirection.RETURN, //    std output redirection 
                    OutputRedirection.RETURN, //    std error redirection
                    null, //                        stdout capture file path
                    null, //                        stderr capture file path
                    "root", //                      runas
                    null, //                        arg
                    null, //                        env vars
                    30); //  
        }

        @Override
        public void onCommand(Response response) {
            taskRunner.feedResponse(response);
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
        return new ModuleComponent();
    }

}
