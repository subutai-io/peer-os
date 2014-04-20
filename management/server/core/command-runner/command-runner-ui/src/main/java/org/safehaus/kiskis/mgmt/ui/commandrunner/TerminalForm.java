/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.ui.commandrunner;

import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.Runo;
import java.util.Date;
import java.util.Set;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.commandrunner.AgentResult;
import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.commandrunner.CommandCallback;
import org.safehaus.kiskis.mgmt.api.commandrunner.CommandRunner;
import org.safehaus.kiskis.mgmt.api.commandrunner.RequestBuilder;
import org.safehaus.kiskis.mgmt.server.ui.MgmtAgentManager;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Disposable;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.ResponseType;
import org.safehaus.kiskis.mgmt.shared.protocol.settings.Common;

/**
 *
 * @author dilshat
 */
public class TerminalForm extends CustomComponent implements Disposable {

    private final MgmtAgentManager agentTree;
    private final TextArea commandOutputTxtArea;
    private volatile int taskCount = 0;

    public TerminalForm(final CommandRunner commandRunner, final AgentManager agentManager) {
        setHeight(100, UNITS_PERCENTAGE);

        HorizontalSplitPanel horizontalSplit = new HorizontalSplitPanel();
        horizontalSplit.setStyleName(Runo.SPLITPANEL_SMALL);
        horizontalSplit.setSplitPosition(200, UNITS_PIXELS);
        agentTree = MgmtApplication.createAgentTree();
        horizontalSplit.setFirstComponent(agentTree);

        GridLayout grid = new GridLayout(20, 10);
        grid.setSizeFull();
        grid.setMargin(true);
        grid.setSpacing(true);
        commandOutputTxtArea = new TextArea("Commands output");
        commandOutputTxtArea.setSizeFull();
        commandOutputTxtArea.setImmediate(true);
        commandOutputTxtArea.setWordwrap(false);
        grid.addComponent(commandOutputTxtArea, 0, 0, 19, 8);
        Label programLbl = new Label("Program");
        final TextField programTxtFld = new TextField();
        programTxtFld.setValue("pwd");
        programTxtFld.setWidth(100, UNITS_PERCENTAGE);
        grid.addComponent(programLbl, 0, 9, 1, 9);
        grid.addComponent(programTxtFld, 2, 9, 11, 9);
        Label workDirLbl = new Label("Cwd");
        final TextField workDirTxtFld = new TextField();
        workDirTxtFld.setValue("/");
        grid.addComponent(workDirLbl, 12, 9, 12, 9);
        grid.addComponent(workDirTxtFld, 13, 9, 13, 9);
        Label timeoutLbl = new Label("Timeout");
        final TextField timeoutTxtFld = new TextField();
        timeoutTxtFld.setValue("30");
        grid.addComponent(timeoutLbl, 14, 9, 15, 9);
        grid.addComponent(timeoutTxtFld, 16, 9, 16, 9);
        Button clearBtn = new Button("Clear");
        grid.addComponent(clearBtn, 17, 9, 17, 9);
        final Button sendBtn = new Button("Send");
        grid.addComponent(sendBtn, 18, 9, 18, 9);
        final Label indicator = new Label();
        indicator.setIcon(new ThemeResource("icons/indicator.gif"));
        indicator.setContentMode(Label.CONTENT_XHTML);
        indicator.setHeight(11, UNITS_PIXELS);
        indicator.setWidth(50, UNITS_PIXELS);
        indicator.setVisible(false);
        grid.addComponent(indicator, 19, 9, 19, 9);

        horizontalSplit.setSecondComponent(grid);
        setCompositionRoot(horizontalSplit);

        programTxtFld.addShortcutListener(new ShortcutListener("Shortcut Name", ShortcutAction.KeyCode.ENTER, null) {
            @Override
            public void handleAction(Object sender, Object target) {
                sendBtn.click();
            }
        });
        sendBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                Set<Agent> agents = agentTree.getSelectedAgents();
                if (agents.isEmpty()) {
                    show("Please, select nodes");
                } else if (programTxtFld.getValue() == null || Util.isStringEmpty(programTxtFld.getValue().toString())) {
                    show("Please, enter command");
                } else {

                    RequestBuilder requestBuilder = new RequestBuilder(programTxtFld.getValue().toString());

                    if (timeoutTxtFld.getValue() != null && Util.isNumeric(timeoutTxtFld.getValue().toString())) {
                        int timeout = Integer.valueOf(timeoutTxtFld.getValue().toString());
                        if (timeout > 0 && timeout <= Common.MAX_COMMAND_TIMEOUT_SEC) {
                            requestBuilder.withTimeout(timeout);
                        }
                    }

                    if (workDirTxtFld.getValue() != null && !Util.isStringEmpty(workDirTxtFld.getValue().toString())) {
                        requestBuilder.withCwd(workDirTxtFld.getValue().toString());
                    }
                    Command command = commandRunner.createCommand(requestBuilder, agents);
                    indicator.setVisible(true);
                    taskCount++;
                    commandRunner.runCommandAsync(command, new CommandCallback() {

                        @Override
                        public void onResponse(Response response, AgentResult agentResult, Command command) {
                            Agent agent = agentManager.getAgentByUUID(response.getUuid());
                            String host = agent == null ? String.format("Offline[%s]", response.getUuid()) : agent.getHostname();
                            StringBuilder out = new StringBuilder(host).append(":\n");
                            if (!Util.isStringEmpty(response.getStdOut())) {
                                out.append(response.getStdOut()).append("\n");
                            }
                            if (!Util.isStringEmpty(response.getStdErr())) {
                                out.append(response.getStdErr()).append("\n");
                            }
                            if (Util.isFinalResponse(response)) {
                                if (response.getType() == ResponseType.EXECUTE_RESPONSE_DONE) {
                                    out.append("Exit code: ").append(response.getExitCode()).append("\n\n");
                                } else {
                                    out.append("Command timed out").append("\n\n");
                                }
                            }
                            addOutput(out.toString());
                            if (command.hasCompleted()) {
                                taskCount--;
                                if (taskCount == 0) {
                                    indicator.setVisible(false);
                                }
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
            commandOutputTxtArea.setValue(String.format("%s%s", commandOutputTxtArea.getValue(), output));
            commandOutputTxtArea.setCursorPosition(commandOutputTxtArea.getValue().toString().length() - 1);
        }
    }

    private void show(String notification) {
        getWindow().showNotification(notification);
    }

    public void dispose() {
        agentTree.dispose();
    }

}
