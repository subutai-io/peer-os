/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.server.ui;

import com.google.common.base.Strings;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.*;
import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.commandrunner.*;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.Response;
import org.safehaus.subutai.shared.protocol.enums.ResponseType;

import java.util.Set;

/**
 * @author dilshat
 */
public class TerminalWindow extends Window {

    private final TextArea commandOutputTxtArea;
    private volatile int taskCount = 0;

    public TerminalWindow(final Set<Agent> agents, final CommandRunner commandRunner, final AgentManager agentManager) {
        super(String.format("Shell"));
        setModal(true);
        setWidth(600, UNITS_PIXELS);
        setHeight(400, UNITS_PIXELS);

        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.setMargin(true);
        content.setSpacing(true);

        commandOutputTxtArea = new TextArea("Commands output");
        commandOutputTxtArea.setRows(15);
        commandOutputTxtArea.setColumns(43);
        commandOutputTxtArea.setImmediate(true);
        commandOutputTxtArea.setWordwrap(true);
        content.addComponent(commandOutputTxtArea);

        HorizontalLayout controls = new HorizontalLayout();
        controls.setSpacing(true);
        content.addComponent(controls);

        Label lblCommand = new Label("Command");
        final TextField txtCommand = new TextField();
        txtCommand.setWidth(250, UNITS_PIXELS);
        txtCommand.setValue("pwd");

        final Button clearBtn = new Button("Clear");
        final Button sendBtn = new Button("Send");
        final Label indicator = new Label();
        indicator.setIcon(new ThemeResource("icons/indicator.gif"));
        indicator.setContentMode(Label.CONTENT_XHTML);
        indicator.setHeight(11, UNITS_PIXELS);
        indicator.setWidth(50, UNITS_PIXELS);
        indicator.setVisible(false);

        controls.addComponent(lblCommand);
        controls.addComponent(txtCommand);
        controls.addComponent(clearBtn);
        controls.addComponent(sendBtn);
        controls.addComponent(indicator);

        txtCommand.addShortcutListener(new ShortcutListener("Shortcut Name", ShortcutAction.KeyCode.ENTER, null) {
            @Override
            public void handleAction(Object sender, Object target) {
                sendBtn.click();
            }
        });
        sendBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (!Strings.isNullOrEmpty(txtCommand.getValue().toString())) {
                    indicator.setVisible(true);
                    taskCount++;
                    final Command command = commandRunner.createCommand(new RequestBuilder(txtCommand.getValue().toString()), agents);
                    MgmtAppFactory.getExecutor().execute(new Runnable() {
                        @Override
                        public void run() {
                            commandRunner.runCommand(command, new CommandCallback() {

                                @Override
                                public void onResponse(Response response, AgentResult agentResult, Command command) {
                                    Agent agent = agentManager.getAgentByUUID(agentResult.getAgentUUID());
                                    String host = agent == null ? String.format("Offline[%s]", response.getUuid()) : agent.getHostname();
                                    StringBuilder out = new StringBuilder(host).append(":\n");
                                    if (!Strings.isNullOrEmpty(response.getStdOut())) {
                                        out.append(response.getStdOut()).append("\n");
                                    }
                                    if (!Strings.isNullOrEmpty(response.getStdErr())) {
                                        out.append(response.getStdErr()).append("\n");
                                    }
                                    if (response.isFinal()) {
                                        if (response.getType() == ResponseType.EXECUTE_RESPONSE_DONE) {
                                            out.append("Exit code: ").append(response.getExitCode()).append("\n\n");
                                        } else {
                                            out.append("Command timed out").append("\n\n");
                                        }
                                    }
                                    addOutput(out.toString());

                                }
                            });
                            taskCount--;
                            if (taskCount == 0) {
                                indicator.setVisible(false);
                            }
                        }
                    });
                } else {
                    addOutput("Please enter command to run");
                }
            }
        });
        clearBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                commandOutputTxtArea.setValue("");
            }
        });

        addComponent(content);
    }

    private void addOutput(String output) {
        if (!Strings.isNullOrEmpty(output)) {
            commandOutputTxtArea.setValue(String.format("%s%s", commandOutputTxtArea.getValue(), output));
            commandOutputTxtArea.setCursorPosition(commandOutputTxtArea.getValue().toString().length() - 1);
        }
    }

}
