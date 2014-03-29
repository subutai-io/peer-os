/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.ui.mongodb.window;

import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskCallback;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.CommandFactory;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.OutputRedirection;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.RequestType;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.ResponseType;
import org.safehaus.kiskis.mgmt.ui.mongodb.MongoUI;

/**
 *
 * @author dilshat
 */
public class TerminalWindow extends Window {

    private final TextArea commandOutputTxtArea;
    private volatile int taskCount = 0;

    public TerminalWindow(final Agent agent) {
        super(String.format("Shell with %s", agent.getHostname()));
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
                if (!Util.isStringEmpty(txtCommand.getValue().toString())) {
                    Task task = new Task();
                    Request request = getRequestTemplate();
                    request.setProgram(txtCommand.getValue().toString());
                    task.addRequest(request, agent);
                    indicator.setVisible(true);
                    taskCount++;
                    MongoUI.getTaskRunner().executeTask(task, new TaskCallback() {
                        @Override
                        public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                            Agent agent = MongoUI.getAgentManager().getAgentByUUID(response.getUuid());
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
                            if (task.isCompleted()) {
                                taskCount--;
                                if (taskCount == 0) {
                                    indicator.setVisible(false);
                                }
                            }
                            return null;
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
        if (!Util.isStringEmpty(output)) {
            commandOutputTxtArea.setValue(String.format("%s%s", commandOutputTxtArea.getValue(), output));
            commandOutputTxtArea.setCursorPosition(commandOutputTxtArea.getValue().toString().length() - 1);
        }
    }

    public static Request getRequestTemplate() {
        return CommandFactory.newRequest(RequestType.EXECUTE_REQUEST, null, MongoUI.MODULE_NAME, null, 1, "/", "pwd", OutputRedirection.RETURN, OutputRedirection.RETURN, null, null, "root", null, null, 60); //
    }

}
